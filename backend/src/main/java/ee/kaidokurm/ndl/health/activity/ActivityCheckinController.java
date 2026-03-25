package ee.kaidokurm.ndl.health.activity;

import ee.kaidokurm.ndl.auth.user.UserService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import ee.kaidokurm.ndl.common.api.dto.ActivityCheckinResponse;
import ee.kaidokurm.ndl.common.api.dto.PaginatedResponse;
import ee.kaidokurm.ndl.common.api.mapper.ResponseMapper;
import ee.kaidokurm.ndl.common.api.validation.OwnershipValidator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class ActivityCheckinController {

    private final UserService userService;
    private final ActivityCheckinService activityService;
    private final ActivityCheckinRepository repository;

    public ActivityCheckinController(UserService userService, ActivityCheckinService activityService,
            ActivityCheckinRepository repository) {
        this.userService = userService;
        this.activityService = activityService;
        this.repository = repository;
    }

    public static class AddActivityCheckinRequest {
        @NotBlank(message = "activityType is required")
        @Size(max = 64, message = "activityType must be <= 64 chars")
        public String activityType;

        @Min(value = 1, message = "durationMinutes must be >= 1")
        @Max(value = 720, message = "durationMinutes must be <= 720")
        public Integer durationMinutes;

        @NotBlank(message = "intensity is required")
        @Size(max = 24, message = "intensity must be <= 24 chars")
        public String intensity;

        @Size(max = 500, message = "note must be <= 500 chars")
        public String note;

        public OffsetDateTime checkinAt;
    }

    public static class UpdateActivityCheckinRequest {
        @Size(max = 64, message = "activityType must be <= 64 chars")
        public String activityType;

        @Min(value = 1, message = "durationMinutes must be >= 1")
        @Max(value = 720, message = "durationMinutes must be <= 720")
        public Integer durationMinutes;

        @Size(max = 24, message = "intensity must be <= 24 chars")
        public String intensity;

        @Size(max = 500, message = "note must be <= 500 chars")
        public String note;

        public OffsetDateTime checkinAt;
    }

    @PostMapping("/api/activity")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityCheckinResponse add(@Valid @RequestBody AddActivityCheckinRequest body,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = activityService.add(user.getId(), body.activityType, body.durationMinutes, body.intensity,
                body.note, body.checkinAt);
        return ResponseMapper.toActivityCheckinResponse(entity);
    }

    @GetMapping("/api/activity")
    public List<ActivityCheckinResponse> latest(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        return activityService.latest(user.getId()).stream().map(ResponseMapper::toActivityCheckinResponse).toList();
    }

    @GetMapping("/api/activity/latest")
    public ApiSuccess<ActivityCheckinResponse> latestSingle(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        return ApiSuccess.of(ResponseMapper.toActivityCheckinResponse(activityService.latestSingle(user.getId())));
    }

    @GetMapping("/api/activity/history")
    public PaginatedResponse<ActivityCheckinResponse> history(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var pageRequest = PageRequest.of(page, size, Sort.by("checkinAt").descending());
        var historyPage = activityService.getHistory(user.getId(), pageRequest);

        var content = historyPage.getContent().stream().map(ResponseMapper::toActivityCheckinResponse).toList();

        return new PaginatedResponse<>(content, historyPage.getNumber(), historyPage.getSize(),
                historyPage.getTotalElements(), historyPage.getTotalPages(), historyPage.isFirst(),
                historyPage.isLast());
    }

    @PatchMapping("/api/activity/{id}")
    public ActivityCheckinResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateActivityCheckinRequest body,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = repository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Activity check-in");
        entity = Objects.requireNonNull(entity);

        if (body.activityType != null && !body.activityType.isBlank()) {
            entity.setActivityType(body.activityType);
        }
        if (body.durationMinutes != null) {
            entity.setDurationMinutes(body.durationMinutes);
        }
        if (body.intensity != null) {
            entity.setIntensity(body.intensity);
        }
        if (body.note != null) {
            entity.setNote(body.note);
        }
        if (body.checkinAt != null) {
            entity.setCheckinAt(body.checkinAt);
        }

        var saved = activityService.update(user.getId(), entity);
        return ResponseMapper.toActivityCheckinResponse(saved);
    }

    @DeleteMapping("/api/activity/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var entity = repository.findByIdAndUserId(id, user.getId()).orElse(null);
        OwnershipValidator.validateResourceExists(entity != null, "Activity check-in");
        activityService.delete(user.getId(), Objects.requireNonNull(entity));
    }
}
