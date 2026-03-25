package ee.kaidokurm.ndl.health.goal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    Optional<GoalEntity> findFirstByUserIdAndActiveTrue(UUID userId);

    Optional<GoalEntity> findActiveByUserId(UUID userId);

    // Paginated queries
    Page<GoalEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<GoalEntity> findByUserIdAndActiveTrue(UUID userId);

    List<GoalEntity> findByUserIdAndActiveTrueOrderByCreatedAtDesc(UUID userId);

    // Get all non-deleted goals for user
    @Query("SELECT g FROM GoalEntity g WHERE g.userId = :userId AND g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    List<GoalEntity> findAllByUserIdNotDeleted(@Param("userId") UUID userId);

    // Ownership verification
    @Query("SELECT g FROM GoalEntity g WHERE g.id = :id AND g.userId = :userId AND g.deletedAt IS NULL")
    Optional<GoalEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    // Count for ownership check
    @Query("SELECT COUNT(g) FROM GoalEntity g WHERE g.id = :id AND g.userId = :userId AND g.deletedAt IS NULL")
    long countByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
