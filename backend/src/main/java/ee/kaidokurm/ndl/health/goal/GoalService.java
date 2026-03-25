package ee.kaidokurm.ndl.health.goal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {

    private final GoalRepository repo;

    public GoalService(GoalRepository repo) {
        this.repo = repo;
    }

    public GoalEntity getActive(UUID userId) {
        return repo.findFirstByUserIdAndActiveTrue(userId).orElse(null);
    }

    public java.util.List<GoalEntity> getActiveGoals(UUID userId) {
        return repo.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public GoalEntity createAndActivate(
            UUID userId,
            GoalType goalType,
            Double targetWeightKg,
            Integer targetActivityDaysPerWeek,
            LocalDate targetDate,
            String notes) {
        OffsetDateTime now = OffsetDateTime.now();
        GoalEntity created = new GoalEntity(
                UUID.randomUUID(),
                userId,
                goalType,
                targetWeightKg,
                targetActivityDaysPerWeek,
                targetDate != null ? targetDate : now.toLocalDate().plusDays(90),
                notes,
                true,
                now,
                now);

        return repo.save(created);
    }

    @Transactional
    public GoalEntity update(UUID userId, UUID goalId, GoalType goalType, Double targetWeightKg,
            Integer targetActivityDaysPerWeek, LocalDate targetDate, String notes, Boolean isActive) {

        GoalEntity existing = repo.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Goal not found");
        }

        OffsetDateTime now = OffsetDateTime.now();

        GoalEntity updated = new GoalEntity(
                existing.getId(),
                existing.getUserId(),
                goalType != null ? goalType : existing.getGoalType(),
                targetWeightKg != null ? targetWeightKg : existing.getTargetWeightKg(),
                targetActivityDaysPerWeek != null ? targetActivityDaysPerWeek : existing.getTargetActivityDaysPerWeek(),
                targetDate != null ? targetDate : existing.getTargetDate(),
                notes != null ? notes : existing.getNotes(),
                isActive != null ? isActive : existing.isActive(),
                existing.getCreatedAt(),
                now);

        return repo.save(updated);
    }
}
