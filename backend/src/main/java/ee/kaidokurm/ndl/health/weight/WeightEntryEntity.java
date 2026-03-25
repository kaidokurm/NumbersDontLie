package ee.kaidokurm.ndl.health.weight;

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
@Table(name = "weight_entries")
@SQLDelete(sql = "UPDATE weight_entries SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WeightEntryEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    @Column(name = "weight_kg", nullable = false)
    private double weightKg;

    @Column(name = "note")
    @Convert(converter = EncryptedStringConverter.class)
    private String note;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected WeightEntryEntity() {
    }

    public WeightEntryEntity(UUID id, UUID userId, OffsetDateTime measuredAt, double weightKg, String note) {
        this.id = id;
        this.userId = userId;
        this.measuredAt = measuredAt;
        this.weightKg = weightKg;
        this.note = note;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public String getNote() {
        return note;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public void setMeasuredAt(OffsetDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
