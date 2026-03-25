package ee.kaidokurm.ndl.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to refresh an access token.
 */
public class RefreshTokenRequest {
    @JsonProperty("refresh_token")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
