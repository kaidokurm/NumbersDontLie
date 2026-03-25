package ee.kaidokurm.ndl.common.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for AI insight response. Contains AI-generated wellness insights
 * including recommendations and insights.
 */
public class InsightResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("goal_id")
    private UUID goalId;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("recommendations")
    private String[] recommendations;

    @JsonProperty("reflection_question")
    private String reflectionQuestion;

    @JsonProperty("generated_at")
    private OffsetDateTime generatedAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    public InsightResponse() {
    }

    public InsightResponse(UUID id, UUID userId, UUID goalId, String summary, String[] recommendations,
            String reflectionQuestion, OffsetDateTime generatedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.goalId = goalId;
        this.summary = summary;
        this.recommendations = recommendations;
        this.reflectionQuestion = reflectionQuestion;
        this.generatedAt = generatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public void setGoalId(UUID goalId) {
        this.goalId = goalId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String[] getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String[] recommendations) {
        this.recommendations = recommendations;
    }

    public String getReflectionQuestion() {
        return reflectionQuestion;
    }

    public void setReflectionQuestion(String reflectionQuestion) {
        this.reflectionQuestion = reflectionQuestion;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
