package ee.kaidokurm.ndl.auth.api;

import ee.kaidokurm.ndl.auth.api.dto.ResendCodeRequest;
import ee.kaidokurm.ndl.auth.api.dto.ResendCodeResponse;
import ee.kaidokurm.ndl.auth.api.dto.VerifyEmailRequest;
import ee.kaidokurm.ndl.auth.api.dto.VerifyEmailResponse;
import ee.kaidokurm.ndl.auth.email.EmailService;
import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.user.model.UserRepository;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email-verification")
public class EmailVerificationController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    public EmailVerificationController(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /**
     * Verify an email with a 6-digit code.
     * 
     * Request: { "email": "user@example.com", "code": "123456" } Response: - 200: {
     * "message": "Email verified successfully", "emailVerified": true } - 400: {
     * "message": "Invalid or expired code" } - 404: { "message": "User not found" }
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiSuccess<VerifyEmailResponse>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new IllegalArgumentException("Code is required");
        }
        // Find user by email
        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        // Verify code
        boolean verified = emailService.verifyCode(request.getCode(), user);
        if (!verified) {
            throw new IllegalArgumentException("Invalid or expired code");
        }
        // Mark email as verified in user entity
        user.setEmailVerified(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiSuccess.of(new VerifyEmailResponse()));
    }

    /**
     * Resend verification code to user's email.
     * 
     * Request: { "email": "user@example.com" } Response: - 200: { "message": "Code
     * sent to email", "cooldownSeconds": 0 } - 400: { "message": "Cannot resend
     * yet. Wait X seconds", "cooldownSeconds": 45 } - 404: { "message": "User not
     * found" }
     */
    @PostMapping("/resend-code")
    public ResponseEntity<ApiSuccess<ResendCodeResponse>> resendCode(@RequestBody ResendCodeRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Find user by email
        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user can resend
        if (!emailService.canResendCode(user)) {
            long cooldown = emailService.getResendCooldownSeconds(user);
            throw new IllegalArgumentException("Cannot resend yet. Wait " + cooldown + " seconds");
        }

        // Generate and send new code
        emailService.generateVerificationCode(user);

        return ResponseEntity.ok(ApiSuccess.of(new ResendCodeResponse("Code sent to email")));
    }
}
