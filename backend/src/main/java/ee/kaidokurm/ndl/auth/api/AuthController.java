package ee.kaidokurm.ndl.auth.api;

import ee.kaidokurm.ndl.auth.service.AuthService;
import ee.kaidokurm.ndl.auth.user.model.UserEntity;
import ee.kaidokurm.ndl.auth.api.dto.LoginResponse;
import ee.kaidokurm.ndl.auth.api.dto.RegisterResponse;
import ee.kaidokurm.ndl.auth.email.EmailService;
import ee.kaidokurm.ndl.auth.api.dto.AuthLoginRequest;
import ee.kaidokurm.ndl.auth.api.dto.AuthRegisterRequest;
import ee.kaidokurm.ndl.auth.twofactor.TwoFactorService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import ee.kaidokurm.ndl.common.ratelimit.RateLimitService;

import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final TwoFactorService twoFactorService;
    private final RateLimitService rateLimitService;

    public AuthController(
            AuthService authService,
            EmailService emailService,
            TwoFactorService twoFactorService,
            RateLimitService rateLimitService) {
        this.authService = authService;
        this.emailService = emailService;
        this.twoFactorService = twoFactorService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Register a new user with email and password.
     * 
     * Request: { "email": "user@example.com", "password": "Password123!" }
     * Response: - 201: { "id": "uuid", "email": "user@example.com",
     * "emailVerified": false, "message": "User registered. Check email for
     * verification code." } - 400: { "message": "Email already registered" } or
     * validation error
     */
    @PostMapping("/register")
    public ResponseEntity<ApiSuccess<RegisterResponse>> register(
            @RequestBody AuthRegisterRequest request,
            HttpServletRequest httpRequest) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        rateLimitService.check("auth:register:email", request.getEmail(), 5, Duration.ofMinutes(10));
        rateLimitService.check("auth:register:ip", httpRequest.getRemoteAddr(), 20, Duration.ofMinutes(10));
        // Register user
        UserEntity user = authService.registerUser(request.getEmail(), request.getPassword());
        // Generate and send verification code
        emailService.generateVerificationCode(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccess.of(new RegisterResponse(user.getId(), user.getEmail())));
    }

    /**
     * Authenticate user with email and password.
     * 
     * Request: { "email": "user@example.com", "password": "Password123!" }
     * Response: - 200: { "id": "uuid", "email": "user@example.com",
     * "emailVerified": true } - 401: { "message": "Invalid email or password" }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiSuccess<LoginResponse>> login(
            @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        rateLimitService.check("auth:login:email", request.getEmail(), 10, Duration.ofMinutes(1));
        rateLimitService.check("auth:login:ip", httpRequest.getRemoteAddr(), 40, Duration.ofMinutes(1));
        // Authenticate user
        UserEntity user = authService.authenticateUser(request.getEmail(), request.getPassword());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException("Email verification is required before login");
        }
        if (twoFactorService.isEnabled(user.getId())) {
            if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isBlank()) {
                throw new IllegalStateException("Two-factor code is required");
            }
            boolean validCode = twoFactorService.verifyCode(user.getId(), request.getTwoFactorCode().trim());
            if (!validCode) {
                throw new IllegalArgumentException("Invalid two-factor code");
            }
        }
        // Generate JWT access token
        String accessToken = authService.generateAccessToken(user);
        // Generate refresh token (raw token returned once, hashed value stored at rest)
        String refreshToken = authService.generateRefreshToken(user);
        // Return tokens
        return ResponseEntity.ok(ApiSuccess.of(new LoginResponse(accessToken, refreshToken)));
    }
}
