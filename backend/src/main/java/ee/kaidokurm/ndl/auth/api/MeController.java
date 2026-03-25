package ee.kaidokurm.ndl.auth.api;

import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ee.kaidokurm.ndl.auth.user.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class MeController {

    private static final Logger log = LoggerFactory.getLogger(MeController.class);
    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    public record MeResponse(
            String id,
            @JsonProperty("auth0_sub") String auth0Sub,
            String email,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            String sub,
            String iss,
            List<String> aud,
            String scope,
            Object claims) {
    }

    @GetMapping("/api/me")
    public ApiSuccess<MeResponse> me(JwtAuthenticationToken auth) {
        var jwt = auth.getToken();
        String issuer = jwt.getClaimAsString("iss");

        log.info("GET /api/me - issuer: {}, sub: {}, has email: {}", issuer, jwt.getSubject(),
                jwt.getClaimAsString("email") != null);

        // Sync user from JWT (saves email from OAuth providers)
        try {
            var user = userService.ensureUserFromJwt(auth);
            log.debug("User synced from JWT: sub={}", jwt.getSubject());
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("keys", jwt.getClaims().keySet());
            return ApiSuccess.of(new MeResponse(
                    user.getId().toString(),
                    user.getAuth0Sub() != null ? user.getAuth0Sub() : jwt.getSubject(),
                    user.getEmail(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    jwt.getSubject(),
                    issuer,
                    jwt.getAudience(),
                    jwt.getClaimAsString("scope"),
                    claims));
        } catch (Exception e) {
            log.error("Failed to sync user from JWT: {}", e.getMessage(), e);
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("keys", jwt.getClaims().keySet());
            return ApiSuccess.of(new MeResponse(
                    null,
                    null,
                    jwt.getClaimAsString("email"),
                    null,
                    null,
                    jwt.getSubject(),
                    issuer,
                    jwt.getAudience(),
                    jwt.getClaimAsString("scope"),
                    claims));
        }
    }
}
