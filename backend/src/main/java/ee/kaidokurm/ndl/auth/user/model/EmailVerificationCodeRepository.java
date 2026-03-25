package ee.kaidokurm.ndl.auth.user.model;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for EmailVerificationCodeEntity.
 * 
 * Handles all queries related to email verification codes.
 */
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCodeEntity, UUID> {

    /**
     * Find the latest (most recent) verification code for a user Used to get the
     * current code to verify
     */
    Optional<EmailVerificationCodeEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find verification code by the actual code value Used when user enters code in
     * form
     */
    Optional<EmailVerificationCodeEntity> findByCode(String code);

    /**
     * Find unverified code for user (verified_at is null)
     */
    Optional<EmailVerificationCodeEntity> findByUserIdAndVerifiedAtIsNull(UUID userId);
}
