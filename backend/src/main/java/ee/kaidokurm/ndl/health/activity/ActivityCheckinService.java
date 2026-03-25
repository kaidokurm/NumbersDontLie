package ee.kaidokurm.ndl.health.activity;

import ee.kaidokurm.ndl.health.goal.GoalProgressService;
import ee.kaidokurm.ndl.health.wellness.WellnessScoreService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityCheckinService {

    private static final int MAX_COLLISION_RETRIES = 120;

    private final ActivityCheckinRepository repository;
    private final WellnessScoreService wellnessScoreService;
    private final GoalProgressService goalProgressService;

    public ActivityCheckinService(ActivityCheckinRepository repository, WellnessScoreService wellnessScoreService,
            GoalProgressService goalProgressService) {
        this.repository = repository;
        this.wellnessScoreService = wellnessScoreService;
        this.goalProgressService = goalProgressService;
    }

    @Transactional
    public ActivityCheckinEntity add(UUID userId, String activityType, Integer durationMinutes, String intensity,
            String note, OffsetDateTime checkinAt) {
        ActivityType normalizedType = ActivityType.fromInput(activityType);
        ActivityIntensity normalizedIntensity = ActivityIntensity.fromInput(intensity);
        OffsetDateTime normalized = resolveUniqueTimestamp(userId,
                checkinAt != null ? checkinAt : OffsetDateTime.now());

        ActivityCheckinEntity entity = new ActivityCheckinEntity(
                UUID.randomUUID(), userId, normalizedType.name().toLowerCase(), durationMinutes,
                normalizedIntensity.name().toLowerCase(), note, normalized,
                OffsetDateTime.now(), OffsetDateTime.now());

        ActivityCheckinEntity saved = repository.save(entity);
        refreshDerivedMetrics(userId);
        return saved;
    }

    @Transactional
    public ActivityCheckinEntity update(UUID userId, ActivityCheckinEntity entity) {
        entity.setActivityType(ActivityType.fromInput(entity.getActivityType()).name().toLowerCase());
        entity.setIntensity(ActivityIntensity.fromInput(entity.getIntensity()).name().toLowerCase());
        OffsetDateTime normalized = resolveUniqueTimestampForUpdate(userId, entity.getCheckinAt(), entity.getId());
        entity.setCheckinAt(normalized);
        entity.setUpdatedAt(OffsetDateTime.now());

        ActivityCheckinEntity saved = repository.save(entity);
        refreshDerivedMetrics(userId);
        return saved;
    }

    @Transactional
    public void delete(UUID userId, ActivityCheckinEntity entity) {
        repository.delete(entity);
        refreshDerivedMetrics(userId);
    }

    public List<ActivityCheckinEntity> latest(UUID userId) {
        return repository.findTop100ByUserIdOrderByCheckinAtDesc(userId);
    }

    public ActivityCheckinEntity latestSingle(UUID userId) {
        return repository.findTop100ByUserIdOrderByCheckinAtDesc(userId).stream().findFirst().orElse(null);
    }

    public Page<ActivityCheckinEntity> getHistory(UUID userId, Pageable pageable) {
        return repository.findByUserIdOrderByCheckinAtDesc(userId, pageable);
    }

    public long countActiveDaysLast7(UUID userId) {
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        return repository.findByUserIdAndCheckinAtAfterOrderByCheckinAtDesc(userId, since).stream()
                .map(ActivityCheckinEntity::getCheckinAt)
                .map(OffsetDateTime::toLocalDate)
                .distinct()
                .count();
    }

    private void refreshDerivedMetrics(UUID userId) {
        wellnessScoreService.calculateAndUpdateWellnessScore(userId);
        goalProgressService.recordCurrentActivityProgressForUser(userId,
                BigDecimal.valueOf(countActiveDaysLast7(userId)));
    }

    private OffsetDateTime resolveUniqueTimestamp(UUID userId, OffsetDateTime base) {
        OffsetDateTime candidate = base;
        for (int i = 0; i < MAX_COLLISION_RETRIES; i++) {
            if (!repository.existsByUserIdAndCheckinAt(userId, candidate)) {
                return candidate;
            }
            candidate = candidate.plusSeconds(1);
        }
        throw new IllegalArgumentException("Too many activity entries for the same timestamp");
    }

    private OffsetDateTime resolveUniqueTimestampForUpdate(UUID userId, OffsetDateTime base, UUID id) {
        OffsetDateTime candidate = base;
        for (int i = 0; i < MAX_COLLISION_RETRIES; i++) {
            if (!repository.existsByUserIdAndCheckinAtAndIdNot(userId, candidate, id)) {
                return candidate;
            }
            candidate = candidate.plusSeconds(1);
        }
        throw new IllegalArgumentException("Too many activity entries for the same timestamp");
    }
}
