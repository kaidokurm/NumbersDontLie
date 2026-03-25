package ee.kaidokurm.ndl.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Centralized configuration properties for authentication. Binds properties
 * from application.yaml under 'app.jwt.*' and 'app.auth.*'
 * 
 * Example yaml: app: jwt: access-token-expiry-minutes: 15 issuer: ndl-api auth:
 * password-min-length: 12
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class AuthProperties {

    private Jwt jwt = new Jwt();
    private Auth auth = new Auth();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    /**
     * JWT token configuration
     */
    public static class Jwt {
        private long accessTokenExpiryMinutes = 15;
        private String issuer = "ndl-api";

        public long getAccessTokenExpiryMinutes() {
            return accessTokenExpiryMinutes;
        }

        public void setAccessTokenExpiryMinutes(long accessTokenExpiryMinutes) {
            this.accessTokenExpiryMinutes = accessTokenExpiryMinutes;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }

    /**
     * General authentication configuration
     */
    public static class Auth {
        private int passwordMinLength = 12;

        public int getPasswordMinLength() {
            return passwordMinLength;
        }

        public void setPasswordMinLength(int passwordMinLength) {
            this.passwordMinLength = passwordMinLength;
        }
    }
}
