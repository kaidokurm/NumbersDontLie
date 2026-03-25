package ee.kaidokurm.ndl.health.weight;

import ee.kaidokurm.ndl.auth.user.UserService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import ee.kaidokurm.ndl.common.api.dto.PaginatedResponse;
import ee.kaidokurm.ndl.common.api.dto.WeightEntryResponse;
import ee.kaidokurm.ndl.common.api.mapper.ResponseMapper;
import ee.kaidokurm.ndl.common.api.validation.OwnershipValidator;
import ee.kaidokurm.ndl.health.unit.UnitConversionService;

import java.util.Objects;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class WeightController {

    private final UserService userService;
    private final WeightService weightService;
    private final WeightEntryRepository weightRepository;
    private final UnitConversionService unitConversionService;

    public WeightController(UserService userService, WeightService weightService,
            WeightEntryRepository weightRepository, UnitConversionService unitConversionService) {
        this.userService = userService;
        this.weightService = weightService;
        this.weightRepository = weightRepository;
        this.unitConversionService = unitConversionService;
    }

    public static class AddWeightRequest {
        @DecimalMin(value = "1.0", message = "weightKg must be > 0")
        public double weightKg;

        @com.fasterxml.jackson.annotation.JsonProperty("weight_unit")
        public String weightUnit;

        public OffsetDateTime measuredAt;
        public String note;
    }

    public static class UpdateWeightRequest {
        @DecimalMin(value = "1.0", message = "weightKg must be > 0")
        public Double weightKg;

        @com.fasterxml.jackson.annotation.JsonProperty("weight_unit")
        public String weightUnit;

        public OffsetDateTime measuredAt;
        public String note;
    }

    /**
     * Add a new weight entry.
     * 
     * @param body the weight entry request body
     * @param auth JWT authentication token
     * @return the created weight entry
     */
    @PostMapping("/api/weight")
    @ResponseStatus(HttpStatus.CREATED)
    public WeightEntryResponse add(@Valid @RequestBody AddWeightRequest body, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        double normalizedWeightKg = unitConversionService.toKilograms(body.weightKg, body.weightUnit);
        var entity = weightService.add(user.getId(), normalizedWeightKg, body.measuredAt, body.note);
        return ResponseMapper.toWeightEntryResponse(entity);
    }

    /**
     * Get 30 latest weight entries for the authenticated user.
     * 
     * @param auth JWT authentication token
     * @return list of weight entries
     */
    @GetMapping("/api/weight")
    public List<WeightEntryResponse> list(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entities = weightService.latest(user.getId());
        return entities.stream().map(ResponseMapper::toWeightEntryResponse).toList();
    }

    /**
     * Get the latest weight entry for the authenticated user.
     * 
     * @param auth JWT authentication token
     * @return the latest weight entry
     */
    @GetMapping("/api/weight/latest")
    public ApiSuccess<WeightEntryResponse> getLatest(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = weightService.latest(user.getId()).stream().findFirst().orElse(null);
        return ApiSuccess.of(ResponseMapper.toWeightEntryResponse(entity));
    }

    /**
     * Get a specific weight entry by ID.
     * 
     * @param id   the weight entry ID
     * @param auth JWT authentication token
     * @return the weight entry
     */
    @GetMapping("/api/weight/{id}")
    public WeightEntryResponse get(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = weightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Weight entry");
        entity = Objects.requireNonNull(entity);
        return ResponseMapper.toWeightEntryResponse(entity);
    }

    /**
     * Update a weight entry.
     * 
     * @param id   the weight entry ID
     * @param body the update request
     * @param auth JWT authentication token
     * @return the updated weight entry
     */
    @PatchMapping("/api/weight/{id}")
    public WeightEntryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateWeightRequest body,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = weightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Weight entry");
        entity = Objects.requireNonNull(entity);

        if (body.weightKg != null) {
            entity.setWeightKg(unitConversionService.toKilograms(body.weightKg, body.weightUnit));
        }
        if (body.measuredAt != null) {
            entity.setMeasuredAt(body.measuredAt);
        }
        if (body.note != null) {
            entity.setNote(body.note);
        }

        var updated = weightService.update(user.getId(), entity);
        return ResponseMapper.toWeightEntryResponse(updated);
    }

    /**
     * Get paginated weight history for the authenticated user.
     * 
     * @param page the page number (0-indexed)
     * @param size the page size
     * @param auth JWT authentication token
     * @return paginated weight entries
     */
    @GetMapping("/api/weight/history")
    public PaginatedResponse<WeightEntryResponse> getHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var pageRequest = PageRequest.of(page, size, Sort.by("measuredAt").descending());
        var weightPage = weightService.getHistory(user.getId(), pageRequest);

        var content = weightPage.getContent().stream().map(ResponseMapper::toWeightEntryResponse).toList();

        return new PaginatedResponse<>(content, weightPage.getNumber(), weightPage.getSize(),
                weightPage.getTotalElements(), weightPage.getTotalPages(), weightPage.isFirst(), weightPage.isLast());
    }

    /**
     * Delete a weight entry (soft delete).
     * 
     * @param id   the weight entry ID
     * @param auth JWT authentication token
     */
    @DeleteMapping("/api/weight/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = weightRepository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Weight entry");
        weightRepository.delete(entity);
    }
}
