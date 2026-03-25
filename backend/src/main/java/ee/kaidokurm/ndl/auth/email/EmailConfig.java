package ee.kaidokurm.ndl.auth.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Email configuration properties from application.yaml Supports both console
 * logging (for testing) and real SMTP
 */
@Configuration
@ConfigurationProperties(prefix = "app.email")
public class EmailConfig {

    /**
     * Whether to actually send emails via SMTP. If false, emails are logged to
     * console (testing mode)
     */
    private boolean enabled = false;

    /**
     * Email address to send from
     */
    private String fromAddress;

    /**
     * Display name for from address (e.g., "Numbers Don't Lie <noreply@...>")
     */
    private String fromName;

    /**
     * Frontend base URL for email links (e.g., https://app.example.com)
     */
    private String frontendUrl = "http://localhost:5173";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }
}
