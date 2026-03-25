package ee.kaidokurm.ndl.health.profile;

import ee.kaidokurm.ndl.auth.user.UserService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import ee.kaidokurm.ndl.common.api.dto.HealthProfileResponse;
import ee.kaidokurm.ndl.common.api.mapper.ResponseMapper;
import ee.kaidokurm.ndl.common.api.validation.OwnershipValidator;
import ee.kaidokurm.ndl.health.unit.UnitConversionService;
import ee.kaidokurm.ndl.health.wellness.WellnessScoreService;

import java.util.Objects;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class ProfileController {

    private final UserService userService;
    private final HealthProfileService profileService;
    private final WellnessScoreService wellnessScoreService;
    private final HealthProfileRepository profileRepository;
    private final UnitConversionService unitConversionService;

    public ProfileController(UserService userService, HealthProfileService profileService,
            WellnessScoreService wellnessScoreService, HealthProfileRepository profileRepository,
            UnitConversionService unitConversionService) {
        this.userService = userService;
        this.profileService = profileService;
        this.wellnessScoreService = wellnessScoreService;
        this.profileRepository = profileRepository;
        this.unitConversionService = unitConversionService;
    }

    /**
     * Get user's health profile.
     */
    @GetMapping("/api/profile")
    public ApiSuccess<HealthProfileResponse> getProfile(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = profileService.find(user.getId()).orElse(null);
        return ApiSuccess.of(ResponseMapper.toHealthProfileResponse(entity));
    }

    /**
     * Create or update user's health profile. Accepts all health data:
     * demographics, activity, dietary, fitness assessment.
     * 
     * Automatically recalculates wellness score after profile update.
     */
    @PostMapping("/api/profile")
    public HealthProfileResponse upsertProfile(@Valid @RequestBody HealthProfileRequest request,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        int normalizedHeightCm = unitConversionService.toCentimeters(request.getHeightCm(), request.getHeightUnit());
        var profile = profileService.upsert(user.getId(), request.getBirthYear(), request.getGender(),
                normalizedHeightCm, request.getBaselineActivityLevel(), request.getDietaryPreferences(),
                request.getDietaryRestrictions(), request.getFitnessAssessment(),
                request.getFitnessAssessmentCompleted());

        // Auto-calculate wellness score after profile update
        wellnessScoreService.calculateAndUpdateWellnessScore(user.getId());

        return ResponseMapper.toHealthProfileResponse(profile);
    }

    /**
     * Delete user's health profile (soft delete).
     * 
     * @param auth JWT authentication token
     */
    @DeleteMapping("/api/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = profileService.find(user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Health profile");
        entity = Objects.requireNonNull(entity);
        OwnershipValidator.validateOwnership(entity.getUserId(), user.getId(), "health profile");
        profileRepository.delete(entity);
    }
}
