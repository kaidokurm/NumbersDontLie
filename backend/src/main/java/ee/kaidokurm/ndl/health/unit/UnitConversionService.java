package ee.kaidokurm.ndl.health.unit;

import org.springframework.stereotype.Service;

@Service
public class UnitConversionService {

    private static final double INCH_TO_CM = 2.54;
    private static final double POUND_TO_KG = 0.45359237;

    public int toCentimeters(double height, String unit) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
        String normalized = normalizeUnit(unit, "cm");
        double cm = switch (normalized) {
            case "cm" -> height;
            case "in", "inch", "inches" -> height * INCH_TO_CM;
            default -> throw new IllegalArgumentException("Unsupported height unit: " + unit);
        };
        return (int) Math.round(cm);
    }

    public double toKilograms(double weight, String unit) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        String normalized = normalizeUnit(unit, "kg");
        return switch (normalized) {
            case "kg", "kilogram", "kilograms" -> weight;
            case "lb", "lbs", "pound", "pounds" -> weight * POUND_TO_KG;
            default -> throw new IllegalArgumentException("Unsupported weight unit: " + unit);
        };
    }

    private String normalizeUnit(String unit, String fallback) {
        if (unit == null || unit.isBlank()) {
            return fallback;
        }
        return unit.trim().toLowerCase();
    }
}
