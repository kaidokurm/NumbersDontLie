package ee.kaidokurm.ndl.health.goal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ee.kaidokurm.ndl.auth.user.UserService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for goal progress tracking endpoints.
 * 
 * Provides endpoints for: - Retrieving current goal progress (percentage,
 * on-track status, milestones) - Accessing historical progress data for trends
 * - Recording new progress data points
 */
@RestController
@Tag(name = "Goal Progress", description = "Track progress towards health goals")
public class GoalProgressController {

    private final UserService userService;
    private final GoalProgressService goalProgressService;
    private final GoalRepository goalRepository;

    public GoalProgressController(UserService userService, GoalProgressService goalProgressService,
            GoalRepository goalRepository) {
        this.userService = userService;
        this.goalProgressService = goalProgressService;
        this.goalRepository = goalRepository;
    }

    /**
     * Get the current progress for a specific goal.
     * 
     * Returns the latest progress record including percentage, on-track status,
     * days remaining, and milestone tracking.
     * 
     * @param goalId the ID of the goal
     * @return current progress, or 404 if goal not found or no progress recorded
     */
    @GetMapping("/api/goals/{id}/progress")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current goal progress", description = "Returns the latest progress record for a goal")
    public ResponseEntity<GoalProgressResponse> getCurrentProgress(@PathVariable("id") UUID goalId,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);

        // Verify goal exists and belongs to user
        var goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!goalOpt.get().getUserId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        var latestProgress = goalProgressService.getLatestProgress(goalId);
        if (latestProgress.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new GoalProgressResponse(latestProgress.get()));
    }

    /**
     * Get historical progress records for a goal.
     * 
     * Returns the last 30 progress records ordered by most recent first, useful for
     * trend analysis and visualization.
     * 
     * @param goalId the ID of the goal
     * @return list of historical progress records (last 30)
     */
    @GetMapping("/api/goals/{id}/progress/history")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get progress history", description = "Returns historical progress records for a goal (last 30)")
    public ResponseEntity<List<GoalProgressResponse>> getProgressHistory(@PathVariable("id") UUID goalId,
            JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);

        // Verify goal exists and belongs to user
        var goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isEmpty() || !goalOpt.get().getUserId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        var progressHistory = goalProgressService.getProgressHistory(goalId);
        var response = progressHistory.stream().map(GoalProgressResponse::new).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Record a new progress point for a goal.
     * 
     * Calculates progress percentage, determines on-track status, checks for
     * milestone completions, and stores the snapshot.
     * 
     * @param goalId       the ID of the goal
     * @param currentValue the current metric value (e.g., weight in kg, activity
     *                     days)
     * @return the recorded progress
     */
    @PostMapping("/api/goals/{id}/progress/record")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Record goal progress", description = "Records a new progress point and calculates all metrics")
    public ResponseEntity<GoalProgressResponse> recordProgress(@PathVariable("id") UUID goalId,
            @RequestParam BigDecimal currentValue, JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);

        // Verify goal exists and belongs to user
        var goalOpt = goalRepository.findById(goalId);
        if (goalOpt.isEmpty() || !goalOpt.get().getUserId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        var recorded = goalProgressService.recordProgress(goalId, currentValue);
        if (recorded == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new GoalProgressResponse(recorded));
    }
}
