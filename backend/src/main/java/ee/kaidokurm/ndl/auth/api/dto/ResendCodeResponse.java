package ee.kaidokurm.ndl.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response returned after successfully resending a verification code. Includes
 * cooldown information to guide client retry logic.
 */
public record ResendCodeResponse(@JsonProperty("message") String message,

        @JsonProperty("cooldown_seconds") long cooldownSeconds) {
    public ResendCodeResponse(String message) {
        this(message, 0);
    }
}
