package ee.kaidokurm.ndl.health.wellness;

/**
 * Response DTO for wellness score information.
 * 
 * Contains the numeric score and human-readable description.
 */
public class WellnessScoreResponse {

    private Integer score;
    private String description;

    public WellnessScoreResponse(Integer score, String description) {
        this.score = score;
        this.description = description;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
