package ee.kaidokurm.ndl.health.goal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO representing current and historical goal progress.
 * 
 * Used in API responses to provide structured progress information including: -
 * Progress percentage (0-100) - On-track status and days remaining - Milestone
 * tracking - Timestamp of last update
 */
public class GoalProgressResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("current_value")
    private BigDecimal currentValue;

    @JsonProperty("progress_percentage")
    private Integer progressPercentage;

    @JsonProperty("is_on_track")
    private Boolean isOnTrack;

    @JsonProperty("days_remaining")
    private Integer daysRemaining;

    @JsonProperty("milestones_completed")
    private Integer milestonesCompleted;

    @JsonProperty("milestone_details")
    private List<Map<String, Object>> milestoneDetails;

    @JsonProperty("recorded_at")
    private OffsetDateTime recordedAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    // Constructors
    public GoalProgressResponse() {
    }

    public GoalProgressResponse(GoalProgressEntity entity) {
        this.currentValue = entity.getCurrentValue();
        this.progressPercentage = entity.getProgressPercentage();
        this.isOnTrack = entity.getIsOnTrack();
        this.daysRemaining = entity.getDaysRemaining();
        this.milestonesCompleted = entity.getMilestonesCompleted();
        this.milestoneDetails = entity.getMilestoneDetails();
        this.recordedAt = entity.getRecordedAt();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

    // Getters and Setters
    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Boolean getIsOnTrack() {
        return isOnTrack;
    }

    public void setIsOnTrack(Boolean isOnTrack) {
        this.isOnTrack = isOnTrack;
    }

    public Integer getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Integer daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public Integer getMilestonesCompleted() {
        return milestonesCompleted;
    }

    public void setMilestonesCompleted(Integer milestonesCompleted) {
        this.milestonesCompleted = milestonesCompleted;
    }

    public List<Map<String, Object>> getMilestoneDetails() {
        return milestoneDetails;
    }

    public void setMilestoneDetails(List<Map<String, Object>> milestoneDetails) {
        this.milestoneDetails = milestoneDetails;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "GoalProgressResponse{" + "currentValue=" + currentValue + ", progressPercentage=" + progressPercentage
                + ", isOnTrack=" + isOnTrack + ", daysRemaining=" + daysRemaining + ", milestonesCompleted="
                + milestonesCompleted + ", recordedAt=" + recordedAt + '}';
    }
}
