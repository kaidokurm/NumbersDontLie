package ee.kaidokurm.ndl.health.profile;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating BMI and determining BMI classification.
 * 
 * BMI = weight (kg) / height² (m²)
 * 
 * Classifications: - Underweight: BMI < 18.5 - Normal weight: BMI 18.5 - 24.9 -
 * Overweight: BMI 25 - 29.9 - Obese: BMI >= 30
 */
public class BMICalculator {

    /**
     * Calculate BMI from weight (kg) and height (cm). Returns BMI value rounded to
     * 2 decimal places.
     */
    public static BigDecimal calculateBMI(double weightKg, int heightCm) {
        if (weightKg <= 0 || heightCm <= 0) {
            throw new IllegalArgumentException("Weight and height must be positive");
        }

        // Convert height from cm to m
        double heightM = heightCm / 100.0;

        // Calculate BMI
        double bmi = weightKg / (heightM * heightM);

        // Round to 2 decimal places
        return BigDecimal.valueOf(bmi).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Classify BMI value into categories. Returns: "underweight", "normal",
     * "overweight", or "obese"
     */
    public static String classifyBMI(BigDecimal bmi) {
        if (bmi == null) {
            return null;
        }

        if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
            return "underweight";
        } else if (bmi.compareTo(new BigDecimal("25")) < 0) {
            return "normal";
        } else if (bmi.compareTo(new BigDecimal("30")) < 0) {
            return "overweight";
        } else {
            return "obese";
        }
    }

    /**
     * Get human-readable description of BMI classification.
     */
    public static String getBMIDescription(String classification) {
        if (classification == null) {
            return "Unknown";
        }

        return switch (classification) {
            case "underweight" -> "Underweight (BMI < 18.5)";
            case "normal" -> "Normal Weight (BMI 18.5 - 24.9)";
            case "overweight" -> "Overweight (BMI 25 - 29.9)";
            case "obese" -> "Obese (BMI >= 30)";
            default -> "Unknown";
        };
    }
}
