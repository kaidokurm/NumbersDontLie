package ee.kaidokurm.ndl.health.goal;

/**
 * Enumeration of different types of health goals.
 * Goal types are a small, stable set and are referenced in test scripts.
 * Activity-level values are the ones that tend to change frequently; goal types
 * usually do not.
 */
public enum GoalType {
    WEIGHT_LOSS,
    MAINTAIN_WEIGHT,
    WEIGHT_GAIN,
    IMPROVE_FITNESS,
    BUILD_MUSCLE,
    ENHANCE_ENDURANCE,
    IMPROVE_FLEXIBILITY,
    REDUCE_STRESS,
    BETTER_SLEEP
}
