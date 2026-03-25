package ee.kaidokurm.ndl.data;

import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;
import ee.kaidokurm.ndl.health.goal.GoalEntity;
import ee.kaidokurm.ndl.health.goal.GoalProgressEntity;
import ee.kaidokurm.ndl.health.goal.GoalProgressRepository;
import ee.kaidokurm.ndl.health.goal.GoalRepository;
import ee.kaidokurm.ndl.health.goal.GoalType;
import ee.kaidokurm.ndl.health.activity.ActivityCheckinEntity;
import ee.kaidokurm.ndl.health.activity.ActivityCheckinRepository;
import ee.kaidokurm.ndl.health.profile.HealthProfileEntity;
import ee.kaidokurm.ndl.health.profile.HealthProfileRepository;
import ee.kaidokurm.ndl.health.weight.WeightEntryEntity;
import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes demo data for testing and visualization.
 * Activated when demo.mode=true
 */
@Component
@ConditionalOnProperty(name = "demo.mode", havingValue = "true")
public class DemoDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String DEMO_EMAIL = "demo@example.com";
    private static final UUID DEMO_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final UserRepository userRepo;
    private final HealthProfileRepository profileRepo;
    private final GoalRepository goalRepo;
    private final GoalProgressRepository goalProgressRepo;
    private final WeightEntryRepository weightRepo;
    private final ActivityCheckinRepository activityRepo;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserRepository userRepo, HealthProfileRepository profileRepo, GoalRepository goalRepo,
            GoalProgressRepository goalProgressRepo, WeightEntryRepository weightRepo,
            ActivityCheckinRepository activityRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.goalRepo = goalRepo;
        this.goalProgressRepo = goalProgressRepo;
        this.weightRepo = weightRepo;
        this.activityRepo = activityRepo;
        this.passwordEncoder = passwordEncoder;
        initializeDemo();
    }

    @Transactional
    private void initializeDemo() {
        try {
            // Check if demo user already exists
            if (userRepo.findById(DEMO_USER_ID).isPresent()) {
                log.info("Demo user already exists, skipping initialization");
                return;
            }

            log.info("Initializing demo data...");

            // 1. Create demo user with email/password (NOT Auth0)
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            UserEntity user = new UserEntity(DEMO_USER_ID, null, DEMO_EMAIL, now);
            // Set password: demo@example.com (encoded)
            user.setPasswordHash(passwordEncoder.encode(DEMO_EMAIL));
            user.setEmailVerified(true);
            userRepo.save(user);
            log.info("Created demo user: {} (email/password auth)", DEMO_EMAIL);

            // 2. Create health profile
            HealthProfileEntity profile = new HealthProfileEntity(
                    DEMO_USER_ID, 1990, "Other", 175, "MODERATE", now, now);
            profile.setDietaryPreferences(List.of("High-protein", "Low-sugar"));
            profile.setDietaryRestrictions(List.of("Lactose"));
            profile.setFitnessAssessment(Map.<String, Object>ofEntries(
                    Map.entry("occupation_type", "Office"),
                    Map.entry("current_activity_frequency", 4),
                    Map.entry("exercise_types", List.of("Cardio", "Strength", "Outdoors")),
                    Map.entry("average_session_duration", "30_60"),
                    Map.entry("self_assessed_fitness_level", "INTERMEDIATE"),
                    Map.entry("preferred_exercise_environment", "GYM"),
                    Map.entry("exercise_time_preference", "EVENING"),
                    Map.entry("current_endurance_minutes", 35),
                    Map.entry("pushups_count", 30),
                    Map.entry("situps_count", 45),
                    Map.entry("pullups_count", 8),
                    Map.entry("run_3km_time_sec", 980)));
            profile.setFitnessAssessmentCompleted(true);
            profileRepo.save(profile);
            log.info("Created demo health profile (height: 175cm)");

            // 3. Create goals (multiple active + one archived for history testing)
            GoalEntity activeWeightGoal = new GoalEntity(UUID.randomUUID(), DEMO_USER_ID, GoalType.WEIGHT_LOSS, 75.0, 4,
                    now.toLocalDate().plusDays(90), "Lose 5kg in 3 months", true,
                    now, now);
            goalRepo.save(activeWeightGoal);

            GoalEntity activeFitnessGoal = new GoalEntity(UUID.randomUUID(), DEMO_USER_ID, GoalType.IMPROVE_FITNESS,
                    null,
                    5, now.toLocalDate().plusDays(60), "Reach 5 active days per week", true,
                    now.minusDays(5), now.minusDays(5));
            goalRepo.save(activeFitnessGoal);

            GoalEntity archivedGoal = new GoalEntity(UUID.randomUUID(), DEMO_USER_ID, GoalType.IMPROVE_FITNESS, null,
                    5, now.toLocalDate().minusDays(10), "Improve weekly training consistency", false,
                    now.minusDays(120), now.minusDays(20));
            goalRepo.save(archivedGoal);
            log.info("Created demo goals (multiple active + archived)");

            // 4. Create 30 days of weight entries with realistic progression
            createWeightEntries(DEMO_USER_ID, now);
            log.info("Created 30 days of weight entries");

            // 5. Create goal progress history for trend charts and analytics
            createWeightGoalProgressHistory(activeWeightGoal, now);
            createActivityGoalProgressHistory(activeFitnessGoal, now);
            createArchivedGoalProgressHistory(archivedGoal, now);
            log.info("Created goal progress history");

            // 6. Create activity check-ins for activity heatmap and timeline demos
            createActivityCheckins(DEMO_USER_ID, now);
            log.info("Created activity check-ins");

            // 7. Set BMI and wellness score snapshot for faster first-load demo UX
            var latestWeight = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(DEMO_USER_ID).stream().findFirst();
            latestWeight.ifPresent(w -> profile.calculateBMI(w.getWeightKg()));
            profile.setWellnessScore(74);
            profile.setUpdatedAt(now);
            profileRepo.save(profile);

            log.info("Demo data initialization complete!");

        } catch (Exception e) {
            log.error("Failed to initialize demo data", e);
            throw new RuntimeException("Demo data initialization failed", e);
        }
    }

    private void createWeightEntries(UUID userId, OffsetDateTime baseTime) {
        // Generate 30 days of weight data, trending downward (weight loss)
        // Start from 30 days ago, daily measurements
        double startWeight = 82.5;
        double dailyVariance = 0.3; // kg of daily fluctuation

        for (int daysAgo = 29; daysAgo >= 0; daysAgo--) {
            OffsetDateTime measuredAt = baseTime.minusDays(daysAgo);

            // Overall trend: losing ~0.15kg per day, plus random variation
            double trendDownward = (29 - daysAgo) * 0.15;
            double variance = (Math.random() - 0.5) * dailyVariance;
            double weight = startWeight - trendDownward + variance;

            WeightEntryEntity entry = new WeightEntryEntity(UUID.randomUUID(), userId, measuredAt,
                    Math.round(weight * 100.0) / 100.0,
                    "Daily weigh-in");
            weightRepo.save(entry);
        }
    }

    private void createWeightGoalProgressHistory(GoalEntity goal, OffsetDateTime baseTime) {
        // Progress snapshots over ~8 weeks to support weekly/monthly summary testing.
        double[] currentWeights = { 82.1, 81.5, 80.9, 80.4, 79.8, 79.1, 78.4, 77.8, 77.2 };
        int[] progressPercents = { 8, 16, 24, 33, 45, 57, 68, 79, 88 };

        for (int i = 0; i < currentWeights.length; i++) {
            OffsetDateTime ts = baseTime.minusDays(56 - (i * 7));
            GoalProgressEntity progress = new GoalProgressEntity(
                    UUID.randomUUID(),
                    goal.getId(),
                    goal.getUserId(),
                    java.math.BigDecimal.valueOf(currentWeights[i]),
                    progressPercents[i],
                    true,
                    Math.max(0,
                            (int) java.time.temporal.ChronoUnit.DAYS.between(baseTime,
                                    goal.getTargetDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC))),
                    ts,
                    ts,
                    ts);
            progress.setMilestonesCompleted(progressPercents[i] / 5);
            progress.setMilestoneDetails(List.of(Map.of(
                    "percentage", (progressPercents[i] / 5) * 5,
                    "completed_at", ts.toString())));
            goalProgressRepo.save(progress);
        }
    }

    private void createActivityGoalProgressHistory(GoalEntity goal, OffsetDateTime baseTime) {
        int[] activityDays = { 2, 3, 3, 4, 4, 5 };
        int[] progressPercents = { 40, 60, 60, 80, 80, 100 };

        for (int i = 0; i < activityDays.length; i++) {
            OffsetDateTime ts = baseTime.minusDays(35 - (i * 7));
            GoalProgressEntity progress = new GoalProgressEntity(
                    UUID.randomUUID(),
                    goal.getId(),
                    goal.getUserId(),
                    java.math.BigDecimal.valueOf(activityDays[i]),
                    progressPercents[i],
                    true,
                    Math.max(0, (int) java.time.temporal.ChronoUnit.DAYS.between(baseTime,
                            goal.getTargetDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC))),
                    ts,
                    ts,
                    ts);
            progress.setMilestonesCompleted(progressPercents[i] / 5);
            progress.setMilestoneDetails(List.of(Map.of(
                    "percentage", (progressPercents[i] / 5) * 5,
                    "completed_at", ts.toString())));
            goalProgressRepo.save(progress);
        }
    }

    private void createArchivedGoalProgressHistory(GoalEntity goal, OffsetDateTime baseTime) {
        int[] activityDays = { 2, 3, 3, 4, 5 };
        int[] progressPercents = { 40, 55, 60, 75, 100 };

        for (int i = 0; i < activityDays.length; i++) {
            OffsetDateTime ts = baseTime.minusDays(110 - (i * 10));
            GoalProgressEntity progress = new GoalProgressEntity(
                    UUID.randomUUID(),
                    goal.getId(),
                    goal.getUserId(),
                    java.math.BigDecimal.valueOf(activityDays[i]),
                    progressPercents[i],
                    true,
                    0,
                    ts,
                    ts,
                    ts);
            progress.setMilestonesCompleted(progressPercents[i] / 5);
            progress.setMilestoneDetails(List.of(Map.of(
                    "percentage", (progressPercents[i] / 5) * 5,
                    "completed_at", ts.toString())));
            goalProgressRepo.save(progress);
        }
    }

    private void createActivityCheckins(UUID userId, OffsetDateTime baseTime) {
        String[] activityTypes = { "cardio", "strength", "walking", "cycling", "mobility" };
        String[] intensities = { "low", "medium", "high" };

        for (int daysAgo = 35; daysAgo >= 0; daysAgo--) {
            int sessions = (daysAgo % 6 == 0) ? 0 : (daysAgo % 2 == 0 ? 1 : 2);
            for (int s = 0; s < sessions; s++) {
                OffsetDateTime ts = baseTime.minusDays(daysAgo).withHour(7 + (s * 10)).withMinute(15).withSecond(0)
                        .withNano(0);
                ActivityCheckinEntity entry = new ActivityCheckinEntity(
                        UUID.randomUUID(),
                        userId,
                        activityTypes[(daysAgo + s) % activityTypes.length],
                        25 + ((daysAgo + s) % 5) * 10,
                        intensities[(daysAgo + s) % intensities.length],
                        "Demo activity check-in",
                        ts,
                        ts,
                        ts);
                activityRepo.save(entry);
            }
        }
    }
}
