package ee.kaidokurm.ndl.health.activity;

import java.util.Arrays;
import java.util.Locale;

public enum ActivityIntensity {
    LOW,
    MEDIUM,
    HIGH;

    public static ActivityIntensity fromInput(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("intensity is required");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return Arrays.stream(values())
                .filter(v -> v.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid intensity. Allowed: low, medium, high"));
    }
}
