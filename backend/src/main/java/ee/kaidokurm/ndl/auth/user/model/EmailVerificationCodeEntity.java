package ee.kaidokurm.ndl.auth.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Email verification code entity.
 * 
 * When a user registers, a 6-digit code is generated and sent to their email.
 * User must enter this code within 24 hours to verify they own the email. Code
 * is one-time use: once verified_at is set, code cannot be reused.
 */
@Entity
@Table(name = "email_verification_codes")
public class EmailVerificationCodeEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String code; // 6 digits: "123456"

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "last_resent_at")
    private OffsetDateTime lastResentAt;

    public EmailVerificationCodeEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(OffsetDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public OffsetDateTime getLastResentAt() {
        return lastResentAt;
    }

    public void setLastResentAt(OffsetDateTime lastResentAt) {
        this.lastResentAt = lastResentAt;
    }

    // Business Logic Methods

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isAlreadyVerified() {
        return verifiedAt != null;
    }

    public void markAsVerified() {
        this.verifiedAt = OffsetDateTime.now();
    }

    @Override
    public String toString() {
        return "EmailVerificationCodeEntity{" + "id=" + id + ", userId=" + userId + ", code='" + code + '\''
                + ", createdAt=" + createdAt + ", expiresAt=" + expiresAt + ", verifiedAt=" + verifiedAt
                + ", lastResentAt=" + lastResentAt + '}';
    }
}
