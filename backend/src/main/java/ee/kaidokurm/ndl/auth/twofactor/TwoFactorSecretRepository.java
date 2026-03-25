package ee.kaidokurm.ndl.auth.twofactor;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorSecretRepository extends JpaRepository<TwoFactorSecretEntity, UUID> {
    Optional<TwoFactorSecretEntity> findByUserId(UUID userId);
}
