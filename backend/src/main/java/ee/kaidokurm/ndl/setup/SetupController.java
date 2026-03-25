package ee.kaidokurm.ndl.setup;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ee.kaidokurm.ndl.auth.user.UserService;

/**
 * REST API for onboarding/setup flow.
 *
 * Provides a single endpoint to check what parts of setup are complete.
 * Frontend polls this to decide whether to show setup wizard or main app.
 */
@RestController
@RequestMapping("/api/setup")
@Tag(name = "Setup", description = "Onboarding flow endpoints")
public class SetupController {

    private final UserService userService;
    private final SetupService setupService;

    public SetupController(UserService userService, SetupService setupService) {
        this.userService = userService;
        this.setupService = setupService;
    }

    /**
     * Get setup status for the current user.
     *
     * Returns what parts of onboarding are complete. Example response: {
     * "isComplete": false, "missing": ["profile", "weight"] }
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/status")
    public SetupStatusDto getSetupStatus(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        return setupService.getSetupStatus(user);
    }
}
