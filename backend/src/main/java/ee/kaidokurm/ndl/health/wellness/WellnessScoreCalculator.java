package ee.kaidokurm.ndl.health.wellness;

/**
 * Utility class for calculating wellness score components.
 * 
 * Wellness score is calculated as a weighted average of four components: - BMI
 * contribution (30%) - Activity level contribution (30%) - Goal progress
 * contribution (20%) - Health habits contribution (20%)
 * 
 * Each component is calculated as 0-100 and then weighted. Final wellness score
 * is also 0-100.
 */
public class WellnessScoreCalculator {

    /**
     * Calculate BMI component score (0-100) based on BMI classification.
     * 
     * Scoring logic: - Normal weight (18.5-24.9): 100 points (optimal) - Overweight
     * (25-29.9): 70 points (acceptable but needs improvement) - Obese (>=30): 40
     * points (significant health concern) - Underweight (<18.5): 50 points (below
     * optimal)
     * 
     * @param bmiClassification the BMI classification (e.g., "normal",
     *                          "overweight")
     * @return BMI score 0-100
     */
    public static int calculateBmiScore(String bmiClassification) {
        if (bmiClassification == null) {
            return 0; // No data available
        }

        return switch (bmiClassification.toLowerCase()) {
            case "normal" -> 100; // Healthy weight range
            case "overweight" -> 70; // Needs improvement but not critical
            case "obese" -> 40; // Significant health concern
            case "underweight" -> 50; // Below optimal but less critical
            default -> 0; // Unknown classification
        };
    }

    /**
     * Calculate activity level score (0-100) based on weekly activity frequency.
     * 
     * Scoring logic: - 0 days: 0 points (sedentary) - 1-2 days: 30 points (minimal
     * activity) - 3-4 days: 70 points (moderate activity) - 5-6 days: 85 points
     * (active) - 7 days: 100 points (very active)
     * 
     * @param weeklyActivityFrequency number of days active per week (0-7)
     * @return activity score 0-100
     */
    public static int calculateActivityScore(int weeklyActivityFrequency) {
        if (weeklyActivityFrequency < 0 || weeklyActivityFrequency > 7) {
            return 0; // Invalid input
        }

        return switch (weeklyActivityFrequency) {
            case 0 -> 0; // Sedentary
            case 1, 2 -> 30; // Minimal activity
            case 3, 4 -> 70; // Moderate activity
            case 5, 6 -> 85; // Active
            case 7 -> 100; // Very active
            default -> 0;
        };
    }

    /**
     * Calculate overall wellness score from component scores.
     * 
     * Formula: (bmi_score * 0.30) + (activity_score * 0.30) + (goal_score * 0.20) +
     * (habits_score * 0.20)
     * 
     * @param bmiScore      BMI component (0-100)
     * @param activityScore Activity level component (0-100)
     * @param goalScore     Goal progress component (0-100)
     * @param habitsScore   Health habits component (0-100)
     * @return weighted wellness score 0-100
     */
    public static int calculateOverallScore(int bmiScore, int activityScore, int goalScore, int habitsScore) {
        double weightedScore = (bmiScore * 0.30) + (activityScore * 0.30) + (goalScore * 0.20) + (habitsScore * 0.20);

        // Round to nearest integer and ensure within bounds
        int score = Math.round((float) weightedScore);
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Get text description for a wellness score.
     * 
     * @param score wellness score (0-100)
     * @return human-readable description
     */
    public static String getScoreDescription(int score) {
        if (score >= 90) {
            return "Excellent";
        } else if (score >= 80) {
            return "Very Good";
        } else if (score >= 70) {
            return "Good";
        } else if (score >= 60) {
            return "Fair";
        } else {
            return "Needs Improvement";
        }
    }
}
