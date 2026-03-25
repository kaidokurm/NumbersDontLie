package ee.kaidokurm.ndl.health.activity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityCheckinRepository extends JpaRepository<ActivityCheckinEntity, UUID> {

    List<ActivityCheckinEntity> findTop100ByUserIdOrderByCheckinAtDesc(UUID userId);

    List<ActivityCheckinEntity> findByUserIdOrderByCheckinAtDesc(UUID userId);

    List<ActivityCheckinEntity> findByUserIdAndCheckinAtAfterOrderByCheckinAtDesc(UUID userId, OffsetDateTime since);

    Page<ActivityCheckinEntity> findByUserIdOrderByCheckinAtDesc(UUID userId, Pageable pageable);

    boolean existsByUserIdAndCheckinAt(UUID userId, OffsetDateTime checkinAt);

    boolean existsByUserIdAndCheckinAtAndIdNot(UUID userId, OffsetDateTime checkinAt, UUID id);

    @Query("SELECT a FROM ActivityCheckinEntity a WHERE a.id = :id AND a.userId = :userId AND a.deletedAt IS NULL")
    Optional<ActivityCheckinEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
