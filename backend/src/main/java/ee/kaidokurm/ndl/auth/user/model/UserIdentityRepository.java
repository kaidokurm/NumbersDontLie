package ee.kaidokurm.ndl.auth.user.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, UUID> {
    Optional<UserIdentityEntity> findByProviderAndProviderSub(String provider, String providerSub);

    List<UserIdentityEntity> findAllByUserId(UUID userId);
}
