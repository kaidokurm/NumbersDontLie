package ee.kaidokurm.ndl.ai.insight;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiInsightRepository extends JpaRepository<AiInsightEntity, UUID> {
    Optional<AiInsightEntity> findFirstByUserIdAndInputHashOrderByCreatedAtDesc(UUID userId, String inputHash);

    Optional<AiInsightEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    List<AiInsightEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // Paginated queries
    Page<AiInsightEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Ownership verification
    @Query("SELECT ai FROM AiInsightEntity ai WHERE ai.id = :id AND ai.userId = :userId AND ai.deletedAt IS NULL")
    Optional<AiInsightEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    // Count for ownership check
    @Query("SELECT COUNT(ai) FROM AiInsightEntity ai WHERE ai.id = :id AND ai.userId = :userId AND ai.deletedAt IS NULL")
    long countByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
