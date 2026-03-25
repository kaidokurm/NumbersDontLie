package ee.kaidokurm.ndl.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SensitiveTokenHasher {

    private final String pepper;

    public SensitiveTokenHasher(@Value("${app.security.token-pepper:}") String pepper) {
        this.pepper = pepper == null ? "" : pepper;
    }

    public String hash(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Token/code is required");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((pepper + "|" + raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash token", e);
        }
    }
}
