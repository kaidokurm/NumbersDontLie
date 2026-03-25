package ee.kaidokurm.ndl.auth.service;

import ee.kaidokurm.ndl.auth.model.RefreshTokenEntity;
import ee.kaidokurm.ndl.auth.model.RefreshTokenRepository;
import ee.kaidokurm.ndl.auth.security.SensitiveTokenHasher;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing refresh tokens.
 * 
 * Refresh tokens enable users to obtain new access tokens without
 * re-authenticating. Tokens are valid for 7 days.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;
    private final SensitiveTokenHasher tokenHasher;

    // Refresh token validity period (days)
    private static final int TOKEN_VALIDITY_DAYS = 7;

    public RefreshTokenService(RefreshTokenRepository tokenRepository, SensitiveTokenHasher tokenHasher) {
        this.tokenRepository = tokenRepository;
        this.tokenHasher = tokenHasher;
    }

    /**
     * Generate and save a new refresh token for a user. Returns the token string to
     * be sent to client.
     */
    @Transactional
    public String generateRefreshToken(UUID userId) {
        // Generate unique token using UUID
        String tokenString = UUID.randomUUID().toString();

        var entity = new RefreshTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setToken(tokenHasher.hash(tokenString));
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setExpiresAt(OffsetDateTime.now().plusDays(TOKEN_VALIDITY_DAYS));

        tokenRepository.save(entity);

        return tokenString;
    }

    /**
     * Validate and retrieve a refresh token. Returns the token entity if valid, or
     * null if invalid/expired/revoked.
     */
    public RefreshTokenEntity validateToken(String token) {
        var entity = tokenRepository.findByToken(tokenHasher.hash(token)).orElse(null);
        if (entity == null) {
            // Legacy compatibility: previously tokens were stored plaintext.
            entity = tokenRepository.findByToken(token).orElse(null);
        }

        if (entity == null) {
            return null;
        }

        // Check if valid (not expired and not revoked)
        if (!entity.isValid()) {
            return null;
        }

        return entity;
    }

    /**
     * Revoke a refresh token (e.g., on logout).
     */
    @Transactional
    public void revokeToken(String token) {
        var entity = tokenRepository.findByToken(tokenHasher.hash(token)).orElse(null);
        if (entity == null) {
            entity = tokenRepository.findByToken(token).orElse(null);
        }

        if (entity != null && !entity.isRevoked()) {
            entity.revoke();
            tokenRepository.save(entity);
        }
    }

    /**
     * Revoke all refresh tokens for a user (e.g., on password change).
     */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        var tokens = tokenRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        tokens.ifPresent(token -> {
            if (!token.isRevoked()) {
                token.revoke();
                tokenRepository.save(token);
            }
        });
    }
}
