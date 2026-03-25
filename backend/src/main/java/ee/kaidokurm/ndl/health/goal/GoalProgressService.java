package ee.kaidokurm.ndl.health.goal;

import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ee.kaidokurm.ndl.health.wellness.WellnessScoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for calculating and tracking progress towards health goals.
 * 
 * Handles: - Progress calculation based on goal type (weight loss, activity,
 * etc.) - On-track determination (pace vs. deadline) - Milestone tracking
 * (every 5% progress) - Historical record keeping for trend analysis
 */
@Service
public class GoalProgressService {

    private final GoalProgressRepository progressRepository;
    private final GoalRepository goalRepository;
    private final WellnessScoreService wellnessScoreService;
    private final WeightEntryRepository weightEntryRepository;

    // Milestone interval: track every 5% of progress
    private static final int MILESTONE_INTERVAL = 5;
    private static final int DEFAULT_GOAL_DURATION_DAYS = 90;

    public GoalProgressService(GoalProgressRepository progressRepository, GoalRepository goalRepository,
            WellnessScoreService wellnessScoreService, WeightEntryRepository weightEntryRepository) {
        this.progressRepository = progressRepository;
        this.goalRepository = goalRepository;
        this.wellnessScoreService = wellnessScoreService;
        this.weightEntryRepository = weightEntryRepository;
    }

    /**
     * Calculate and record progress for a specific goal.
     * 
     * This method: 1. Retrieves the goal 2. Calculates current progress based on
     * goal type 3. Determines on-track status 4. Checks for milestone completions
     * 5. Records snapshot in database
     * 
     * @param goalId       the goal to calculate progress for
     * @param currentValue the current metric value (e.g., weight in kg, activity
     *                     days)
     * @return the recorded GoalProgressEntity, or null if goal not found
     */
    @Transactional
    public GoalProgressEntity recordProgress(UUID goalId, BigDecimal currentValue) {
        Optional<GoalEntity> goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isEmpty()) {
            return null;
        }

        GoalEntity goal = goalOpt.get();

        // Calculate progress percentage based on goal type
        int progressPercentage = calculateProgressPercentage(goal, currentValue);

        // Determine if on-track
        boolean isOnTrack = determineOnTrackStatus(goal, progressPercentage);

        // Calculate days remaining
        Integer daysRemaining = calculateDaysRemaining(goal);

        // Check for milestone completions
        List<Map<String, Object>> milestoneDetails = checkMilestones(goalId, progressPercentage);
        int milestonesCompleted = milestoneDetails.size();

        // Create progress record
        var progress = new GoalProgressEntity(UUID.randomUUID(), goalId, goal.getUserId(), currentValue,
                progressPercentage, isOnTrack, daysRemaining, OffsetDateTime.now(), OffsetDateTime.now(),
                OffsetDateTime.now());

        progress.setMilestoneDetails(milestoneDetails);
        progress.setMilestonesCompleted(milestonesCompleted);

        GoalProgressEntity saved = progressRepository.save(progress);
        wellnessScoreService.calculateAndUpdateWellnessScore(goal.getUserId());
        return saved;
    }

    /**
     * Calculate progress percentage based on goal type and current value.
     * 
     * For weight-related goals: Use target weight to calculate how close we are
     * 
     * For activity goals: progress = (current_days / target_days) * 100
     * 
     * For other goals: Return 0 (not yet implemented)
     * 
     * @param goal         the goal entity
     * @param currentValue the current metric value
     * @return progress percentage 0-100 (capped at 100 for completion)
     */
    private int calculateProgressPercentage(GoalEntity goal, BigDecimal currentValue) {
        if (currentValue == null) {
            return 0;
        }

        if (goal.getGoalType() == GoalType.WEIGHT_LOSS || goal.getGoalType() == GoalType.WEIGHT_GAIN
                || goal.getGoalType() == GoalType.MAINTAIN_WEIGHT) {
            if (goal.getTargetWeightKg() == null) {
                return 0;
            }
            return calculateWeightGoalProgress(goal, currentValue.doubleValue());
        }

        if (goal.getGoalType() == GoalType.IMPROVE_FITNESS || goal.getGoalType() == GoalType.ENHANCE_ENDURANCE
                || goal.getGoalType() == GoalType.BUILD_MUSCLE || goal.getGoalType() == GoalType.IMPROVE_FLEXIBILITY) {
            if (goal.getTargetActivityDaysPerWeek() == null || goal.getTargetActivityDaysPerWeek() == 0) {
                return 0;
            }

            int targetDays = goal.getTargetActivityDaysPerWeek();
            int currentDays = Math.max(0, currentValue.intValue());
            int progress = Math.round((currentDays * 100.0f) / targetDays);
            return clamp(progress);
        }

        return 0;
    }

    /**
     * Determine if user is on-track to meet goal by target date.
     * 
     * On-track if: - (days_elapsed / total_days) >= (progress / 100)
     * 
     * Example: 50% time passed should have 50% progress
     * 
     * @param goal               the goal
     * @param progressPercentage current progress 0-100
     * @return true if on-track, false if behind
     */
    private boolean determineOnTrackStatus(GoalEntity goal, int progressPercentage) {
        OffsetDateTime targetDateTime = getTargetDateTime(goal);
        OffsetDateTime start = goal.getCreatedAt();
        if (targetDateTime == null || start == null) {
            return progressPercentage > 0;
        }

        if (OffsetDateTime.now().isAfter(targetDateTime)) {
            return progressPercentage >= 100;
        }

        long totalSeconds = Math.max(1, ChronoUnit.SECONDS.between(start, targetDateTime));
        long elapsedSeconds = Math.max(0, ChronoUnit.SECONDS.between(start, OffsetDateTime.now()));
        elapsedSeconds = Math.min(elapsedSeconds, totalSeconds);

        double expectedProgress = (elapsedSeconds * 100.0) / totalSeconds;
        return progressPercentage + 5 >= Math.round(expectedProgress);
    }

    /**
     * Calculate days remaining until goal target date.
     * 
     * @param goal the goal
     * @return days remaining, or null if no target date
     */
    private Integer calculateDaysRemaining(GoalEntity goal) {
        OffsetDateTime targetDateTime = getTargetDateTime(goal);
        if (targetDateTime == null) {
            return null;
        }
        long remaining = ChronoUnit.DAYS.between(OffsetDateTime.now(), targetDateTime);
        return (int) Math.max(remaining, 0);
    }

    private int calculateWeightGoalProgress(GoalEntity goal, double currentWeight) {
        double targetWeight = goal.getTargetWeightKg();
        double baselineWeight = resolveWeightBaseline(goal).orElse(currentWeight);

        if (goal.getGoalType() == GoalType.MAINTAIN_WEIGHT) {
            double diff = Math.abs(currentWeight - targetWeight);
            int score = (int) Math.round(100 - (diff * 20.0));
            return clamp(score);
        }

        double totalDelta = targetWeight - baselineWeight;
        if (Math.abs(totalDelta) < 0.0001) {
            return Math.abs(currentWeight - targetWeight) < 0.0001 ? 100 : 0;
        }

        double achievedDelta = currentWeight - baselineWeight;
        double ratio = achievedDelta / totalDelta;
        int score = (int) Math.round(ratio * 100.0);
        return clamp(score);
    }

    private Optional<Double> resolveWeightBaseline(GoalEntity goal) {
        Optional<Double> firstProgressBaseline = progressRepository.findFirstByGoalIdOrderByRecordedAtAsc(goal.getId())
                .map(GoalProgressEntity::getCurrentValue)
                .map(BigDecimal::doubleValue);
        if (firstProgressBaseline.isPresent()) {
            return firstProgressBaseline;
        }

        return weightEntryRepository.findTop30ByUserIdOrderByMeasuredAtDesc(goal.getUserId()).stream()
                .map(entry -> BigDecimal.valueOf(entry.getWeightKg()).setScale(2, RoundingMode.HALF_UP))
                .map(BigDecimal::doubleValue)
                .findFirst();
    }

    private OffsetDateTime getTargetDateTime(GoalEntity goal) {
        LocalDate targetDate = goal.getTargetDate();
        if (targetDate != null) {
            return targetDate.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset()).minusSeconds(1);
        }
        if (goal.getCreatedAt() != null) {
            return goal.getCreatedAt().plusDays(DEFAULT_GOAL_DURATION_DAYS);
        }
        return null;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    /**
     * Check if any milestones were completed with this progress update.
     * 
     * Milestones are tracked at 5% intervals (5%, 10%, 15%, etc.) Compares previous
     * highest milestone to current progress.
     * 
     * @param goalId          the goal ID
     * @param currentProgress current progress percentage
     * @return list of newly completed milestones as JSON objects
     */
    private List<Map<String, Object>> checkMilestones(UUID goalId, int currentProgress) {
        List<Map<String, Object>> newMilestones = new ArrayList<>();

        // Get previous progress
        Optional<GoalProgressEntity> previousOpt = progressRepository.findFirstByGoalIdOrderByRecordedAtDesc(goalId);
        int previousProgress = previousOpt.map(GoalProgressEntity::getProgressPercentage).orElse(0);

        // Check each milestone interval
        for (int milestone = MILESTONE_INTERVAL; milestone <= 100; milestone += MILESTONE_INTERVAL) {
            // If we crossed this milestone
            if (previousProgress < milestone && currentProgress >= milestone) {
                Map<String, Object> milestoneRecord = new HashMap<>();
                milestoneRecord.put("percentage", milestone);
                milestoneRecord.put("completed_at", OffsetDateTime.now().toString());
                newMilestones.add(milestoneRecord);
            }
        }

        return newMilestones;
    }

    /**
     * Get the most recent progress for a goal.
     * 
     * @param goalId the goal ID
     * @return optional containing latest progress, empty if none exists
     */
    public Optional<GoalProgressEntity> getLatestProgress(UUID goalId) {
        return progressRepository.findFirstByGoalIdOrderByRecordedAtDesc(goalId);
    }

    /**
     * Get progress history for a goal (last 30 records).
     * 
     * @param goalId the goal ID
     * @return list of progress records in reverse chronological order
     */
    public List<GoalProgressEntity> getProgressHistory(UUID goalId) {
        return progressRepository.findTop30ByGoalIdOrderByRecordedAtDesc(goalId);
    }

    /**
     * Get all progress for a user across all goals.
     * 
     * @param userId the user ID
     * @return list of all progress records ordered by most recent first
     */
    public List<GoalProgressEntity> getUserProgress(UUID userId) {
        return progressRepository.findByUserIdOrderByRecordedAtDesc(userId);
    }

    @Transactional
    public Optional<GoalProgressEntity> recordCurrentActivityProgressForUser(UUID userId, BigDecimal currentDays) {
        if (currentDays == null) {
            return Optional.empty();
        }
        var activeGoals = goalRepository.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
        if (activeGoals.isEmpty()) {
            return Optional.empty();
        }
        GoalProgressEntity firstSaved = null;
        for (var goal : activeGoals) {
            if (goal.getTargetActivityDaysPerWeek() == null || goal.getTargetActivityDaysPerWeek() <= 0) {
                continue;
            }
            var saved = recordProgress(goal.getId(), currentDays);
            if (firstSaved == null && saved != null) {
                firstSaved = saved;
            }
        }
        return Optional.ofNullable(firstSaved);
    }
}
