package ee.kaidokurm.ndl.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response returned for password reset operations.
 */
public record PasswordResetResponse(@JsonProperty("message") String message) {
}
