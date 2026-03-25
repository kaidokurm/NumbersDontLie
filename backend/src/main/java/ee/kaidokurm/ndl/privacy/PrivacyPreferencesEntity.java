package ee.kaidokurm.ndl.privacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "privacy_preferences")
public class PrivacyPreferencesEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "data_usage_consent", nullable = false)
    private boolean dataUsageConsent;

    @Column(name = "consent_given_at")
    private OffsetDateTime consentGivenAt;

    @Column(name = "allow_anonymized_analytics", nullable = false)
    private boolean allowAnonymizedAnalytics;

    @Column(name = "public_profile_visible", nullable = false)
    private boolean publicProfileVisible;

    @Column(name = "email_notifications_enabled", nullable = false)
    private boolean emailNotificationsEnabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected PrivacyPreferencesEntity() {
    }

    public PrivacyPreferencesEntity(UUID userId, boolean dataUsageConsent, OffsetDateTime consentGivenAt,
            boolean allowAnonymizedAnalytics, boolean publicProfileVisible, boolean emailNotificationsEnabled,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.userId = userId;
        this.dataUsageConsent = dataUsageConsent;
        this.consentGivenAt = consentGivenAt;
        this.allowAnonymizedAnalytics = allowAnonymizedAnalytics;
        this.publicProfileVisible = publicProfileVisible;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isDataUsageConsent() {
        return dataUsageConsent;
    }

    public void setDataUsageConsent(boolean dataUsageConsent) {
        this.dataUsageConsent = dataUsageConsent;
    }

    public OffsetDateTime getConsentGivenAt() {
        return consentGivenAt;
    }

    public void setConsentGivenAt(OffsetDateTime consentGivenAt) {
        this.consentGivenAt = consentGivenAt;
    }

    public boolean isAllowAnonymizedAnalytics() {
        return allowAnonymizedAnalytics;
    }

    public void setAllowAnonymizedAnalytics(boolean allowAnonymizedAnalytics) {
        this.allowAnonymizedAnalytics = allowAnonymizedAnalytics;
    }

    public boolean isPublicProfileVisible() {
        return publicProfileVisible;
    }

    public void setPublicProfileVisible(boolean publicProfileVisible) {
        this.publicProfileVisible = publicProfileVisible;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
