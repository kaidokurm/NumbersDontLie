package ee.kaidokurm.ndl.health.summary;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * DTO for weekly/monthly health summaries Aggregates metrics: weight change,
 * wellness score, activity level, goal progress
 */
public class PeriodSummaryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("period_type")
    private String periodType; // "weekly" or "monthly"

    @JsonProperty("start_date")
    private String startDate; // ISO date

    @JsonProperty("end_date")
    private String endDate; // ISO date

    @JsonProperty("weight_start_kg")
    private Double weightStartKg;

    @JsonProperty("weight_end_kg")
    private Double weightEndKg;

    @JsonProperty("weight_change_kg")
    private Double weightChangeKg;

    @JsonProperty("avg_wellness_score")
    private Double avgWellnessScore;

    @JsonProperty("activity_level")
    private String activityLevel; // from profile

    @JsonProperty("goal_progress_percentage")
    private Integer goalProgressPercentage;

    @JsonProperty("days_tracked")
    private Integer daysTracked;

    @JsonProperty("weight_entries_count")
    private Integer weightEntriesCount;

    @JsonProperty("generated_at")
    private OffsetDateTime generatedAt;

    public PeriodSummaryDto() {
    }

    public PeriodSummaryDto(String periodType, String startDate, String endDate, Double weightStartKg,
            Double weightEndKg, Double weightChangeKg, Double avgWellnessScore, String activityLevel,
            Integer goalProgressPercentage, Integer daysTracked, Integer weightEntriesCount) {
        this.periodType = periodType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weightStartKg = weightStartKg;
        this.weightEndKg = weightEndKg;
        this.weightChangeKg = weightChangeKg;
        this.avgWellnessScore = avgWellnessScore;
        this.activityLevel = activityLevel;
        this.goalProgressPercentage = goalProgressPercentage;
        this.daysTracked = daysTracked;
        this.weightEntriesCount = weightEntriesCount;
        this.generatedAt = OffsetDateTime.now();
    }

    // Getters
    public String getPeriodType() {
        return periodType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Double getWeightStartKg() {
        return weightStartKg;
    }

    public Double getWeightEndKg() {
        return weightEndKg;
    }

    public Double getWeightChangeKg() {
        return weightChangeKg;
    }

    public Double getAvgWellnessScore() {
        return avgWellnessScore;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public Integer getGoalProgressPercentage() {
        return goalProgressPercentage;
    }

    public Integer getDaysTracked() {
        return daysTracked;
    }

    public Integer getWeightEntriesCount() {
        return weightEntriesCount;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
