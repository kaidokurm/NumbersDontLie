package ee.kaidokurm.ndl.common.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for goal response. Contains goal metadata including type, target values,
 * and timestamps.
 */
public class GoalResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("goal_type")
    private String goalType;

    @JsonProperty("target_weight_kg")
    private Double targetWeightKg;

    @JsonProperty("target_activity_days_per_week")
    private Integer targetActivityDaysPerWeek;

    @JsonProperty("target_date")
    private LocalDate targetDate;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    public GoalResponse() {
    }

    public GoalResponse(UUID id, UUID userId, String goalType, Double targetWeightKg, Integer targetActivityDaysPerWeek,
            LocalDate targetDate, String notes, Boolean isActive, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.goalType = goalType;
        this.targetWeightKg = targetWeightKg;
        this.targetActivityDaysPerWeek = targetActivityDaysPerWeek;
        this.targetDate = targetDate;
        this.notes = notes;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public Double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(Double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public Integer getTargetActivityDaysPerWeek() {
        return targetActivityDaysPerWeek;
    }

    public void setTargetActivityDaysPerWeek(Integer targetActivityDaysPerWeek) {
        this.targetActivityDaysPerWeek = targetActivityDaysPerWeek;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
}
