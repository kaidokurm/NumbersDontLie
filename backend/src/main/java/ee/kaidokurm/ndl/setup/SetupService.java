package ee.kaidokurm.ndl.setup;

import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.health.goal.GoalRepository;
import ee.kaidokurm.ndl.health.profile.HealthProfileRepository;
import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;
import ee.kaidokurm.ndl.privacy.PrivacyPreferencesService;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service that determines what parts of onboarding are complete.
 *
 * This is the "source of truth" for setup requirements. Add new requirements
 * here and they automatically propagate to the API.
 */
@Service
public class SetupService {

    private final HealthProfileRepository profileRepo;
    private final GoalRepository goalRepo;
    private final WeightEntryRepository weightRepo;
    private final PrivacyPreferencesService privacyPreferencesService;

    public SetupService(HealthProfileRepository profileRepo, GoalRepository goalRepo,
            WeightEntryRepository weightRepo, PrivacyPreferencesService privacyPreferencesService) {
        this.profileRepo = profileRepo;
        this.goalRepo = goalRepo;
        this.weightRepo = weightRepo;
        this.privacyPreferencesService = privacyPreferencesService;
    }

    /**
     * Check if a user has completed all setup requirements.
     *
     * Requirements: 1. Health profile (height, gender, etc.) 2. Active goal (weight
     * target) 3. At least one weight entry (baseline measurement)
     */
    public SetupStatusDto getSetupStatus(UserEntity user) {
        List<String> missing = new ArrayList<>();

        // Check profile exists
        if (profileRepo.findByUserId(user.getId()).isEmpty()) {
            missing.add("profile");
        }

        // Check active goal exists
        if (goalRepo.findActiveByUserId(user.getId()).isEmpty()) {
            missing.add("goal");
        }

        // Check at least one weight entry exists
        if (!weightRepo.existsByUserId(user.getId())) {
            missing.add("weight");
        }

        // Data usage consent is required before health-data processing features.
        if (!privacyPreferencesService.hasDataUsageConsent(user.getId())) {
            missing.add("consent");
        }

        boolean isComplete = missing.isEmpty();
        return new SetupStatusDto(isComplete, missing);
    }
}
