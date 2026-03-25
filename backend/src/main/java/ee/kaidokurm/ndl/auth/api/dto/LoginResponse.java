package ee.kaidokurm.ndl.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response returned after successful email/password login. Contains access
 * token (JWT) and refresh token for token renewal.
 */
public record LoginResponse(@JsonProperty("access_token") String accessToken,

        @JsonProperty("refresh_token") String refreshToken,

        @JsonProperty("token_type") String tokenType,

        @JsonProperty("expires_in") long expiresIn) {
    public LoginResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer", 900);
    }
}
