package ee.kaidokurm.ndl.health.weight;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeightEntryRepository extends JpaRepository<WeightEntryEntity, UUID> {
    List<WeightEntryEntity> findTop30ByUserIdOrderByMeasuredAtDesc(UUID userId);

    List<WeightEntryEntity> findTop90ByUserIdOrderByMeasuredAtDesc(UUID userId);

    List<WeightEntryEntity> findByUserIdOrderByMeasuredAtDesc(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByUserIdAndMeasuredAt(UUID userId, java.time.OffsetDateTime measuredAt);

    boolean existsByUserIdAndMeasuredAtAndIdNot(UUID userId, java.time.OffsetDateTime measuredAt, UUID id);

    // Paginated queries
    Page<WeightEntryEntity> findByUserIdOrderByMeasuredAtDesc(UUID userId, Pageable pageable);

    // Ownership verification
    @Query("SELECT we FROM WeightEntryEntity we WHERE we.id = :id AND we.userId = :userId AND we.deletedAt IS NULL")
    Optional<WeightEntryEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    // Count for ownership check
    @Query("SELECT COUNT(we) FROM WeightEntryEntity we WHERE we.id = :id AND we.userId = :userId AND we.deletedAt IS NULL")
    long countByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
