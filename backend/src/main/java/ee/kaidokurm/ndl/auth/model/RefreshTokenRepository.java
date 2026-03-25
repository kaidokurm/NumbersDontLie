package ee.kaidokurm.ndl.auth.model;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    /**
     * Find a refresh token by its token string. Used when user requests a new
     * access token.
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Find the latest (most recent) refresh token for a user. Can be used to check
     * if user has an active session.
     */
    Optional<RefreshTokenEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
