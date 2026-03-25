package ee.kaidokurm.ndl.health.activity;

import ee.kaidokurm.ndl.common.persistence.encryption.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "activity_checkins")
@SQLDelete(sql = "UPDATE activity_checkins SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ActivityCheckinEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_type", nullable = false)
    private String activityType;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "intensity")
    private String intensity;

    @Column(name = "note")
    @Convert(converter = EncryptedStringConverter.class)
    private String note;

    @Column(name = "checkin_at", nullable = false)
    private OffsetDateTime checkinAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected ActivityCheckinEntity() {
    }

    public ActivityCheckinEntity(UUID id, UUID userId, String activityType, Integer durationMinutes, String intensity,
            String note, OffsetDateTime checkinAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.activityType = activityType;
        this.durationMinutes = durationMinutes;
        this.intensity = intensity;
        this.note = note;
        this.checkinAt = checkinAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getIntensity() {
        return intensity;
    }

    public String getNote() {
        return note;
    }

    public OffsetDateTime getCheckinAt() {
        return checkinAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCheckinAt(OffsetDateTime checkinAt) {
        this.checkinAt = checkinAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
