package ee.kaidokurm.ndl.auth.api;

import ee.kaidokurm.ndl.auth.api.dto.RefreshTokenRequest;
import ee.kaidokurm.ndl.auth.api.dto.RefreshTokenResponse;
import ee.kaidokurm.ndl.auth.model.RefreshTokenEntity;
import ee.kaidokurm.ndl.auth.service.AuthService;
import ee.kaidokurm.ndl.auth.service.RefreshTokenService;
import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Refresh token endpoint for obtaining new access tokens.
 * 
 * Users can use a valid refresh token to get a new access token without
 * re-entering credentials. Refresh tokens are valid for 7 days.
 */
@RestController
@RequestMapping("/api/auth")
public class RefreshTokenController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public RefreshTokenController(AuthService authService, RefreshTokenService refreshTokenService,
            UserRepository userRepository) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    /**
     * Refresh access token using a valid refresh token.
     * 
     * Request: { "refresh_token": "token-uuid" } Response: - 200: { "access_token":
     * "jwt", "refresh_token": "uuid", "token_type": "Bearer", "expires_in": 900 } -
     * 400: { "message": "Invalid or expired refresh token" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiSuccess<RefreshTokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        // Validate request
        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        // Validate refresh token
        RefreshTokenEntity tokenEntity = refreshTokenService.validateToken(request.getRefreshToken());
        if (tokenEntity == null) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Get user
        UserEntity user = userRepository.findById(tokenEntity.getUserId()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Generate new access token
        String newAccessToken = authService.generateAccessToken(user);

        // Optionally: Generate new refresh token (rotating refresh tokens pattern)
        // RefreshTokenEntity newRefreshToken = authService.generateRefreshToken(user);

        return ResponseEntity.ok(ApiSuccess.of(new RefreshTokenResponse(newAccessToken)));
    }
}
