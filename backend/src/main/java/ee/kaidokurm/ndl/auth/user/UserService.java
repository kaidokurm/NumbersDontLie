package ee.kaidokurm.ndl.auth.user;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.user.model.UserIdentityEntity;
import ee.kaidokurm.ndl.auth.user.model.UserIdentityRepository;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repo;
    private final UserIdentityRepository identityRepo;
    private final Auth0UserInfoService auth0UserInfoService;
    private final ConcurrentHashMap<String, Object> userLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userInfoAttemptAt = new ConcurrentHashMap<>();

    public UserService(
            UserRepository repo,
            UserIdentityRepository identityRepo,
            Auth0UserInfoService auth0UserInfoService) {
        this.repo = repo;
        this.identityRepo = identityRepo;
        this.auth0UserInfoService = auth0UserInfoService;
    }

    @Transactional
    public UserEntity ensureUser(String auth0Sub, String emailOrNull) {
        return ensureUser(auth0Sub, emailOrNull, emailOrNull != null ? Boolean.TRUE : null);
    }

    @Transactional
    public UserEntity ensureUser(String auth0Sub, String emailOrNull, Boolean emailVerifiedOrNull) {
        log.debug("ensureUser called: auth0Sub={}, email={}", auth0Sub, emailOrNull);
        Object lock = userLocks.computeIfAbsent(auth0Sub, ignored -> new Object());
        synchronized (lock) {
            try {
                // Local JWT tokens use subject = internal user UUID; never create a new user in
                // this flow.
                try {
                    UUID localUserId = UUID.fromString(auth0Sub);
                    var localUser = repo.findById(localUserId).orElse(null);
                    if (localUser != null) {
                        if (emailOrNull != null && !emailOrNull.equals(localUser.getEmail())) {
                            localUser.setEmail(emailOrNull);
                            if (emailVerifiedOrNull != null) {
                                localUser.setEmailVerified(emailVerifiedOrNull);
                            }
                            localUser.setUpdatedAt(OffsetDateTime.now());
                            try {
                                repo.save(localUser);
                            } catch (DataIntegrityViolationException ex) {
                                // Do not fail requests when email is already used by another account.
                                log.warn("Skipping local user email sync due to unique constraint: userId={}, email={}",
                                        localUser.getId(), emailOrNull);
                            }
                        }
                        return localUser;
                    }
                } catch (IllegalArgumentException ignored) {
                    // Not a UUID subject, continue with OAuth subject flow.
                }

                IdentityKey key = IdentityKey.fromSubject(auth0Sub);
                UserEntity user = findUserByIdentityKey(key);
                if (user == null) {
                    // Legacy fallback while users.auth0_sub still exists.
                    user = repo.findByAuth0Sub(auth0Sub).orElse(null);
                    if (user != null) {
                        ensureIdentityLinked(user, key, emailOrNull);
                    }
                }

                if (user == null) {
                    rejectEmailCollisionIfAny(emailOrNull, auth0Sub, key.provider());
                    // First login: create user + identity.
                    log.info("Creating new user: auth0Sub={}, email={}", auth0Sub, emailOrNull);
                    user = createOAuthUser(auth0Sub, emailOrNull, emailVerifiedOrNull, key.provider());
                    ensureIdentityLinked(user, key, emailOrNull);
                }

                boolean changed = false;
                if (emailOrNull != null && !emailOrNull.equals(user.getEmail())) {
                    log.info("Updating user email: auth0Sub={}, oldEmail={}, newEmail={}", auth0Sub, user.getEmail(),
                            emailOrNull);
                    user.setEmail(emailOrNull);
                    changed = true;
                }
                if (emailVerifiedOrNull != null && !Objects.equals(user.getEmailVerified(), emailVerifiedOrNull)) {
                    user.setEmailVerified(emailVerifiedOrNull);
                    changed = true;
                }
                if (changed) {
                    user.setUpdatedAt(OffsetDateTime.now());
                    try {
                        user = repo.save(user);
                    } catch (DataIntegrityViolationException ex) {
                        // Email may already belong to another account. Keep current user state
                        // instead of failing all parallel dashboard calls.
                        log.warn("Skipping user email sync due to unique constraint: auth0Sub={}, email={}", auth0Sub,
                                emailOrNull);
                        UserEntity byIdentity = findUserByIdentityKey(key);
                        user = byIdentity != null ? byIdentity : repo.findByAuth0Sub(auth0Sub).orElse(user);
                    }
                }

                return user;
            } finally {
                userLocks.remove(auth0Sub, lock);
            }
        }
    }

    /**
     * Convenience method: Extract auth0Sub and email from JWT, then ensure user
     * exists. Email is extracted from the "email" claim if present.
     */
    @Transactional
    public UserEntity ensureUserFromJwt(JwtAuthenticationToken auth) {
        String auth0Sub = auth.getToken().getSubject();
        Map<String, Object> claims = auth.getToken().getClaims();
        String issuer = auth.getToken().getClaimAsString("iss");
        UserEntity existingUser = resolveUserBySubject(auth0Sub);

        String email = getClaimAsString(claims, "email");
        Boolean emailVerified = getClaimAsBoolean(claims, "email_verified");

        // Some Auth0 access tokens omit email/email_verified; try custom claim names.
        if (email == null) {
            for (var entry : claims.entrySet()) {
                if (entry.getKey().endsWith("/email") && entry.getValue() instanceof String value) {
                    email = value;
                    break;
                }
            }
        }
        if (emailVerified == null) {
            for (var entry : claims.entrySet()) {
                if (entry.getKey().endsWith("/email_verified") && entry.getValue() instanceof Boolean value) {
                    emailVerified = value;
                    break;
                }
            }
        }

        // Prefer stored user email to avoid /userinfo rate limits.
        if (email == null && existingUser != null && existingUser.getEmail() != null) {
            email = existingUser.getEmail();
            if (emailVerified == null) {
                emailVerified = existingUser.getEmailVerified();
            }
        }

        // Last fallback: call Auth0 /userinfo with the bearer token.
        if (email == null && shouldAttemptUserInfo(auth0Sub)) {
            userInfoAttemptAt.put(auth0Sub, System.currentTimeMillis());
            var userInfo = auth0UserInfoService.fetchUserInfo(auth.getToken().getTokenValue(), issuer);
            if (userInfo.email() != null) {
                email = userInfo.email();
                userInfoAttemptAt.remove(auth0Sub);
            }
            if (emailVerified == null) {
                emailVerified = userInfo.emailVerified();
            }
        }

        log.debug("ensureUserFromJwt called: sub={}, email={}, emailVerified={}", auth0Sub, email, emailVerified);
        return ensureUser(auth0Sub, email, emailVerified);
    }

    @Transactional(readOnly = true)
    public AccountAuthMethods getAccountAuthMethods(UUID userId) {
        var user = repo.findById(userId).orElseThrow();
        List<LinkedIdentity> identities = identityRepo.findAllByUserId(userId).stream()
                .map(identity -> new LinkedIdentity(
                        identity.getProvider(),
                        identity.getProviderSub(),
                        identity.getCreatedAt()))
                .sorted((a, b) -> a.provider().compareToIgnoreCase(b.provider()))
                .toList();
        return new AccountAuthMethods(user.getPasswordHash() != null, identities);
    }

    @Transactional
    public AccountLinkResult linkCurrentOAuthIdentityByEmail(JwtAuthenticationToken auth) {
        String auth0Sub = auth.getToken().getSubject();
        IdentityKey key = IdentityKey.fromSubject(auth0Sub);

        try {
            UUID.fromString(auth0Sub);
            throw new IllegalStateException("Local accounts cannot be linked through OAuth identity linking");
        } catch (IllegalArgumentException ignored) {
            // OAuth subject flow.
        }

        var existingIdentity = identityRepo.findByProviderAndProviderSub(key.provider(), key.providerSub())
                .orElse(null);
        if (existingIdentity != null) {
            if (repo.findById(existingIdentity.getUserId()).isPresent()) {
                return new AccountLinkResult(existingIdentity.getUserId(), true, "Identity already linked");
            }
            throw new IllegalStateException("Identity is linked to a missing user record");
        }

        String issuer = auth.getToken().getClaimAsString("iss");
        String email = getClaimAsString(auth.getToken().getClaims(), "email");
        Boolean emailVerified = getClaimAsBoolean(auth.getToken().getClaims(), "email_verified");

        if (email == null && shouldAttemptUserInfo(auth0Sub)) {
            userInfoAttemptAt.put(auth0Sub, System.currentTimeMillis());
            var userInfo = auth0UserInfoService.fetchUserInfo(auth.getToken().getTokenValue(), issuer);
            email = userInfo.email();
            if (emailVerified == null) {
                emailVerified = userInfo.emailVerified();
            }
            if (email != null) {
                userInfoAttemptAt.remove(auth0Sub);
            }
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException(
                    "Cannot link account because email is missing from identity provider");
        }
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalStateException(
                    "Cannot link account because provider email is not verified");
        }

        var existingUser = repo.findByEmailIgnoreCase(email).orElse(null);
        if (existingUser == null) {
            throw new IllegalStateException(
                    "No existing account found for this email. Sign in with your intended primary method first.");
        }

        ensureIdentityLinked(existingUser, key, email);

        if (!Boolean.TRUE.equals(existingUser.getEmailVerified())) {
            existingUser.setEmailVerified(true);
            existingUser.setUpdatedAt(OffsetDateTime.now());
            repo.save(existingUser);
        }

        return new AccountLinkResult(existingUser.getId(), false, "Identity linked successfully");
    }

    private UserEntity resolveUserBySubject(String auth0Sub) {
        try {
            UUID localUserId = UUID.fromString(auth0Sub);
            return repo.findById(localUserId).orElse(null);
        } catch (IllegalArgumentException ignored) {
            // OAuth subject.
        }
        IdentityKey key = IdentityKey.fromSubject(auth0Sub);
        UserEntity byIdentity = findUserByIdentityKey(key);
        if (byIdentity != null) {
            return byIdentity;
        }
        return repo.findByAuth0Sub(auth0Sub).orElse(null);
    }

    private UserEntity findUserByIdentityKey(IdentityKey key) {
        return identityRepo.findByProviderAndProviderSub(key.provider(), key.providerSub())
                .flatMap(identity -> repo.findById(identity.getUserId()))
                .orElse(null);
    }

    private void ensureIdentityLinked(UserEntity user, IdentityKey key, String emailAtLinkTime) {
        var existing = identityRepo.findByProviderAndProviderSub(key.provider(), key.providerSub()).orElse(null);
        if (existing != null) {
            if (!user.getId().equals(existing.getUserId())) {
                throw new IllegalStateException("This identity is already linked to a different account");
            }
            return;
        }
        var now = OffsetDateTime.now();
        var identity = new UserIdentityEntity();
        identity.setId(UUID.randomUUID());
        identity.setUserId(user.getId());
        identity.setProvider(key.provider());
        identity.setProviderSub(key.providerSub());
        identity.setEmailAtLinkTime(emailAtLinkTime);
        identity.setCreatedAt(now);
        identity.setUpdatedAt(now);
        try {
            identityRepo.save(identity);
        } catch (DataIntegrityViolationException ex) {
            // Concurrent insert by another request: verify ownership.
            var concurrent = identityRepo.findByProviderAndProviderSub(key.provider(), key.providerSub())
                    .orElseThrow(() -> ex);
            if (!user.getId().equals(concurrent.getUserId())) {
                throw new IllegalStateException("This identity is already linked to a different account");
            }
        }
    }

    private UserEntity createOAuthUser(
            String auth0Sub,
            String emailOrNull,
            Boolean emailVerifiedOrNull,
            String provider) {
        var now = OffsetDateTime.now();
        var newUser = new UserEntity(UUID.randomUUID(), auth0Sub, emailOrNull, now);
        if (emailOrNull != null && emailVerifiedOrNull != null) {
            newUser.setEmailVerified(emailVerifiedOrNull);
        }
        try {
            return repo.save(newUser);
        } catch (DataIntegrityViolationException ex) {
            UserEntity existingBySub = repo.findByAuth0Sub(auth0Sub).orElse(null);
            if (existingBySub != null) {
                return existingBySub;
            }
            // If another account already owns this email, block and guide the user.
            if (emailOrNull != null) {
                var existingByEmail = repo.findByEmailIgnoreCase(emailOrNull).orElse(null);
                if (existingByEmail != null) {
                    throw new IllegalStateException(emailCollisionMessage(provider));
                }
            }
            // Keep auth working even when email unique constraint is hit.
            var fallbackUser = new UserEntity(UUID.randomUUID(), auth0Sub, null, now);
            fallbackUser.setEmailVerified(false);
            try {
                return repo.save(fallbackUser);
            } catch (DataIntegrityViolationException secondEx) {
                return repo.findByAuth0Sub(auth0Sub).orElseThrow(() -> secondEx);
            }
        }
    }

    private String getClaimAsString(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value instanceof String ? (String) value : null;
    }

    private Boolean getClaimAsBoolean(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private boolean shouldAttemptUserInfo(String auth0Sub) {
        Long lastAttempt = userInfoAttemptAt.get(auth0Sub);
        if (lastAttempt == null) {
            return true;
        }
        return (System.currentTimeMillis() - lastAttempt) > 30_000;
    }

    private void rejectEmailCollisionIfAny(String emailOrNull, String auth0Sub, String provider) {
        if (emailOrNull == null || emailOrNull.isBlank()) {
            return;
        }
        var existingByEmail = repo.findByEmailIgnoreCase(emailOrNull).orElse(null);
        if (existingByEmail == null) {
            return;
        }
        if (auth0Sub.equals(existingByEmail.getAuth0Sub())) {
            return;
        }
        throw new IllegalStateException(emailCollisionMessage(provider));
    }

    private String emailCollisionMessage(String provider) {
        return "An account with this email already exists. Sign in with your existing method, then link "
                + providerDisplayName(provider) + " in account settings.";
    }

    private String providerDisplayName(String provider) {
        if ("google-oauth2".equalsIgnoreCase(provider)) {
            return "Google";
        }
        if ("github".equalsIgnoreCase(provider)) {
            return "GitHub";
        }
        if (provider == null || provider.isBlank()) {
            return "this provider";
        }
        return provider;
    }

    private record IdentityKey(String provider, String providerSub) {
        static IdentityKey fromSubject(String subject) {
            int separator = subject.indexOf('|');
            if (separator <= 0 || separator >= subject.length() - 1) {
                return new IdentityKey("auth0", subject);
            }
            return new IdentityKey(subject.substring(0, separator), subject.substring(separator + 1));
        }
    }

    public record LinkedIdentity(String provider, String providerSub, OffsetDateTime linkedAt) {
    }

    public record AccountAuthMethods(boolean hasPassword, List<LinkedIdentity> identities) {
    }

    public record AccountLinkResult(UUID userId, boolean alreadyLinked, String message) {
    }
}
