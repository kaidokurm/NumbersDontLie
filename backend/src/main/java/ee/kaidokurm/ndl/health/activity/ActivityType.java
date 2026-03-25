package ee.kaidokurm.ndl.health.activity;

import java.util.Arrays;
import java.util.Locale;

public enum ActivityType {
    WALKING,
    RUNNING,
    CYCLING,
    STRENGTH,
    CARDIO,
    SPORTS,
    MOBILITY,
    YOGA,
    OTHER;

    public static ActivityType fromInput(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("activityType is required");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return Arrays.stream(values())
                .filter(v -> v.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid activityType. Allowed: " + String.join(", ",
                                Arrays.stream(values()).map(v -> v.name().toLowerCase(Locale.ROOT)).toList())));
    }
}
