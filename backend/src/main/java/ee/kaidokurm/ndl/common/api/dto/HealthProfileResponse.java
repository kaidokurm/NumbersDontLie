package ee.kaidokurm.ndl.common.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for health profile response. Contains comprehensive health profile data
 * including demographics, fitness assessment, dietary preferences, and wellness
 * score.
 */
public class HealthProfileResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("birth_year")
    private Integer birthYear;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("height_cm")
    private Integer heightCm;

    @JsonProperty("baseline_activity_level")
    private String baselineActivityLevel;

    @JsonProperty("dietary_preferences")
    private List<String> dietaryPreferences;

    @JsonProperty("dietary_restrictions")
    private List<String> dietaryRestrictions;

    @JsonProperty("fitness_assessment")
    private Map<String, Object> fitnessAssessment;

    @JsonProperty("fitness_assessment_completed")
    private Boolean fitnessAssessmentCompleted;

    @JsonProperty("wellness_score")
    private Integer wellnessScore;

    @JsonProperty("bmi_value")
    private BigDecimal bmiValue;

    @JsonProperty("bmi_classification")
    private String bmiClassification;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    public HealthProfileResponse() {
    }

    public HealthProfileResponse(UUID userId, Integer birthYear, String gender, Integer heightCm,
            String baselineActivityLevel, List<String> dietaryPreferences, List<String> dietaryRestrictions,
            Map<String, Object> fitnessAssessment, Boolean fitnessAssessmentCompleted, Integer wellnessScore,
            BigDecimal bmiValue, String bmiClassification, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.userId = userId;
        this.birthYear = birthYear;
        this.gender = gender;
        this.heightCm = heightCm;
        this.baselineActivityLevel = baselineActivityLevel;
        this.dietaryPreferences = dietaryPreferences;
        this.dietaryRestrictions = dietaryRestrictions;
        this.fitnessAssessment = fitnessAssessment;
        this.fitnessAssessmentCompleted = fitnessAssessmentCompleted;
        this.wellnessScore = wellnessScore;
        this.bmiValue = bmiValue;
        this.bmiClassification = bmiClassification;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public String getBaselineActivityLevel() {
        return baselineActivityLevel;
    }

    public void setBaselineActivityLevel(String baselineActivityLevel) {
        this.baselineActivityLevel = baselineActivityLevel;
    }

    public List<String> getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(List<String> dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public Map<String, Object> getFitnessAssessment() {
        return fitnessAssessment;
    }

    public void setFitnessAssessment(Map<String, Object> fitnessAssessment) {
        this.fitnessAssessment = fitnessAssessment;
    }

    public Boolean getFitnessAssessmentCompleted() {
        return fitnessAssessmentCompleted;
    }

    public void setFitnessAssessmentCompleted(Boolean fitnessAssessmentCompleted) {
        this.fitnessAssessmentCompleted = fitnessAssessmentCompleted;
    }

    public Integer getWellnessScore() {
        return wellnessScore;
    }

    public void setWellnessScore(Integer wellnessScore) {
        this.wellnessScore = wellnessScore;
    }

    public BigDecimal getBmiValue() {
        return bmiValue;
    }

    public void setBmiValue(BigDecimal bmiValue) {
        this.bmiValue = bmiValue;
    }

    public String getBmiClassification() {
        return bmiClassification;
    }

    public void setBmiClassification(String bmiClassification) {
        this.bmiClassification = bmiClassification;
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
