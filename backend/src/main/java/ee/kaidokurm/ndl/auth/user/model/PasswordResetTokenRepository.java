package ee.kaidokurm.ndl.auth.user.model;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for PasswordResetTokenEntity.
 * 
 * Handles all queries related to password reset tokens.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    /**
     * Find password reset token by its token string Used when user clicks reset
     * link and enters the token
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Find the latest (most recent) reset token for a user Can be used to check if
     * user has a pending reset
     */
    Optional<PasswordResetTokenEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
