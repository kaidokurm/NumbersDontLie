package ee.kaidokurm.ndl.health.wellness;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/**
 * Weekly wellness trend point for charting wellness evolution.
 */
public class WellnessHistoryPointResponse {

    @JsonProperty("week_start")
    private LocalDate weekStart;

    @JsonProperty("week_end")
    private LocalDate weekEnd;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("bmi_score")
    private Integer bmiScore;

    @JsonProperty("activity_score")
    private Integer activityScore;

    @JsonProperty("goal_score")
    private Integer goalScore;

    @JsonProperty("habits_score")
    private Integer habitsScore;

    public WellnessHistoryPointResponse() {
    }

    public WellnessHistoryPointResponse(LocalDate weekStart, LocalDate weekEnd, Integer score, Integer bmiScore,
            Integer activityScore, Integer goalScore, Integer habitsScore) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.score = score;
        this.bmiScore = bmiScore;
        this.activityScore = activityScore;
        this.goalScore = goalScore;
        this.habitsScore = habitsScore;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getBmiScore() {
        return bmiScore;
    }

    public void setBmiScore(Integer bmiScore) {
        this.bmiScore = bmiScore;
    }

    public Integer getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(Integer activityScore) {
        this.activityScore = activityScore;
    }

    public Integer getGoalScore() {
        return goalScore;
    }

    public void setGoalScore(Integer goalScore) {
        this.goalScore = goalScore;
    }

    public Integer getHabitsScore() {
        return habitsScore;
    }

    public void setHabitsScore(Integer habitsScore) {
        this.habitsScore = habitsScore;
    }
}
