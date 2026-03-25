package ee.kaidokurm.ndl.common.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ActivityCheckinResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("activity_type")
    private String activityType;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    @JsonProperty("intensity")
    private String intensity;

    @JsonProperty("note")
    private String note;

    @JsonProperty("checkin_at")
    private OffsetDateTime checkinAt;

    public ActivityCheckinResponse() {
    }

    public ActivityCheckinResponse(UUID id, String activityType, Integer durationMinutes, String intensity, String note,
            OffsetDateTime checkinAt) {
        this.id = id;
        this.activityType = activityType;
        this.durationMinutes = durationMinutes;
        this.intensity = intensity;
        this.note = note;
        this.checkinAt = checkinAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OffsetDateTime getCheckinAt() {
        return checkinAt;
    }

    public void setCheckinAt(OffsetDateTime checkinAt) {
        this.checkinAt = checkinAt;
    }
}
