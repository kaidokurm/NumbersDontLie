package ee.kaidokurm.ndl.ai.insight;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_insights")
@SQLDelete(sql = "UPDATE ai_insights SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class AiInsightEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "input_hash", nullable = false)
    private String inputHash;

    @Column(name = "model", nullable = false)
    private String model;

    // Store payload as structured JSON; DB column is jsonb
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected AiInsightEntity() {
    }

    public AiInsightEntity(UUID id, UUID userId, UUID goalId, String inputHash, String model, JsonNode payload,
            OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.inputHash = inputHash;
        this.model = model;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public String getInputHash() {
        return inputHash;
    }

    public String getModel() {
        return model;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
