package ee.kaidokurm.ndl.common.api.mapper;

import ee.kaidokurm.ndl.common.api.dto.HealthProfileResponse;
import ee.kaidokurm.ndl.common.api.dto.WeightEntryResponse;
import ee.kaidokurm.ndl.common.api.dto.GoalResponse;
import ee.kaidokurm.ndl.common.api.dto.ActivityCheckinResponse;
import ee.kaidokurm.ndl.health.profile.HealthProfileEntity;
import ee.kaidokurm.ndl.health.weight.WeightEntryEntity;
import ee.kaidokurm.ndl.health.goal.GoalEntity;
import ee.kaidokurm.ndl.health.activity.ActivityCheckinEntity;

/**
 * Mapper utility for converting JPA entities to Response DTOs. Provides
 * consistent DTO creation across the application.
 */
public class ResponseMapper {

    /**
     * Converts WeightEntryEntity to WeightEntryResponse DTO.
     * 
     * @param entity the weight entry entity
     * @return the response DTO
     */
    public static WeightEntryResponse toWeightEntryResponse(WeightEntryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new WeightEntryResponse(entity.getId(), entity.getWeightKg(), entity.getMeasuredAt(), entity.getNote());
    }

    /**
     * Converts HealthProfileEntity to HealthProfileResponse DTO.
     * 
     * @param entity the health profile entity
     * @return the response DTO
     */
    public static HealthProfileResponse toHealthProfileResponse(HealthProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        return new HealthProfileResponse(entity.getUserId(), entity.getBirthYear(), entity.getGender(),
                entity.getHeightCm(), entity.getBaselineActivityLevel(), entity.getDietaryPreferences(),
                entity.getDietaryRestrictions(), entity.getFitnessAssessment(), entity.getFitnessAssessmentCompleted(),
                entity.getWellnessScore(), entity.getBmiValue(), entity.getBmiClassification(), entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    /**
     * Converts GoalEntity to GoalResponse DTO.
     * 
     * @param entity the goal entity
     * @return the response DTO
     */
    public static GoalResponse toGoalResponse(GoalEntity entity) {
        if (entity == null) {
            return null;
        }
        return new GoalResponse(entity.getId(), entity.getUserId(),
                entity.getGoalType() != null ? entity.getGoalType().toString() : null, entity.getTargetWeightKg(),
                entity.getTargetActivityDaysPerWeek(), entity.getTargetDate(), entity.getNotes(), entity.isActive(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static ActivityCheckinResponse toActivityCheckinResponse(ActivityCheckinEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ActivityCheckinResponse(entity.getId(), entity.getActivityType(), entity.getDurationMinutes(),
                entity.getIntensity(), entity.getNote(), entity.getCheckinAt());
    }
}
