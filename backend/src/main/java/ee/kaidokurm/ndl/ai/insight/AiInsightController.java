package ee.kaidokurm.ndl.ai.insight;

import ee.kaidokurm.ndl.ai.insight.AiInsightService.AiInsightResult;
import ee.kaidokurm.ndl.auth.user.UserService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import ee.kaidokurm.ndl.common.api.validation.OwnershipValidator;
import ee.kaidokurm.ndl.common.ratelimit.RateLimitService;
import ee.kaidokurm.ndl.privacy.PrivacyPreferencesService;

import java.util.Objects;
import java.time.Duration;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class AiInsightController {

    private final UserService userService;
    private final AiInsightService insightService;
    private final AiInsightRepository insightRepository;
    private final PrivacyPreferencesService privacyPreferencesService;
    private final RateLimitService rateLimitService;

    public AiInsightController(UserService userService, AiInsightService insightService,
            AiInsightRepository insightRepository, PrivacyPreferencesService privacyPreferencesService,
            RateLimitService rateLimitService) {
        this.userService = userService;
        this.insightService = insightService;
        this.insightRepository = insightRepository;
        this.privacyPreferencesService = privacyPreferencesService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Get the current AI insight for the authenticated user.
     * 
     * @param auth JWT authentication token
     * @return the current insight
     */
    @GetMapping("/api/insights/current")
    public ApiSuccess<AiInsightResult> current(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        rateLimitService.check("ai-insights:current:user", user.getId().toString(), 8, Duration.ofMinutes(1));
        if (!privacyPreferencesService.hasDataUsageConsent(user.getId())) {
            throw new IllegalStateException("Data usage consent is required before generating AI insights");
        }
        return ApiSuccess.of(insightService.getCurrent(user.getId()));
    }

    /**
     * Delete an AI insight (soft delete).
     * 
     * @param id   the insight ID
     * @param auth JWT authentication token
     */
    @DeleteMapping("/api/insights/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = insightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Insight");
        entity = Objects.requireNonNull(entity);
        insightRepository.delete(entity);
    }
}
