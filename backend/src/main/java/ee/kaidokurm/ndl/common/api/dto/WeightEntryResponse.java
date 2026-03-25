package ee.kaidokurm.ndl.common.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for weight entry response. Contains weight measurement data including
 * value, timestamp, and optional note.
 */
public class WeightEntryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("weight_kg")
    private Double weightKg;

    @JsonProperty("measured_at")
    private OffsetDateTime measuredAt;

    @JsonProperty("note")
    private String note;

    public WeightEntryResponse() {
    }

    public WeightEntryResponse(UUID id, Double weightKg, OffsetDateTime measuredAt, String note) {
        this.id = id;
        this.weightKg = weightKg;
        this.measuredAt = measuredAt;
        this.note = note;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public OffsetDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(OffsetDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
