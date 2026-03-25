package ee.kaidokurm.ndl.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response returned when refreshing an access token. Returns a new access token
 * using the provided refresh token.
 */
public record RefreshTokenResponse(@JsonProperty("access_token") String accessToken,

        @JsonProperty("token_type") String tokenType,

        @JsonProperty("expires_in") long expiresIn) {
    public RefreshTokenResponse(String accessToken) {
        this(accessToken, "Bearer", 900);
    }
}
