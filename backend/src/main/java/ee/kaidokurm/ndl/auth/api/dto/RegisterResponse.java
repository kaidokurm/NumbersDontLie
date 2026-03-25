package ee.kaidokurm.ndl.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Response returned after successful user registration. Email verification code
 * is sent separately to the user's email.
 */
public record RegisterResponse(@JsonProperty("id") UUID id,

        @JsonProperty("email") String email,

        @JsonProperty("email_verified") boolean emailVerified,

        @JsonProperty("message") String message) {
    public RegisterResponse(UUID id, String email) {
        this(id, email, false, "User registered. Check email for verification code.");
    }
}
