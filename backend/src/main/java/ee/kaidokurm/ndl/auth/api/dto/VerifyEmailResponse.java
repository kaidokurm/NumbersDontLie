package ee.kaidokurm.ndl.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response returned after successfully verifying an email verification code.
 */
public record VerifyEmailResponse(@JsonProperty("message") String message,

        @JsonProperty("email_verified") boolean emailVerified) {
    public VerifyEmailResponse() {
        this("Email verified successfully", true);
    }
}
