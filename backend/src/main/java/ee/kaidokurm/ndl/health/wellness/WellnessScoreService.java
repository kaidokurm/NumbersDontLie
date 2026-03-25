package ee.kaidokurm.ndl.health.wellness;

import ee.kaidokurm.ndl.health.activity.ActivityCheckinEntity;
import ee.kaidokurm.ndl.health.activity.ActivityCheckinRepository;
import ee.kaidokurm.ndl.health.goal.GoalProgressEntity;
import ee.kaidokurm.ndl.health.goal.GoalProgressRepository;
import ee.kaidokurm.ndl.health.goal.GoalRepository;
import ee.kaidokurm.ndl.health.profile.BMICalculator;
import ee.kaidokurm.ndl.health.profile.HealthProfileEntity;
import ee.kaidokurm.ndl.health.profile.HealthProfileRepository;
import ee.kaidokurm.ndl.health.weight.WeightEntryEntity;
import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WellnessScoreService {

    private final HealthProfileRepository profileRepository;
    private final GoalRepository goalRepository;
    private final GoalProgressRepository goalProgressRepository;
    private final WeightEntryRepository weightEntryRepository;
    private final ActivityCheckinRepository activityCheckinRepository;

    public WellnessScoreService(HealthProfileRepository profileRepository, GoalRepository goalRepository,
            GoalProgressRepository goalProgressRepository, WeightEntryRepository weightEntryRepository,
            ActivityCheckinRepository activityCheckinRepository) {
        this.profileRepository = profileRepository;
        this.goalRepository = goalRepository;
        this.goalProgressRepository = goalProgressRepository;
        this.weightEntryRepository = weightEntryRepository;
        this.activityCheckinRepository = activityCheckinRepository;
    }

    @Transactional
    public Integer calculateAndUpdateWellnessScore(UUID userId) {
        var profile = profileRepository.findById(userId).orElse(null);
        if (profile == null) {
            return null;
        }

        int bmiScore = calculateBmiComponentScore(profile);
        int activityScore = calculateActivityComponentScore(userId, profile);
        int goalScore = calculateGoalProgressComponentScore(userId);
        int habitsScore = calculateHabitsComponentScore(profile, userId);

        int overallScore = WellnessScoreCalculator.calculateOverallScore(bmiScore, activityScore, goalScore,
                habitsScore);

        profile.setWellnessScore(overallScore);
        profileRepository.save(profile);

        return overallScore;
    }

    private int calculateBmiComponentScore(HealthProfileEntity profile) {
        String bmiClassification = profile.getBmiClassification();
        if (bmiClassification == null || bmiClassification.isBlank()) {
            return 0;
        }
        return WellnessScoreCalculator.calculateBmiScore(bmiClassification);
    }

    private int calculateActivityComponentScore(UUID userId, HealthProfileEntity profile) {
        Integer activityDays = countRecentActivityDays(userId, OffsetDateTime.now().minusDays(7));
        if (activityDays != null) {
            return WellnessScoreCalculator.calculateActivityScore(activityDays);
        }

        Integer baseline = extractWeeklyActivityFrequency(profile);
        if (baseline != null) {
            return WellnessScoreCalculator.calculateActivityScore(baseline);
        }
        return 0;
    }

    private int calculateGoalProgressComponentScore(UUID userId) {
        var activeGoal = goalRepository.findFirstByUserIdAndActiveTrue(userId);
        if (activeGoal.isEmpty()) {
            return 50;
        }

        return goalProgressRepository.findFirstByGoalIdOrderByRecordedAtDesc(activeGoal.get().getId())
                .map(GoalProgressEntity::getProgressPercentage)
                .filter(Objects::nonNull)
                .map(value -> clampScore(value.intValue()))
                .orElse(0);
    }

    private int calculateHabitsComponentScore(HealthProfileEntity profile, UUID userId) {
        List<Integer> signals = new ArrayList<>();

        var recentWeightEntries = weightEntryRepository.findTop30ByUserIdOrderByMeasuredAtDesc(userId);
        if (!recentWeightEntries.isEmpty()) {
            OffsetDateTime since = OffsetDateTime.now().minusDays(7);
            long daysWithWeightEntries = recentWeightEntries.stream()
                    .filter(entry -> entry.getMeasuredAt() != null && !entry.getMeasuredAt().isBefore(since))
                    .map(entry -> entry.getMeasuredAt().toLocalDate())
                    .distinct()
                    .count();
            int checkinConsistencyScore = (int) Math.min(100, Math.round((daysWithWeightEntries / 7.0) * 100.0));
            signals.add(checkinConsistencyScore);
        }

        Integer activityDays = countRecentActivityDays(userId, OffsetDateTime.now().minusDays(7));
        if (activityDays != null) {
            signals.add((int) Math.min(100, Math.round((activityDays / 7.0) * 100.0)));
        }

        Integer activityFrequency = extractWeeklyActivityFrequency(profile);
        if (activityFrequency != null) {
            signals.add(WellnessScoreCalculator.calculateActivityScore(activityFrequency));
        }

        if (signals.isEmpty()) {
            return 50;
        }

        double avg = signals.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(50.0);
        return clampScore((int) Math.round(avg));
    }

    private Integer extractWeeklyActivityFrequency(HealthProfileEntity profile) {
        if (profile.getFitnessAssessment() == null) {
            return null;
        }
        Object frequencyObj = profile.getFitnessAssessment().get("current_activity_frequency");
        if (frequencyObj == null) {
            return null;
        }
        try {
            if (frequencyObj instanceof Number num) {
                return num.intValue();
            }
            if (frequencyObj instanceof String str) {
                return Integer.parseInt(str);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    public List<WellnessHistoryPointResponse> getWeeklyWellnessHistory(UUID userId, int weeks) {
        var profileOpt = profileRepository.findById(userId);
        if (profileOpt.isEmpty()) {
            return List.of();
        }
        HealthProfileEntity profile = profileOpt.get();

        List<WeightEntryEntity> weights = weightEntryRepository.findByUserIdOrderByMeasuredAtDesc(userId);
        if (weights.isEmpty()) {
            return List.of();
        }
        List<GoalProgressEntity> progressHistory = goalProgressRepository.findByUserIdOrderByRecordedAtDesc(userId);
        List<ActivityCheckinEntity> activityHistory = activityCheckinRepository
                .findByUserIdOrderByCheckinAtDesc(userId);

        List<WellnessHistoryPointResponse> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int safeWeeks = Math.max(4, Math.min(52, weeks));

        for (int i = safeWeeks - 1; i >= 0; i--) {
            LocalDate weekEnd = today.minusWeeks(i);
            LocalDate weekStart = weekEnd.minusDays(6);

            Double latestWeight = latestWeightUpTo(weights, weekEnd);
            if (latestWeight == null) {
                continue;
            }

            int bmiScore = calculateBmiScoreAtWeight(profile, latestWeight);
            int activityScore = calculateActivityScoreForWindow(profile, activityHistory, weekStart, weekEnd);
            int goalScore = calculateGoalProgressScoreAt(progressHistory, weekEnd);
            int habitsScore = calculateHabitsScoreForWindow(profile, weights, activityHistory, weekStart, weekEnd);

            int overallScore = WellnessScoreCalculator.calculateOverallScore(bmiScore, activityScore, goalScore,
                    habitsScore);
            points.add(new WellnessHistoryPointResponse(weekStart, weekEnd, overallScore, bmiScore, activityScore,
                    goalScore, habitsScore));
        }

        return points;
    }

    private Double latestWeightUpTo(List<WeightEntryEntity> weights, LocalDate cutoffDate) {
        return weights.stream()
                .filter(w -> w.getMeasuredAt() != null && !w.getMeasuredAt().toLocalDate().isAfter(cutoffDate))
                .max(Comparator.comparing(WeightEntryEntity::getMeasuredAt))
                .map(WeightEntryEntity::getWeightKg)
                .orElse(null);
    }

    private int calculateBmiScoreAtWeight(HealthProfileEntity profile, double weightKg) {
        if (profile.getHeightCm() <= 0) {
            return 0;
        }
        try {
            var bmi = BMICalculator.calculateBMI(weightKg, profile.getHeightCm());
            String classification = BMICalculator.classifyBMI(bmi);
            return WellnessScoreCalculator.calculateBmiScore(classification);
        } catch (Exception e) {
            return 0;
        }
    }

    private int calculateGoalProgressScoreAt(List<GoalProgressEntity> progressHistory, LocalDate weekEnd) {
        return progressHistory.stream()
                .filter(p -> p.getRecordedAt() != null && !p.getRecordedAt().toLocalDate().isAfter(weekEnd))
                .max(Comparator.comparing(GoalProgressEntity::getRecordedAt))
                .map(GoalProgressEntity::getProgressPercentage)
                .filter(Objects::nonNull)
                .map(v -> clampScore(v.intValue()))
                .orElse(50);
    }

    private int calculateHabitsScoreForWindow(HealthProfileEntity profile, List<WeightEntryEntity> weights,
            List<ActivityCheckinEntity> activityHistory, LocalDate weekStart, LocalDate weekEnd) {
        List<Integer> signals = new ArrayList<>();

        long daysWithWeightEntries = weights.stream()
                .filter(entry -> entry.getMeasuredAt() != null)
                .map(entry -> entry.getMeasuredAt().toLocalDate())
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .distinct()
                .count();
        int checkinConsistencyScore = (int) Math.min(100, Math.round((daysWithWeightEntries / 7.0) * 100.0));
        signals.add(checkinConsistencyScore);

        long activityDays = activityHistory.stream()
                .filter(entry -> entry.getCheckinAt() != null)
                .map(entry -> entry.getCheckinAt().toLocalDate())
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .distinct()
                .count();
        int activityConsistencyScore = (int) Math.min(100, Math.round((activityDays / 7.0) * 100.0));
        signals.add(activityConsistencyScore);

        Integer activityFrequency = extractWeeklyActivityFrequency(profile);
        if (activityFrequency != null) {
            signals.add(WellnessScoreCalculator.calculateActivityScore(activityFrequency));
        }

        double avg = signals.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(50.0);
        return clampScore((int) Math.round(avg));
    }

    private Integer countRecentActivityDays(UUID userId, OffsetDateTime since) {
        var entries = activityCheckinRepository.findByUserIdAndCheckinAtAfterOrderByCheckinAtDesc(userId, since);
        if (entries.isEmpty()) {
            return null;
        }
        return (int) entries.stream().map(ActivityCheckinEntity::getCheckinAt).map(OffsetDateTime::toLocalDate)
                .distinct().count();
    }

    private int calculateActivityScoreForWindow(HealthProfileEntity profile, List<ActivityCheckinEntity> entries,
            LocalDate weekStart, LocalDate weekEnd) {
        long activityDays = entries.stream()
                .filter(entry -> entry.getCheckinAt() != null)
                .map(entry -> entry.getCheckinAt().toLocalDate())
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .distinct()
                .count();
        if (activityDays > 0) {
            return WellnessScoreCalculator.calculateActivityScore((int) activityDays);
        }
        Integer baseline = extractWeeklyActivityFrequency(profile);
        if (baseline != null) {
            return WellnessScoreCalculator.calculateActivityScore(baseline);
        }
        return 0;
    }

    public Integer getWellnessScore(UUID userId) {
        return profileRepository.findById(userId).map(HealthProfileEntity::getWellnessScore).orElse(null);
    }

    public String getWellnessScoreDescription(UUID userId) {
        Integer score = getWellnessScore(userId);
        if (score == null) {
            return "No data available";
        }
        return WellnessScoreCalculator.getScoreDescription(score);
    }
}
