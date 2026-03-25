package ee.kaidokurm.ndl.export;

import ee.kaidokurm.ndl.ai.insight.AiInsightRepository;
import ee.kaidokurm.ndl.auth.twofactor.TwoFactorSecretRepository;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;
import ee.kaidokurm.ndl.health.goal.GoalProgressRepository;
import ee.kaidokurm.ndl.health.goal.GoalRepository;
import ee.kaidokurm.ndl.health.profile.HealthProfileRepository;
import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;
import ee.kaidokurm.ndl.privacy.PrivacyPreferencesRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DataExportService {

        private final UserRepository userRepository;
        private final HealthProfileRepository healthProfileRepository;
        private final PrivacyPreferencesRepository privacyPreferencesRepository;
        private final TwoFactorSecretRepository twoFactorSecretRepository;
        private final WeightEntryRepository weightEntryRepository;
        private final GoalRepository goalRepository;
        private final GoalProgressRepository goalProgressRepository;
        private final AiInsightRepository aiInsightRepository;
        private final ObjectMapper objectMapper;

        public DataExportService(
                        UserRepository userRepository,
                        HealthProfileRepository healthProfileRepository,
                        PrivacyPreferencesRepository privacyPreferencesRepository,
                        TwoFactorSecretRepository twoFactorSecretRepository,
                        WeightEntryRepository weightEntryRepository,
                        GoalRepository goalRepository,
                        GoalProgressRepository goalProgressRepository,
                        AiInsightRepository aiInsightRepository,
                        ObjectMapper objectMapper) {
                this.userRepository = userRepository;
                this.healthProfileRepository = healthProfileRepository;
                this.privacyPreferencesRepository = privacyPreferencesRepository;
                this.twoFactorSecretRepository = twoFactorSecretRepository;
                this.weightEntryRepository = weightEntryRepository;
                this.goalRepository = goalRepository;
                this.goalProgressRepository = goalProgressRepository;
                this.aiInsightRepository = aiInsightRepository;
                this.objectMapper = objectMapper;
        }

        public DataExportResponse exportForUser(UUID userId) {
                var user = userRepository.findById(userId).orElseThrow();

                var profile = healthProfileRepository.findByUserId(userId).map(p -> new HealthProfileData(
                                p.getBirthYear(),
                                p.getGender(),
                                p.getHeightCm(),
                                p.getBaselineActivityLevel(),
                                p.getDietaryPreferences(),
                                p.getDietaryRestrictions(),
                                p.getFitnessAssessment(),
                                p.getFitnessAssessmentCompleted(),
                                p.getBmiValue(),
                                p.getBmiClassification(),
                                p.getWellnessScore(),
                                p.getCreatedAt(),
                                p.getUpdatedAt())).orElse(null);

                var privacy = privacyPreferencesRepository.findByUserId(userId).map(p -> new PrivacyPreferencesData(
                                p.isDataUsageConsent(),
                                p.getConsentGivenAt(),
                                p.isAllowAnonymizedAnalytics(),
                                p.isPublicProfileVisible(),
                                p.isEmailNotificationsEnabled(),
                                p.getCreatedAt(),
                                p.getUpdatedAt())).orElse(null);

                var twoFactor = twoFactorSecretRepository.findByUserId(userId)
                                .map(tf -> new TwoFactorData(tf.isEnabled(), tf.getVerifiedAt(), tf.getCreatedAt(),
                                                tf.getUpdatedAt()))
                                .orElse(new TwoFactorData(false, null, null, null));

                var weights = weightEntryRepository.findByUserIdOrderByMeasuredAtDesc(userId).stream()
                                .map(w -> new WeightEntryData(w.getId(), w.getMeasuredAt(), w.getWeightKg(),
                                                w.getNote()))
                                .toList();

                var goals = goalRepository.findAllByUserIdNotDeleted(userId).stream()
                                .map(g -> new GoalData(
                                                g.getId(),
                                                g.getGoalType() == null ? null : g.getGoalType().name(),
                                                g.getTargetWeightKg(),
                                                g.getTargetActivityDaysPerWeek(),
                                                g.getTargetDate(),
                                                g.getNotes(),
                                                g.isActive(),
                                                g.getCreatedAt(),
                                                g.getUpdatedAt()))
                                .toList();

                var progress = goalProgressRepository.findByUserIdOrderByRecordedAtDesc(userId).stream()
                                .map(p -> new GoalProgressData(
                                                p.getId(),
                                                p.getGoalId(),
                                                p.getCurrentValue(),
                                                p.getProgressPercentage(),
                                                p.getIsOnTrack(),
                                                p.getDaysRemaining(),
                                                p.getMilestonesCompleted(),
                                                p.getMilestoneDetails(),
                                                p.getRecordedAt(),
                                                p.getCreatedAt(),
                                                p.getUpdatedAt()))
                                .toList();

                var insights = aiInsightRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                                .map(i -> new InsightData(
                                                i.getId(),
                                                i.getGoalId(),
                                                i.getInputHash(),
                                                i.getModel(),
                                                parsePayload(i.getPayload()),
                                                i.getCreatedAt()))
                                .toList();

                return new DataExportResponse(
                                OffsetDateTime.now(),
                                new AccountData(user.getId(), user.getAuth0Sub(), user.getEmail(),
                                                user.getEmailVerified(),
                                                user.getCreatedAt(), user.getUpdatedAt()),
                                profile,
                                privacy,
                                twoFactor,
                                weights,
                                goals,
                                progress,
                                insights);
        }

        private JsonNode parsePayload(JsonNode payload) {
                return payload == null ? objectMapper.getNodeFactory().nullNode() : payload;
        }

        public record DataExportResponse(
                        @JsonProperty("exported_at") OffsetDateTime exportedAt,
                        AccountData account,
                        @JsonProperty("health_profile") HealthProfileData healthProfile,
                        @JsonProperty("privacy_preferences") PrivacyPreferencesData privacyPreferences,
                        @JsonProperty("two_factor") TwoFactorData twoFactor,
                        @JsonProperty("weight_entries") List<WeightEntryData> weightEntries,
                        List<GoalData> goals,
                        @JsonProperty("goal_progress") List<GoalProgressData> goalProgress,
                        @JsonProperty("ai_insights") List<InsightData> aiInsights) {
        }

        public record AccountData(
                        @JsonProperty("user_id") UUID userId,
                        @JsonProperty("auth0_sub") String auth0Sub,
                        String email,
                        @JsonProperty("email_verified") Boolean emailVerified,
                        @JsonProperty("created_at") OffsetDateTime createdAt,
                        @JsonProperty("updated_at") OffsetDateTime updatedAt) {
        }

        public record HealthProfileData(
                        @JsonProperty("birth_year") Integer birthYear,
                        String gender,
                        @JsonProperty("height_cm") Integer heightCm,
                        @JsonProperty("baseline_activity_level") String baselineActivityLevel,
                        @JsonProperty("dietary_preferences") List<String> dietaryPreferences,
                        @JsonProperty("dietary_restrictions") List<String> dietaryRestrictions,
                        @JsonProperty("fitness_assessment") Map<String, Object> fitnessAssessment,
                        @JsonProperty("fitness_assessment_completed") Boolean fitnessAssessmentCompleted,
                        @JsonProperty("bmi_value") BigDecimal bmiValue,
                        @JsonProperty("bmi_classification") String bmiClassification,
                        @JsonProperty("wellness_score") Integer wellnessScore,
                        @JsonProperty("created_at") OffsetDateTime createdAt,
                        @JsonProperty("updated_at") OffsetDateTime updatedAt) {
        }

        public record PrivacyPreferencesData(
                        @JsonProperty("data_usage_consent") boolean dataUsageConsent,
                        @JsonProperty("consent_given_at") OffsetDateTime consentGivenAt,
                        @JsonProperty("allow_anonymized_analytics") boolean allowAnonymizedAnalytics,
                        @JsonProperty("public_profile_visible") boolean publicProfileVisible,
                        @JsonProperty("email_notifications_enabled") boolean emailNotificationsEnabled,
                        @JsonProperty("created_at") OffsetDateTime createdAt,
                        @JsonProperty("updated_at") OffsetDateTime updatedAt) {
        }

        public record TwoFactorData(
                        boolean enabled,
                        @JsonProperty("verified_at") OffsetDateTime verifiedAt,
                        @JsonProperty("created_at") OffsetDateTime createdAt,
                        @JsonProperty("updated_at") OffsetDateTime updatedAt) {
        }

        public record WeightEntryData(
                        UUID id,
                        @JsonProperty("measured_at") OffsetDateTime measuredAt,
                        @JsonProperty("weight_kg") double weightKg,
                        String note) {
        }

        public record GoalData(
                        UUID id,
                        @JsonProperty("goal_type") String goalType,
                        @JsonProperty("target_weight_kg") Double targetWeightKg,
                        @JsonProperty("target_activity_days_per_week") Integer targetActivityDaysPerWeek,
                        @JsonProperty("target_date") java.time.LocalDate targetDate,
                        String notes,
                        boolean active,
                        @JsonProperty("created_at") OffsetDateTime createdAt,
                        @JsonProperty("updated_at") OffsetDateTime updatedAt) {
        }

        public record GoalProgressData(
                        UUID id,
                        @JsonProperty("goal_id") UUID goalId,
                        @JsonProperty("current_value") BigDecimal currentValue,
                        @JsonProperty("progress_percentage") Integer progressPercentage,
                        @JsonProperty("is_on_track") Boolean isOnTrack,
                        @JsonProperty("days_remaining") Integer daysRemaining,
                        @JsonProperty("milestones_completed") Integer milestonesCompleted,
                        @JsonProperty("milestone_details") List<Map<String, Object>> milestoneDetails,
                        @JsonProperty("recorded_at") OffsetDateTime recordedAt,
                        @JsonProperty("created_at") OffsetDateTime createdAt,
                        @JsonProperty("updated_at") OffsetDateTime updatedAt) {
        }

        public record InsightData(
                        UUID id,
                        @JsonProperty("goal_id") UUID goalId,
                        @JsonProperty("input_hash") String inputHash,
                        String model,
                        JsonNode payload,
                        @JsonProperty("created_at") OffsetDateTime createdAt) {
        }
}
