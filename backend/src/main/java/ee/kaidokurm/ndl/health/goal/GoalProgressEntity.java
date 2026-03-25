package ee.kaidokurm.ndl.health.goal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

/**
 * Tracks progress towards a user's health goal.
 * 
 * Records periodic snapshots of progress, including: - Current value (e.g.,
 * current weight, current activity days) - Percentage complete towards target -
 * Whether user is on-track to meet goal by target date - Milestone completions
 * (e.g., every 5% progress)
 * 
 * This entity creates a historical record enabling trend analysis and progress
 * visualization.
 */
@Entity
@Table(name = "goal_progress")
@SQLDelete(sql = "UPDATE goal_progress SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GoalProgressEntity {

    @Id
    private UUID id;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "current_value")
    private BigDecimal currentValue;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "is_on_track")
    private Boolean isOnTrack = true;

    @Column(name = "days_remaining")
    private Integer daysRemaining;

    @Column(name = "milestones_completed")
    private Integer milestonesCompleted = 0;

    @Column(name = "milestone_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> milestoneDetails;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected GoalProgressEntity() {
    }

    public GoalProgressEntity(UUID id, UUID goalId, UUID userId, BigDecimal currentValue, Integer progressPercentage,
            Boolean isOnTrack, Integer daysRemaining, OffsetDateTime recordedAt, OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        this.id = id;
        this.goalId = goalId;
        this.userId = userId;
        this.currentValue = currentValue;
        this.progressPercentage = progressPercentage;
        this.isOnTrack = isOnTrack;
        this.daysRemaining = daysRemaining;
        this.recordedAt = recordedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public Boolean getIsOnTrack() {
        return isOnTrack;
    }

    public Integer getDaysRemaining() {
        return daysRemaining;
    }

    public Integer getMilestonesCompleted() {
        return milestonesCompleted;
    }

    public List<Map<String, Object>> getMilestoneDetails() {
        return milestoneDetails;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public void setIsOnTrack(Boolean isOnTrack) {
        this.isOnTrack = isOnTrack;
    }

    public void setDaysRemaining(Integer daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public void setMilestonesCompleted(Integer milestonesCompleted) {
        this.milestonesCompleted = milestonesCompleted;
    }

    public void setMilestoneDetails(List<Map<String, Object>> milestoneDetails) {
        this.milestoneDetails = milestoneDetails;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "GoalProgressEntity{" + "id=" + id + ", goalId=" + goalId + ", userId=" + userId + ", currentValue="
                + currentValue + ", progressPercentage=" + progressPercentage + ", isOnTrack=" + isOnTrack
                + ", daysRemaining=" + daysRemaining + ", milestonesCompleted=" + milestonesCompleted + '}';
    }
}
