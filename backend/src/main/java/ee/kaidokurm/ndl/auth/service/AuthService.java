package ee.kaidokurm.ndl.auth.service;

import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PasswordResetService passwordResetService, RefreshTokenService refreshTokenService,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new user with email and password. Validates: - Email is not
     * already registered - Password meets strength requirements
     * 
     * Returns the created user, or throws exception if validation fails.
     */
    @Transactional
    public UserEntity registerUser(String email, String password) {
        // Validate email format
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        email = email.toLowerCase().trim();

        // Check if email already registered
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Validate password
        String passwordError = passwordResetService.getPasswordValidationError(password);
        if (passwordError != null) {
            throw new IllegalArgumentException(passwordError);
        }

        // Create new user
        var user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmailVerified(false); // User must verify email
        user.setCreatedAt(OffsetDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Authenticate user by email and password. Returns the user if credentials are
     * valid, null otherwise.
     */
    public UserEntity authenticateUser(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        email = email.toLowerCase().trim();

        var user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return null;
        }

        // Check if user has password (email/password auth enabled)
        if (user.getPasswordHash() == null) {
            return null; // User registered via Auth0 only
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    /**
     * Update user password (for authenticated users changing their password).
     * Validates new password meets requirements.
     */
    @Transactional
    public boolean changePassword(UserEntity user, String currentPassword, String newPassword) {
        if (user.getPasswordHash() == null) {
            throw new IllegalStateException("User does not have password authentication enabled");
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }

        // Validate new password
        String passwordError = passwordResetService.getPasswordValidationError(newPassword);
        if (passwordError != null) {
            throw new IllegalArgumentException(passwordError);
        }

        // Cannot use same password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        return true;
    }

    /**
     * Request password reset (generates token). User receives token via email.
     */
    public String requestPasswordReset(String email) {
        email = email.toLowerCase().trim();

        var user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            // For security, don't reveal if email is registered
            // Still return a dummy token
            return UUID.randomUUID().toString();
        }

        // Only email/password users can reset password
        if (user.getPasswordHash() == null) {
            return UUID.randomUUID().toString();
        }

        return passwordResetService.generateResetToken(user);
    }

    /**
     * Complete password reset with token and new password.
     */
    @Transactional
    public boolean completePasswordReset(String token, String newPassword, String email) {
        email = email.toLowerCase().trim();

        var user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return false;
        }

        // Validate password
        String passwordError = passwordResetService.getPasswordValidationError(newPassword);
        if (passwordError != null) {
            throw new IllegalArgumentException(passwordError);
        }

        // Reset password using token
        if (!passwordResetService.resetPassword(token, newPassword, user)) {
            return false;
        }

        // Save user with updated password
        userRepository.save(user);
        return true;
    }

    /**
     * Validate and retrieve a refresh token. Returns the token entity if valid,
     * null otherwise.
     */
    public ee.kaidokurm.ndl.auth.model.RefreshTokenEntity validateRefreshToken(String token) {
        return refreshTokenService.validateToken(token);
    }

    /**
     * Generate an access token (JWT) for a user.
     */
    public String generateAccessToken(UserEntity user) {
        return jwtTokenProvider.generateAccessToken(user);
    }

    /**
     * Generate a refresh token and return the entity. Refresh tokens are valid for
     * 7 days.
     */
    public String generateRefreshToken(UserEntity user) {
        return refreshTokenService.generateRefreshToken(user.getId());
    }
}
