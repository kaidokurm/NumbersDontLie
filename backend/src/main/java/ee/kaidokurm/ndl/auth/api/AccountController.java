package ee.kaidokurm.ndl.auth.api;

import ee.kaidokurm.ndl.auth.user.AccountDeletionService;
import ee.kaidokurm.ndl.auth.user.UserService;
import ee.kaidokurm.ndl.common.api.dto.ApiSuccess;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class AccountController {

    private static final String REQUIRED_CONFIRMATION = "DELETE MY ACCOUNT";

    private final UserService userService;
    private final AccountDeletionService accountDeletionService;

    public AccountController(UserService userService, AccountDeletionService accountDeletionService) {
        this.userService = userService;
        this.accountDeletionService = accountDeletionService;
    }

    public static class DeleteAccountRequest {
        @NotBlank(message = "confirmation is required")
        public String confirmation;
    }

    @GetMapping("/api/account/identities")
    public ApiSuccess<UserService.AccountAuthMethods> getAccountIdentities(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        return ApiSuccess.of(userService.getAccountAuthMethods(user.getId()));
    }

    @PostMapping("/api/account/identities/link-by-email")
    public ApiSuccess<UserService.AccountLinkResult> linkCurrentIdentityByEmail(JwtAuthenticationToken auth) {
        return ApiSuccess.of(userService.linkCurrentOAuthIdentityByEmail(auth));
    }

    @DeleteMapping("/api/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@Valid @RequestBody DeleteAccountRequest request, JwtAuthenticationToken auth) {
        if (!REQUIRED_CONFIRMATION.equals(request.confirmation == null ? "" : request.confirmation.trim())) {
            throw new IllegalArgumentException("Invalid confirmation phrase");
        }

        var user = userService.ensureUserFromJwt(auth);
        accountDeletionService.deleteAccountAndData(user.getId());
    }
}
