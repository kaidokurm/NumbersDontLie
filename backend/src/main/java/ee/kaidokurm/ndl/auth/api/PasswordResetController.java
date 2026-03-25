package ee.kaidokurm.ndl.auth.api;

import ee.kaidokurm.ndl.auth.api.dto.PasswordResetResponse;
import ee.kaidokurm.ndl.auth.api.dto.CompleteResetRequest;
import ee.kaidokurm.ndl.auth.api.dto.PasswordResetRequest;
import ee.kaidokurm.ndl.auth.service.PasswordResetService;
import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import ee.kaidokurm.ndl.common.ratelimit.RateLimitService;

import java.time.Duration;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;

    public PasswordResetController(
            PasswordResetService passwordResetService,
            UserRepository userRepository,
            RateLimitService rateLimitService) {
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Request a password reset. Sends a reset link to user's email with a token.
     * 
     * Request: { "email": "user@example.com" } Response: - 200: { "message":
     * "Password reset link sent to email" } Note: Always returns 200 for security
     * (don't reveal if email exists)
     */
    @PostMapping("/request")
    public ResponseEntity<ApiSuccess<PasswordResetResponse>> requestReset(
            @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        rateLimitService.check("password-reset:request:email", request.getEmail(), 3, Duration.ofMinutes(10));
        rateLimitService.check("password-reset:request:ip", httpRequest.getRemoteAddr(), 20, Duration.ofMinutes(10));

        // Request reset (generates token, no error if email doesn't exist)
        passwordResetService.requestPasswordReset(request.getEmail());

        // Always return success for security
        return ResponseEntity.ok(
                ApiSuccess.of(new PasswordResetResponse("Password reset link sent to email (if account exists)")));
    }

    /**
     * Complete password reset with token and new password.
     * 
     * Request: { "email": "user@example.com", "token": "uuid-string",
     * "newPassword": "NewPassword123!" } Response: - 200: { "message": "Password
     * reset successfully" } - 400: { "message": "Invalid or expired token" } or
     * validation error - 404: { "message": "User not found" }
     */
    @PostMapping("/complete")
    public ResponseEntity<ApiSuccess<PasswordResetResponse>> completeReset(@RequestBody CompleteResetRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getToken() == null || request.getToken().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }
        // Find user by email
        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Email verification is required before password reset");
        }
        // Complete password reset
        boolean success = passwordResetService.completePasswordReset(request.getToken(), request.getNewPassword(),
                request.getEmail());
        if (!success) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        return ResponseEntity.ok(ApiSuccess.of(new PasswordResetResponse("Password reset successfully")));
    }
}
