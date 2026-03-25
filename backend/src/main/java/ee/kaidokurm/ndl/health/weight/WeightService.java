package ee.kaidokurm.ndl.health.weight;

import ee.kaidokurm.ndl.health.profile.HealthProfileRepository;
import ee.kaidokurm.ndl.health.wellness.WellnessScoreService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeightService {
    private static final int MAX_COLLISION_RETRIES = 120;

    private final WeightEntryRepository repo;
    private final HealthProfileRepository profileRepo;
    private final WellnessScoreService wellnessScoreService;

    public WeightService(WeightEntryRepository repo, HealthProfileRepository profileRepo,
            WellnessScoreService wellnessScoreService) {
        this.repo = repo;
        this.profileRepo = profileRepo;
        this.wellnessScoreService = wellnessScoreService;
    }

    @Transactional
    public WeightEntryEntity add(UUID userId, double weightKg, OffsetDateTime measuredAt, String note) {
        OffsetDateTime effectiveMeasuredAt = resolveUniqueTimestamp(userId,
                measuredAt != null ? measuredAt : OffsetDateTime.now());
        // Save weight entry
        var entry = repo.save(new WeightEntryEntity(UUID.randomUUID(), userId,
                effectiveMeasuredAt, weightKg, note));

        // Update BMI in health profile if it exists
        // Just a note on using var vs explicit types:
        // I prefer explicit types for method signatures and
        // public APIs for clarity, but use var for local variables
        // to reduce boilerplate and improve readability.
        // Note that we are checking for presence before calling get().
        var profile = profileRepo.findById(userId);
        if (profile.isPresent()) {
            var healthProfile = profile.get();
            healthProfile.calculateBMI(weightKg);
            healthProfile.setUpdatedAt(OffsetDateTime.now());
            profileRepo.save(healthProfile);

            // Auto-calculate wellness score after BMI update
            wellnessScoreService.calculateAndUpdateWellnessScore(userId);
        }

        return entry;
    }

    public List<WeightEntryEntity> latest(UUID userId) {
        return repo.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
    }

    /**
     * Get paginated weight history for a user.
     * 
     * @param userId   the user ID
     * @param pageable pagination settings
     * @return paginated weight entries
     */
    public Page<WeightEntryEntity> getHistory(UUID userId, Pageable pageable) {
        return repo.findByUserIdOrderByMeasuredAtDesc(userId, pageable);
    }

    /**
     * Update an existing weight entry and recalculate BMI if needed.
     * 
     * @param userId the user ID (for ownership verification)
     * @param entry  the weight entry to update
     * @return the updated entry
     */
    @Transactional
    public WeightEntryEntity update(UUID userId, WeightEntryEntity entry) {
        entry.setMeasuredAt(resolveUniqueTimestampForUpdate(userId, entry.getMeasuredAt(), entry.getId()));
        var saved = repo.save(entry);

        // Recalculate BMI in health profile if it exists
        var profile = profileRepo.findByUserId(userId);
        if (profile.isPresent()) {
            var healthProfile = profile.get();
            healthProfile.calculateBMI(saved.getWeightKg());
            healthProfile.setUpdatedAt(OffsetDateTime.now());
            profileRepo.save(healthProfile);

            // Auto-calculate wellness score after BMI update
            wellnessScoreService.calculateAndUpdateWellnessScore(userId);
        }

        return saved;
    }

    private OffsetDateTime resolveUniqueTimestamp(UUID userId, OffsetDateTime base) {
        OffsetDateTime candidate = base;
        for (int i = 0; i < MAX_COLLISION_RETRIES; i++) {
            if (!repo.existsByUserIdAndMeasuredAt(userId, candidate)) {
                return candidate;
            }
            candidate = candidate.plusSeconds(1);
        }
        throw new IllegalArgumentException("Too many entries for the same timestamp");
    }

    private OffsetDateTime resolveUniqueTimestampForUpdate(UUID userId, OffsetDateTime base, UUID id) {
        OffsetDateTime candidate = base;
        for (int i = 0; i < MAX_COLLISION_RETRIES; i++) {
            if (!repo.existsByUserIdAndMeasuredAtAndIdNot(userId, candidate, id)) {
                return candidate;
            }
            candidate = candidate.plusSeconds(1);
        }
        throw new IllegalArgumentException("Too many entries for the same timestamp");
    }
}
