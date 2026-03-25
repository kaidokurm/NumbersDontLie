package ee.kaidokurm.ndl.auth.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Password reset token entity.
 * 
 * When a user requests password reset, a random token is generated and sent to
 * their email. User clicks link with token, which takes them to reset form
 * where they set a new password. Token is one-time use: once used_at is set,
 * token cannot be reused. Token is valid for 1 hour.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    public PasswordResetTokenEntity() {
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
    }

    // Business Logic Methods

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isAlreadyUsed() {
        return usedAt != null;
    }

    public void markAsUsed() {
        this.usedAt = OffsetDateTime.now();
    }

    @Override
    public String toString() {
        return "PasswordResetTokenEntity{" + "id=" + id + ", userId=" + userId + ", token='" + token + '\''
                + ", createdAt=" + createdAt + ", expiresAt=" + expiresAt + ", usedAt=" + usedAt + '}';
    }
}
