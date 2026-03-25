package ee.kaidokurm.ndl.health.profile;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HealthProfileRepository extends JpaRepository<HealthProfileEntity, UUID> {
    Optional<HealthProfileEntity> findByUserId(UUID userId);

    // Ownership verification
    @Query("SELECT hp FROM HealthProfileEntity hp WHERE hp.id = :id AND hp.userId = :userId AND hp.deletedAt IS NULL")
    Optional<HealthProfileEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    // Count for ownership check
    @Query("SELECT COUNT(hp) FROM HealthProfileEntity hp WHERE hp.id = :id AND hp.userId = :userId AND hp.deletedAt IS NULL")
    long countByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
