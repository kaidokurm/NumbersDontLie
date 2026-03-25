package ee.kaidokurm.ndl.auth.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Auth0UserInfoService {

    private static final Logger log = LoggerFactory.getLogger(Auth0UserInfoService.class);
    private final ObjectMapper objectMapper;

    public Auth0UserInfoService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public record UserInfo(String email, Boolean emailVerified) {
    }

    public UserInfo fetchUserInfo(String accessToken, String issuer) {
        if (accessToken == null || accessToken.isBlank() || issuer == null || issuer.isBlank()) {
            return new UserInfo(null, null);
        }

        String baseIssuer = issuer.endsWith("/") ? issuer : issuer + "/";
        String userInfoUrl = baseIssuer + "userinfo";

        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(userInfoUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                log.warn("Auth0 userinfo request failed: status={} url={}", status, userInfoUrl);
                return new UserInfo(null, null);
            }

            try (InputStream body = connection.getInputStream()) {
                Map<?, ?> payload = objectMapper.readValue(body, Map.class);
                String email = payload.get("email") instanceof String ? (String) payload.get("email") : null;
                Boolean emailVerified = payload.get("email_verified") instanceof Boolean
                        ? (Boolean) payload.get("email_verified")
                        : null;
                return new UserInfo(email, emailVerified);
            }
        } catch (Exception e) {
            log.warn("Could not fetch Auth0 userinfo from {}: {}", userInfoUrl, e.getMessage());
            return new UserInfo(null, null);
        }
    }
}
