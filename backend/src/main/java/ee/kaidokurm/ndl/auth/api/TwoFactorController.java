package ee.kaidokurm.ndl.auth.api;

import ee.kaidokurm.ndl.auth.twofactor.TwoFactorService;
import ee.kaidokurm.ndl.auth.user.UserService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/auth/2fa")
public class TwoFactorController {

    private final UserService userService;
    private final TwoFactorService twoFactorService;

    public TwoFactorController(UserService userService, TwoFactorService twoFactorService) {
        this.userService = userService;
        this.twoFactorService = twoFactorService;
    }

    public static class TwoFactorCodeRequest {
        public String code;
    }

    public record TwoFactorSetupResponse(String secret, String otpauthUri) {
    }

    public record TwoFactorStatusResponse(boolean enabled) {
    }

    @PostMapping("/setup")
    public ResponseEntity<ApiSuccess<TwoFactorSetupResponse>> setup(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        String accountLabel = user.getEmail() != null ? user.getEmail() : user.getId().toString();
        var setup = twoFactorService.setup(user.getId(), accountLabel);
        return ResponseEntity.ok(ApiSuccess.of(new TwoFactorSetupResponse(setup.secret(), setup.otpauthUri())));
    }

    @PostMapping("/enable")
    public ResponseEntity<ApiSuccess<TwoFactorStatusResponse>> enable(@RequestBody TwoFactorCodeRequest request,
            JwtAuthenticationToken auth) {
        if (request == null || request.code == null || request.code.isBlank()) {
            throw new IllegalArgumentException("2FA code is required");
        }
        var user = userService.ensureUserFromJwt(auth);
        boolean enabled = twoFactorService.enable(user.getId(), request.code.trim());
        if (!enabled) {
            throw new IllegalArgumentException("Invalid 2FA code");
        }
        return ResponseEntity.ok(ApiSuccess.of(new TwoFactorStatusResponse(true)));
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiSuccess<TwoFactorStatusResponse>> disable(@RequestBody TwoFactorCodeRequest request,
            JwtAuthenticationToken auth) {
        if (request == null || request.code == null || request.code.isBlank()) {
            throw new IllegalArgumentException("2FA code is required");
        }
        var user = userService.ensureUserFromJwt(auth);
        boolean disabled = twoFactorService.disable(user.getId(), request.code.trim());
        if (!disabled) {
            throw new IllegalArgumentException("Invalid 2FA code");
        }
        return ResponseEntity.ok(ApiSuccess.of(new TwoFactorStatusResponse(false)));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiSuccess<TwoFactorStatusResponse>> status(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        return ResponseEntity.ok(ApiSuccess.of(new TwoFactorStatusResponse(twoFactorService.isEnabled(user.getId()))));
    }
}
