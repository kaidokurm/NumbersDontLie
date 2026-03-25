package ee.kaidokurm.ndl.health.profile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthProfileService {

    private final HealthProfileRepository repo;

    public HealthProfileService(HealthProfileRepository repo) {
        this.repo = repo;
    }

    public Optional<HealthProfileEntity> find(UUID userId) {
        return repo.findById(userId);
    }

    @Transactional
    public HealthProfileEntity upsert(UUID userId, Integer birthYear, String gender, int heightCm,
            String baselineActivityLevel) {
        return upsert(userId, birthYear, gender, heightCm, baselineActivityLevel, null, null, null, null);
    }

    @Transactional
    public HealthProfileEntity upsert(UUID userId, Integer birthYear, String gender, int heightCm,
            String baselineActivityLevel, List<String> dietaryPreferences, List<String> dietaryRestrictions,
            Map<String, Object> fitnessAssessment, Boolean fitnessAssessmentCompleted) {

        OffsetDateTime now = OffsetDateTime.now();
        HealthProfileEntity existing = repo.findById(userId).orElse(null);

        HealthProfileEntity entity;
        if (existing == null) {
            entity = new HealthProfileEntity(userId, birthYear, gender, heightCm, baselineActivityLevel, now, now);
        } else {
            entity = existing;
            entity.setBirthYear(birthYear);
            entity.setGender(gender);
            entity.setHeightCm(heightCm);
            entity.setBaselineActivityLevel(baselineActivityLevel);
            entity.setUpdatedAt(now);
        }

        // Set optional dietary and fitness fields
        if (dietaryPreferences != null) {
            entity.setDietaryPreferences(dietaryPreferences);
        }
        if (dietaryRestrictions != null) {
            entity.setDietaryRestrictions(dietaryRestrictions);
        }
        if (fitnessAssessment != null) {
            entity.setFitnessAssessment(fitnessAssessment);
        }
        if (fitnessAssessmentCompleted != null) {
            entity.setFitnessAssessmentCompleted(fitnessAssessmentCompleted);
        }

        // Save the profile
        var saved = repo.save(entity);

        // Note: BMI is calculated when weight is added via WeightService.
        // If you want to trigger BMI calculation here with a weight value,
        // pass it as a parameter or fetch latest weight from WeightService

        return saved;
    }
}
