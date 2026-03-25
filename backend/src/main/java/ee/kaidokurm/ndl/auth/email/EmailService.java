package ee.kaidokurm.ndl.auth.email;

import ee.kaidokurm.ndl.auth.user.model.EmailVerificationCodeEntity;
import ee.kaidokurm.ndl.auth.user.model.EmailVerificationCodeRepository;
import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.security.SensitiveTokenHasher;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailService {

    private final EmailVerificationCodeRepository codeRepository;
    private final EmailSender emailSender;
    private final SensitiveTokenHasher tokenHasher;
    private final Random random = new Random();

    // Email verification code validity period
    private static final int CODE_VALIDITY_HOURS = 24;
    // Minimum time between resend attempts (minutes)
    private static final int RESEND_COOLDOWN_MINUTES = 1;

    public EmailService(EmailVerificationCodeRepository codeRepository, EmailSender emailSender,
            SensitiveTokenHasher tokenHasher) {
        this.codeRepository = codeRepository;
        this.emailSender = emailSender;
        this.tokenHasher = tokenHasher;
    }

    /**
     * Generate and save a new 6-digit verification code for a user. Invalidates any
     * previous codes for this user.
     */
    @Transactional
    public String generateVerificationCode(UserEntity user) {
        if (user.getEmail() == null) {
            throw new IllegalArgumentException("User email is required to generate verification code");
        }

        // Generate 6-digit code
        String code = String.format("%06d", random.nextInt(1000000));

        var entity = new EmailVerificationCodeEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(user.getId());
        entity.setCode(tokenHasher.hash(code));
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setExpiresAt(OffsetDateTime.now().plusHours(CODE_VALIDITY_HOURS));

        codeRepository.save(entity);

        // Send verification code to email (or log if testing)
        emailSender.sendVerificationCode(user.getEmail(), code);

        return code;
    }

    /**
     * Verify a code and mark it as verified. Returns true if code is valid, not
     * expired, not already used.
     */
    @Transactional
    public boolean verifyCode(String code, UserEntity user) {
        var codeEntity = codeRepository.findByCode(tokenHasher.hash(code)).orElse(null);
        if (codeEntity == null) {
            // Legacy compatibility: previously code was stored plaintext.
            codeEntity = codeRepository.findByCode(code).orElse(null);
        }

        if (codeEntity == null) {
            return false;
        }

        // Check if code belongs to this user
        if (!codeEntity.getUserId().equals(user.getId())) {
            return false;
        }

        // Check if already verified
        if (codeEntity.isAlreadyVerified()) {
            return false;
        }

        // Check if expired
        if (codeEntity.isExpired()) {
            return false;
        }

        // Mark as verified
        codeEntity.markAsVerified();
        codeRepository.save(codeEntity);

        return true;
    }

    /**
     * Check if user can resend verification code (respects cooldown). Returns true
     * if enough time has passed since last resend.
     */
    public boolean canResendCode(UserEntity user) {
        var lastCode = codeRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElse(null);

        if (lastCode == null) {
            return true; // Never sent before
        }

        if (lastCode.getLastResentAt() == null) {
            // Code was generated but never resent, check creation time
            return OffsetDateTime.now().isAfter(lastCode.getCreatedAt().plusMinutes(RESEND_COOLDOWN_MINUTES));
        } else {
            // Check time since last resend
            return OffsetDateTime.now().isAfter(lastCode.getLastResentAt().plusMinutes(RESEND_COOLDOWN_MINUTES));
        }
    }

    /**
     * Get time remaining until user can resend (in seconds). Returns 0 if user can
     * resend now.
     */
    public long getResendCooldownSeconds(UserEntity user) {
        var lastCode = codeRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()).orElse(null);

        if (lastCode == null || canResendCode(user)) {
            return 0;
        }

        OffsetDateTime nextResendTime = lastCode.getLastResentAt() != null
                ? lastCode.getLastResentAt().plusMinutes(RESEND_COOLDOWN_MINUTES)
                : lastCode.getCreatedAt().plusMinutes(RESEND_COOLDOWN_MINUTES);

        return java.time.temporal.ChronoUnit.SECONDS.between(OffsetDateTime.now(), nextResendTime);
    }
}
