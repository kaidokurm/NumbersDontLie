package ee.kaidokurm.ndl.auth.service;

import ee.kaidokurm.ndl.auth.email.EmailConfig;
import ee.kaidokurm.ndl.auth.email.EmailSender;
import ee.kaidokurm.ndl.auth.security.SensitiveTokenHasher;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.kaidokurm.ndl.auth.user.model.PasswordResetTokenEntity;
import ee.kaidokurm.ndl.auth.user.model.PasswordResetTokenRepository;
import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final EmailConfig emailConfig;
    private final SensitiveTokenHasher tokenHasher;

    // Password reset token validity period (hours)
    private static final int TOKEN_VALIDITY_HOURS = 1;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, EmailSender emailSender, EmailConfig emailConfig,
            SensitiveTokenHasher tokenHasher) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.emailConfig = emailConfig;
        this.tokenHasher = tokenHasher;
    }

    /**
     * Request a password reset token for a user by email. If user doesn't exist,
     * silently returns (for security).
     */
    @Transactional
    public void requestPasswordReset(String email) {
        var user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user != null && Boolean.TRUE.equals(user.getEmailVerified())) {
            // Generate and save token (will be sent to user via email)
            String token = generateResetToken(user);
            String resetUrl = emailConfig.getFrontendUrl()
                    + "/reset-password?email="
                    + java.net.URLEncoder.encode(user.getEmail(), java.nio.charset.StandardCharsets.UTF_8)
                    + "&token="
                    + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8);
            emailSender.sendPasswordReset(user.getEmail(), resetUrl);
        }
        // Silently return even if user doesn't exist (for security)
    }

    /**
     * Complete password reset with token and new password. Returns true if password
     * was successfully reset.
     */
    @Transactional
    public boolean completePasswordReset(String token, String newPassword, String email) {
        // Validate password
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException(getPasswordValidationError(newPassword));
        }

        // Find user by email
        var user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return false;
        }

        // Reset password using token
        boolean success = resetPassword(token, newPassword, user);

        if (success) {
            // Save user with new password
            userRepository.save(user);
        }

        return success;
    }

    /**
     * Generate a password reset token for a user. The token is valid for 1 hour.
     */
    @Transactional
    public String generateResetToken(UserEntity user) {
        // Generate unique token
        String token = UUID.randomUUID().toString();

        var entity = new PasswordResetTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(user.getId());
        entity.setToken(tokenHasher.hash(token));
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setExpiresAt(OffsetDateTime.now().plusHours(TOKEN_VALIDITY_HOURS));

        tokenRepository.save(entity);

        return token;
    }

    /**
     * Validate a reset token. Returns the user associated with the token, or null
     * if invalid.
     */
    public PasswordResetTokenEntity validateToken(String token) {
        var entity = tokenRepository.findByToken(tokenHasher.hash(token)).orElse(null);
        if (entity == null) {
            // Legacy compatibility: previously token was stored plaintext.
            entity = tokenRepository.findByToken(token).orElse(null);
        }

        if (entity == null) {
            return null;
        }

        // Check if expired
        if (entity.isExpired()) {
            return null;
        }

        // Check if already used
        if (entity.isAlreadyUsed()) {
            return null;
        }

        return entity;
    }

    /**
     * Reset user password using a valid token. Returns true if password was
     * successfully reset.
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword, UserEntity user) {
        var tokenEntity = validateToken(token);

        if (tokenEntity == null) {
            return false;
        }

        // Check if token belongs to this user
        if (!tokenEntity.getUserId().equals(user.getId())) {
            return false;
        }

        // Validate password
        if (!isValidPassword(newPassword)) {
            return false;
        }

        // Update user password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(OffsetDateTime.now());
        // Note: User entity is saved by caller via repository

        // Mark token as used
        tokenEntity.markAsUsed();
        tokenRepository.save(tokenEntity);

        return true;
    }

    /**
     * Validate password strength. Requirements: - At least 8 characters - At least
     * one uppercase letter - At least one lowercase letter - At least one digit -
     * At least one special character
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Get password validation error message.
     */
    public String getPasswordValidationError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }

        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return "Password must contain at least one special character (!@#$%^&* etc.)";
        }

        return null; // Password is valid
    }
}
