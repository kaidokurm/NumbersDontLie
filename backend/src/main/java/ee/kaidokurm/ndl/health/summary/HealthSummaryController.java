package ee.kaidokurm.ndl.health.summary;

import ee.kaidokurm.ndl.auth.user.UserService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import ee.kaidokurm.ndl.health.profile.HealthProfileEntity;
import ee.kaidokurm.ndl.health.profile.HealthProfileService;
import ee.kaidokurm.ndl.health.goal.GoalProgressEntity;
import ee.kaidokurm.ndl.health.goal.GoalProgressRepository;
import ee.kaidokurm.ndl.health.goal.GoalRepository;
import ee.kaidokurm.ndl.health.weight.WeightEntryEntity;
import ee.kaidokurm.ndl.health.weight.WeightEntryRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class HealthSummaryController {

        private final UserService userService;
        private final HealthProfileService profileService;
        private final WeightEntryRepository weightRepo;
        private final HealthSummaryService summaryService;
        private final GoalRepository goalRepository;
        private final GoalProgressRepository goalProgressRepository;

        public HealthSummaryController(UserService userService, HealthProfileService profileService,
                        WeightEntryRepository weightRepo, HealthSummaryService summaryService,
                        GoalRepository goalRepository, GoalProgressRepository goalProgressRepository) {
                this.userService = userService;
                this.profileService = profileService;
                this.weightRepo = weightRepo;
                this.summaryService = summaryService;
                this.goalRepository = goalRepository;
                this.goalProgressRepository = goalProgressRepository;
        }

        @GetMapping("/api/summary")
        public ApiSuccess<HealthSummaryDto> summary(JwtAuthenticationToken auth) {
                var user = userService.ensureUserFromJwt(auth);

                HealthProfileEntity profile = profileService.find(user.getId())
                                .orElseThrow(() -> new IllegalStateException("Profile required"));

                var weights = weightRepo.findTop30ByUserIdOrderByMeasuredAtDesc(user.getId());
                if (weights.isEmpty()) {
                        throw new IllegalStateException("Weight data required");
                }

                var latest = weights.get(0);
                double bmi = summaryService.bmi(profile.getHeightCm(), latest.getWeightKg());
                Double delta7d = summaryService.weightDelta7d(weights);

                return ApiSuccess.of(new HealthSummaryDto(profile.getHeightCm(), latest.getWeightKg(),
                                Math.round(bmi * 10.0) / 10.0, delta7d));
        }

        /**
         * Get weekly health summary for the past 7 days
         */
        @GetMapping("/api/summary/weekly")
        public ApiSuccess<PeriodSummaryDto> weeklySummary(JwtAuthenticationToken auth) {
                var user = userService.ensureUserFromJwt(auth);
                HealthProfileEntity profile = profileService.find(user.getId()).orElse(null);

                var weights = weightRepo.findTop90ByUserIdOrderByMeasuredAtDesc(user.getId());

                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(7);

                Double weightChange = summaryService.weightDeltaForPeriod(weights, 7);
                List<WeightEntryEntity> periodWeights = summaryService.entriesInRange(weights, startDate, endDate);

                Double avgWellness = profile != null && profile.getWellnessScore() != null
                                ? profile.getWellnessScore().doubleValue()
                                : 0.0;
                Integer goalProgressPercentage = getGoalProgressPercentageForPeriod(user.getId(), startDate, endDate);

                return ApiSuccess.of(new PeriodSummaryDto("weekly", startDate.toString(), endDate.toString(),
                                !periodWeights.isEmpty() ? periodWeights.get(periodWeights.size() - 1).getWeightKg()
                                                : null,
                                !periodWeights.isEmpty() ? periodWeights.get(0).getWeightKg() : null, weightChange,
                                avgWellness,
                                profile != null ? profile.getBaselineActivityLevel() : "UNKNOWN",
                                goalProgressPercentage,
                                7, periodWeights.size()));
        }

        /**
         * Get monthly health summary for the past 30 days
         */
        @GetMapping("/api/summary/monthly")
        public ApiSuccess<PeriodSummaryDto> monthlySummary(JwtAuthenticationToken auth) {
                var user = userService.ensureUserFromJwt(auth);
                HealthProfileEntity profile = profileService.find(user.getId()).orElse(null);

                var weights = weightRepo.findTop90ByUserIdOrderByMeasuredAtDesc(user.getId());

                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(30);

                Double weightChange = summaryService.weightDeltaForPeriod(weights, 30);
                List<WeightEntryEntity> periodWeights = summaryService.entriesInRange(weights, startDate, endDate);

                Double avgWellness = profile != null && profile.getWellnessScore() != null
                                ? profile.getWellnessScore().doubleValue()
                                : 0.0;
                Integer goalProgressPercentage = getGoalProgressPercentageForPeriod(user.getId(), startDate, endDate);

                return ApiSuccess.of(new PeriodSummaryDto("monthly", startDate.toString(), endDate.toString(),
                                !periodWeights.isEmpty() ? periodWeights.get(periodWeights.size() - 1).getWeightKg()
                                                : null,
                                !periodWeights.isEmpty() ? periodWeights.get(0).getWeightKg() : null, weightChange,
                                avgWellness,
                                profile != null ? profile.getBaselineActivityLevel() : "UNKNOWN",
                                goalProgressPercentage, 30,
                                periodWeights.size()));
        }

        private Integer getGoalProgressPercentageForPeriod(java.util.UUID userId, LocalDate startDate,
                        LocalDate endDate) {
                var activeGoalOpt = goalRepository.findFirstByUserIdAndActiveTrue(userId);
                if (activeGoalOpt.isEmpty()) {
                        return 0;
                }

                var goalId = activeGoalOpt.get().getId();
                OffsetDateTime start = startDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
                OffsetDateTime end = endDate.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

                List<GoalProgressEntity> periodProgress = goalProgressRepository
                                .findByGoalIdOrderByRecordedAtDesc(goalId)
                                .stream()
                                .filter(p -> p.getRecordedAt() != null
                                                && !p.getRecordedAt().isBefore(start)
                                                && p.getRecordedAt().isBefore(end))
                                .toList();

                if (periodProgress.isEmpty()) {
                        return goalProgressRepository.findFirstByGoalIdOrderByRecordedAtDesc(goalId)
                                        .map(GoalProgressEntity::getProgressPercentage)
                                        .orElse(0);
                }

                double average = periodProgress.stream()
                                .map(GoalProgressEntity::getProgressPercentage)
                                .filter(java.util.Objects::nonNull)
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0.0);
                return (int) Math.round(average);
        }
}
