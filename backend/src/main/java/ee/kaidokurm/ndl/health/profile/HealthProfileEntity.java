package ee.kaidokurm.ndl.health.profile;

import ee.kaidokurm.ndl.common.persistence.encryption.EncryptedJsonMapConverter;
import ee.kaidokurm.ndl.common.persistence.encryption.EncryptedStringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "health_profiles")
public class HealthProfileEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "gender")
    private String gender;

    @Column(name = "height_cm", nullable = false)
    private int heightCm;

    @Column(name = "baseline_activity_level", nullable = false)
    private String baselineActivityLevel;

    @Column(name = "dietary_preferences", columnDefinition = "text")
    @Convert(converter = EncryptedStringListConverter.class)
    private List<String> dietaryPreferences;

    @Column(name = "dietary_restrictions", columnDefinition = "text")
    @Convert(converter = EncryptedStringListConverter.class)
    private List<String> dietaryRestrictions;

    @Column(name = "fitness_assessment", columnDefinition = "text")
    @Convert(converter = EncryptedJsonMapConverter.class)
    private Map<String, Object> fitnessAssessment;

    @Column(name = "fitness_assessment_completed")
    private Boolean fitnessAssessmentCompleted = false;

    @Column(name = "bmi_value")
    private BigDecimal bmiValue;

    @Column(name = "bmi_classification")
    private String bmiClassification;

    @Column(name = "wellness_score")
    private Integer wellnessScore = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected HealthProfileEntity() {
    }

    public HealthProfileEntity(UUID userId, Integer birthYear, String gender, int heightCm,
            String baselineActivityLevel, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.userId = userId;
        this.birthYear = birthYear;
        this.gender = gender;
        this.heightCm = heightCm;
        this.baselineActivityLevel = baselineActivityLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.fitnessAssessmentCompleted = false;
    }

    // Getters
    public UUID getUserId() {
        return userId;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public String getGender() {
        return gender;
    }

    public int getHeightCm() {
        return heightCm;
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

    public BigDecimal getBmiValue() {
        return bmiValue;
    }

    public String getBmiClassification() {
        return bmiClassification;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setHeightCm(int heightCm) {
        this.heightCm = heightCm;
    }

    public void setBaselineActivityLevel(String baselineActivityLevel) {
        this.baselineActivityLevel = baselineActivityLevel;
    }

    public void setDietaryPreferences(List<String> dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public void setFitnessAssessment(Map<String, Object> fitnessAssessment) {
        this.fitnessAssessment = fitnessAssessment;
    }

    public void setFitnessAssessmentCompleted(Boolean completed) {
        this.fitnessAssessmentCompleted = completed;
    }

    public void setBmiValue(BigDecimal bmiValue) {
        this.bmiValue = bmiValue;
    }

    public void setBmiClassification(String bmiClassification) {
        this.bmiClassification = bmiClassification;
    }

    public Integer getWellnessScore() {
        return wellnessScore;
    }

    public void setWellnessScore(Integer wellnessScore) {
        this.wellnessScore = wellnessScore;
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

    /**
     * Calculate and set BMI value and classification from weight and height. This
     * should be called whenever weight or height changes.
     */
    public void calculateBMI(double weightKg) {
        try {
            BigDecimal bmi = BMICalculator.calculateBMI(weightKg, this.heightCm);
            this.bmiValue = bmi;
            this.bmiClassification = BMICalculator.classifyBMI(bmi);
        } catch (IllegalArgumentException e) {
            // If calculation fails, clear BMI values
            this.bmiValue = null;
            this.bmiClassification = null;
        }
    }

    @Override
    public String toString() {
        return "HealthProfileEntity{" + "userId=" + userId + ", birthYear=" + birthYear + ", gender='" + gender + '\''
                + ", heightCm=" + heightCm + ", baselineActivityLevel='" + baselineActivityLevel + '\''
                + ", dietaryPreferences=" + dietaryPreferences + ", dietaryRestrictions=" + dietaryRestrictions
                + ", fitnessAssessmentCompleted=" + fitnessAssessmentCompleted + '}';
    }
}
