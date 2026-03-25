package ee.kaidokurm.ndl.health.goal;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "goals")
@SQLDelete(sql = "UPDATE goals SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GoalEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;

    @Column(name = "target_weight_kg")
    private Double targetWeightKg;

    @Column(name = "target_activity_days_per_week")
    private Integer targetActivityDaysPerWeek;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected GoalEntity() {
    }

    public GoalEntity(UUID id, UUID userId, GoalType goalType, Double targetWeightKg, Integer targetActivityDaysPerWeek,
            LocalDate targetDate, String notes, boolean active, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.goalType = goalType;
        this.targetWeightKg = targetWeightKg;
        this.targetActivityDaysPerWeek = targetActivityDaysPerWeek;
        this.targetDate = targetDate;
        this.notes = notes;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public Double getTargetWeightKg() {
        return targetWeightKg;
    }

    public Integer getTargetActivityDaysPerWeek() {
        return targetActivityDaysPerWeek;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
