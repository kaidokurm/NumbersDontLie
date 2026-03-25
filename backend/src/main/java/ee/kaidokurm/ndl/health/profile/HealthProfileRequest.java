package ee.kaidokurm.ndl.health.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * Request/Response DTO for health profile data. Contains all user health
 * information: demographics, fitness, dietary.
 */
public class HealthProfileRequest {

    // Demographics
    @JsonProperty("birth_year")
    public Integer birthYear;

    public String gender;

    @Min(value = 50, message = "heightCm must be >= 50")
    @JsonProperty("height_cm")
    public int heightCm;

    @JsonProperty("height_unit")
    public String heightUnit;

    // Activity
    @NotBlank(message = "baselineActivityLevel is required")
    @Pattern(regexp = "^(sedentary|light|moderate|active|very_active)$", message = "baselineActivityLevel must be one of: sedentary, light, moderate, active, very_active")
    @JsonProperty("baseline_activity_level")
    public String baselineActivityLevel;

    // Dietary
    @JsonProperty("dietary_preferences")
    public List<String> dietaryPreferences;

    @JsonProperty("dietary_restrictions")
    public List<String> dietaryRestrictions;

    // Fitness Assessment
    @JsonProperty("fitness_assessment")
    public Map<String, Object> fitnessAssessment;

    @JsonProperty("fitness_assessment_completed")
    public Boolean fitnessAssessmentCompleted;

    public HealthProfileRequest() {
    }

    // Getters
    public Integer getBirthYear() {
        return birthYear;
    }

    public String getGender() {
        return gender;
    }

    public int getHeightCm() {
        return heightCm;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    public String getBaselineActivityLevel() {
        return baselineActivityLevel;
    }

    public List<String> getDietaryPreferences() {
        return dietaryPreferences;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public Map<String, Object> getFitnessAssessment() {
        return fitnessAssessment;
    }

    public Boolean getFitnessAssessmentCompleted() {
        return fitnessAssessmentCompleted;
    }
}
