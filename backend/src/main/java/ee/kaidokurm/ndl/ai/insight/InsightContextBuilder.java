package ee.kaidokurm.ndl.ai.insight;

import ee.kaidokurm.ndl.health.goal.GoalEntity;
import ee.kaidokurm.ndl.health.goal.GoalProgressEntity;
import ee.kaidokurm.ndl.health.goal.GoalProgressRepository;
import ee.kaidokurm.ndl.health.goal.GoalRepository;
import ee.kaidokurm.ndl.health.activity.ActivityCheckinRepository;
import ee.kaidokurm.ndl.health.profile.HealthProfileEntity;
import ee.kaidokurm.ndl.health.profile.HealthProfileService;
import ee.kaidokurm.ndl.health.summary.HealthSummaryService;
import ee.kaidokurm.ndl.health.weight.WeightEntryEntity;
import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Builder service for aggregating and formatting health data into structured
 * context for AI insight generation.
 * 
 * Collects: - User health profile (demographics, fitness, dietary) - Current
 * BMI and classification - Recent weight trends (7d, 30d, 90d) - Active goal
 * details and progress - Activity levels and compliance - Wellness metrics
 * 
 * Formats data as comprehensive prompt context for OpenAI, ensuring AI has
 * complete health picture for high-quality recommendations.
 */
@Service
public class InsightContextBuilder {

    private final HealthProfileService profileService;
    private final WeightEntryRepository weightRepo;
    private final GoalRepository goalRepo;
    private final GoalProgressRepository progressRepo;
    private final HealthSummaryService summaryService;
    private final ActivityCheckinRepository activityCheckinRepository;

    public InsightContextBuilder(HealthProfileService profileService, WeightEntryRepository weightRepo,
            GoalRepository goalRepo, GoalProgressRepository progressRepo, HealthSummaryService summaryService,
            ActivityCheckinRepository activityCheckinRepository) {
        this.profileService = profileService;
        this.weightRepo = weightRepo;
        this.goalRepo = goalRepo;
        this.progressRepo = progressRepo;
        this.summaryService = summaryService;
        this.activityCheckinRepository = activityCheckinRepository;
    }

    /**
     * Build comprehensive health context for a user.
     * 
     * Aggregates all health data including profile, weight trends, goals, and
     * progress. Suitable for AI analysis.
     * 
     * @param userId the user ID
     * @return comprehensive health context as LinkedHashMap (preserves order)
     * @throws IllegalStateException if required data is missing
     */
    public Map<String, Object> buildContext(UUID userId) {
        // Fetch all required data
        HealthProfileEntity profile = profileService.find(userId)
                .orElseThrow(() -> new IllegalStateException("Health profile required"));

        List<WeightEntryEntity> weights = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
        if (weights.isEmpty()) {
            throw new IllegalStateException("Weight data required");
        }

        List<GoalEntity> activeGoals = goalRepo.findByUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
        if (activeGoals.isEmpty()) {
            throw new IllegalStateException("Active goal required");
        }

        // Use LinkedHashMap to preserve field order for stable hashing
        Map<String, Object> context = new LinkedHashMap<>();

        // Demographics
        addDemographicsSection(context, profile);

        // Current metrics
        addCurrentMetricsSection(context, profile, weights);

        // Weight trends
        addWeightTrendsSection(context, weights);

        // Activity & compliance
        addActivitySection(context, profile);

        // Dietary info
        addDietarySection(context, profile);

        // Goal details
        addActiveGoalsSection(context, activeGoals);

        // Goal progress
        addGoalProgressSection(context, activeGoals);

        // Wellness summary
        addWellnessSection(context, profile);

        return context;
    }

    /**
     * Build user prompt for AI based on context.
     * 
     * Formats context into readable text suitable for system prompt inclusion.
     * 
     * @param context the context map from buildContext()
     * @return formatted user prompt string
     */
    public String buildUserPrompt(Map<String, Object> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HEALTH CONTEXT FOR WELLNESS COACHING ===\n\n");

        // Demographics
        Map<?, ?> demo = (Map<?, ?>) context.getOrDefault("demographics", new HashMap<>());
        if (!demo.isEmpty()) {
            sb.append("PERSONAL INFO:\n");
            demo.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Current metrics
        Map<?, ?> metrics = (Map<?, ?>) context.getOrDefault("current_metrics", new HashMap<>());
        if (!metrics.isEmpty()) {
            sb.append("CURRENT STATUS:\n");
            metrics.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Weight trends
        Map<?, ?> trends = (Map<?, ?>) context.getOrDefault("weight_trends", new HashMap<>());
        if (!trends.isEmpty()) {
            sb.append("WEIGHT TRENDS:\n");
            trends.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Activity
        Map<?, ?> activity = (Map<?, ?>) context.getOrDefault("activity", new HashMap<>());
        if (!activity.isEmpty()) {
            sb.append("ACTIVITY & COMPLIANCE:\n");
            activity.forEach((k, v) -> {
                if ("run_3km_time_sec".equals(k) && v instanceof Number secondsNumber) {
                    long totalSeconds = secondsNumber.longValue();
                    long minutes = totalSeconds / 60;
                    long seconds = totalSeconds % 60;
                    sb.append("  ").append(k).append(": ").append(minutes).append(":")
                            .append(String.format("%02d", seconds)).append(" (mm:ss)\n");
                } else {
                    sb.append("  ").append(k).append(": ").append(v).append("\n");
                }
            });
            sb.append("\n");
        }

        // Dietary
        Map<?, ?> dietary = (Map<?, ?>) context.getOrDefault("dietary", new HashMap<>());
        if (!dietary.isEmpty()) {
            sb.append("DIETARY PREFERENCES:\n");
            dietary.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }

        // Goals
        List<?> goals = (List<?>) context.getOrDefault("active_goals", List.of());
        if (!goals.isEmpty()) {
            sb.append("ACTIVE GOALS:\n");
            int i = 1;
            for (Object goalObj : goals) {
                sb.append("  Goal ").append(i++).append(":\n");
                if (goalObj instanceof Map<?, ?> goalMap) {
                    goalMap.forEach((k, v) -> sb.append("    ").append(k).append(": ").append(v).append("\n"));
                } else {
                    sb.append("    ").append(goalObj).append("\n");
                }
            }
            sb.append("\n");
        }

        // Progress
        List<?> progress = (List<?>) context.getOrDefault("active_goals_progress", List.of());
        if (!progress.isEmpty()) {
            sb.append("GOAL PROGRESS:\n");
            int i = 1;
            for (Object progressObj : progress) {
                sb.append("  Goal ").append(i++).append(" Progress:\n");
                if (progressObj instanceof Map<?, ?> progressMap) {
                    progressMap.forEach((k, v) -> sb.append("    ").append(k).append(": ").append(v).append("\n"));
                } else {
                    sb.append("    ").append(progressObj).append("\n");
                }
            }
            sb.append("\n");
        }

        // Wellness
        Map<?, ?> wellness = (Map<?, ?>) context.getOrDefault("wellness", new HashMap<>());
        if (!wellness.isEmpty()) {
            sb.append("WELLNESS SUMMARY:\n");
            wellness.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
        }

        sb.append("\n=== COACHING TASK ===\n");
        sb.append("Based on this comprehensive health context, provide:\n");
        sb.append("1. Exactly 3 actionable recommendations (movement, recovery, mindset)\n");
        sb.append("2. One reflective question for journaling\n");
        sb.append("3. A 2-3 sentence motivational summary\n");
        sb.append("Keep all text concise and supportive. Reference specific data points when relevant.");

        return sb.toString();
    }

    private void addDemographicsSection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> demo = new LinkedHashMap<>();
        if (profile.getBirthYear() != null) {
            demo.put("age_year_born", profile.getBirthYear());
        }
        if (profile.getGender() != null) {
            demo.put("gender", profile.getGender());
        }
        demo.put("height_cm", profile.getHeightCm());
        context.put("demographics", demo);
    }

    private void addCurrentMetricsSection(Map<String, Object> context, HealthProfileEntity profile,
            List<WeightEntryEntity> weights) {
        Map<String, Object> metrics = new LinkedHashMap<>();

        if (!weights.isEmpty()) {
            WeightEntryEntity latest = weights.get(0);
            metrics.put("current_weight_kg", latest.getWeightKg());
            metrics.put("measured_at", latest.getMeasuredAt());

            double bmi = summaryService.bmi(profile.getHeightCm(), latest.getWeightKg());
            metrics.put("current_bmi", Math.round(bmi * 10.0) / 10.0);
        }

        if (profile.getBmiClassification() != null) {
            metrics.put("bmi_classification", profile.getBmiClassification());
        }

        if (profile.getWellnessScore() != null) {
            metrics.put("wellness_score", profile.getWellnessScore());
        }

        context.put("current_metrics", metrics);
    }

    private void addWeightTrendsSection(Map<String, Object> context, List<WeightEntryEntity> weights) {
        Map<String, Object> trends = new LinkedHashMap<>();

        if (weights.size() >= 1) {
            Double delta7d = summaryService.weightDelta7d(weights);
            if (delta7d != null) {
                trends.put("weight_change_7_days_kg", Math.round(delta7d * 100.0) / 100.0);
            }
        }

        trends.put("total_entries", weights.size());
        context.put("weight_trends", trends);
    }

    private void addActivitySection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> activity = new LinkedHashMap<>();

        if (profile.getBaselineActivityLevel() != null) {
            activity.put("baseline_activity_level", profile.getBaselineActivityLevel());
        }

        // Fitness assessment data
        if (profile.getFitnessAssessment() != null && !profile.getFitnessAssessment().isEmpty()) {
            Map<String, Object> fitness = profile.getFitnessAssessment();
            putIfPresent(fitness, activity, "occupation_type");
            putIfPresent(fitness, activity, "current_activity_frequency");
            putIfPresent(fitness, activity, "exercise_types");
            putIfPresent(fitness, activity, "average_session_duration");
            putIfPresent(fitness, activity, "self_assessed_fitness_level");
            putIfPresent(fitness, activity, "preferred_exercise_environment");
            putIfPresent(fitness, activity, "exercise_time_preference");
            putIfPresent(fitness, activity, "current_endurance_minutes");
            putIfPresent(fitness, activity, "pushups_count");
            putIfPresent(fitness, activity, "situps_count");
            putIfPresent(fitness, activity, "pullups_count");
            putIfPresent(fitness, activity, "run_3km_time_sec");
        }

        if (profile.getFitnessAssessmentCompleted() != null) {
            activity.put("fitness_assessment_completed", profile.getFitnessAssessmentCompleted());
        }

        var recentActivity = activityCheckinRepository.findTop100ByUserIdOrderByCheckinAtDesc(profile.getUserId());
        long activeDaysLast7 = recentActivity.stream()
                .filter(a -> a.getCheckinAt() != null && !a.getCheckinAt().isBefore(OffsetDateTime.now().minusDays(7)))
                .map(a -> a.getCheckinAt().toLocalDate())
                .distinct()
                .count();
        activity.put("active_days_last_7d", activeDaysLast7);

        var timeline = recentActivity.stream().limit(10).map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("checkin_at", a.getCheckinAt());
            item.put("activity_type", a.getActivityType());
            if (a.getDurationMinutes() != null) {
                item.put("duration_minutes", a.getDurationMinutes());
            }
            if (a.getIntensity() != null) {
                item.put("intensity", a.getIntensity());
            }
            return item;
        }).toList();
        if (!timeline.isEmpty()) {
            activity.put("recent_activity_timeline", timeline);
        }

        context.put("activity", activity);
    }

    private void putIfPresent(Map<String, Object> source, Map<String, Object> target, String key) {
        Object value = source.get(key);
        if (value != null) {
            target.put(key, value);
        }
    }

    private void addDietarySection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> dietary = new LinkedHashMap<>();

        if (profile.getDietaryPreferences() != null && !profile.getDietaryPreferences().isEmpty()) {
            dietary.put("preferences", profile.getDietaryPreferences());
        }

        if (profile.getDietaryRestrictions() != null && !profile.getDietaryRestrictions().isEmpty()) {
            dietary.put("restrictions", profile.getDietaryRestrictions());
        }

        context.put("dietary", dietary);
    }

    private void addActiveGoalsSection(Map<String, Object> context, List<GoalEntity> goals) {
        List<Map<String, Object>> goalItems = goals.stream().map(goal -> {
            Map<String, Object> goalMap = new LinkedHashMap<>();
            goalMap.put("goal_id", goal.getId().toString());
            if (goal.getGoalType() != null) {
                goalMap.put("goal_type", goal.getGoalType().name());
            }
            if (goal.getNotes() != null) {
                goalMap.put("notes", goal.getNotes());
            }
            if (goal.getTargetWeightKg() != null) {
                goalMap.put("target_weight_kg", goal.getTargetWeightKg());
            }
            if (goal.getTargetActivityDaysPerWeek() != null) {
                goalMap.put("target_activity_days_per_week", goal.getTargetActivityDaysPerWeek());
            }
            if (goal.getCreatedAt() != null) {
                goalMap.put("goal_started", goal.getCreatedAt());
            }
            if (goal.getTargetDate() != null) {
                goalMap.put("target_date", goal.getTargetDate());
            }
            return goalMap;
        }).toList();
        context.put("active_goals", goalItems);
    }

    private void addGoalProgressSection(Map<String, Object> context, List<GoalEntity> goals) {
        List<Map<String, Object>> progressItems = goals.stream().map(goal -> {
            Map<String, Object> progress = new LinkedHashMap<>();
            progress.put("goal_id", goal.getId().toString());
            Optional<GoalProgressEntity> latestOpt = progressRepo.findFirstByGoalIdOrderByRecordedAtDesc(goal.getId());

            if (latestOpt.isPresent()) {
                GoalProgressEntity latest = latestOpt.get();
                progress.put("progress_percentage", latest.getProgressPercentage());
                progress.put("current_value", latest.getCurrentValue());
                progress.put("is_on_track", latest.getIsOnTrack());
                progress.put("days_remaining", latest.getDaysRemaining());
                progress.put("milestones_completed", latest.getMilestonesCompleted());
                progress.put("last_recorded_at", latest.getRecordedAt());
            }

            List<GoalProgressEntity> history = progressRepo.findTop30ByGoalIdOrderByRecordedAtDesc(goal.getId());
            progress.put("total_progress_records", history.size());
            return progress;
        }).toList();

        context.put("active_goals_progress", progressItems);
    }

    private void addWellnessSection(Map<String, Object> context, HealthProfileEntity profile) {
        Map<String, Object> wellness = new LinkedHashMap<>();

        if (profile.getWellnessScore() != null) {
            wellness.put("overall_wellness_score", profile.getWellnessScore());
        }

        // Add description based on score
        if (profile.getWellnessScore() != null) {
            int score = profile.getWellnessScore();
            String description;
            if (score >= 90) {
                description = "Excellent";
            } else if (score >= 75) {
                description = "Very Good";
            } else if (score >= 60) {
                description = "Good";
            } else if (score >= 45) {
                description = "Fair";
            } else {
                description = "Needs Improvement";
            }
            wellness.put("wellness_level", description);
        }

        wellness.put("last_updated", OffsetDateTime.now());
        context.put("wellness", wellness);
    }
}
