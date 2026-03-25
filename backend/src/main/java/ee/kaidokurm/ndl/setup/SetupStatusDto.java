package ee.kaidokurm.ndl.setup;

import java.util.List;

/**
 * Data Transfer Object for setup status. Tells the frontend what parts of the
 * setup are complete.
 *
 * Example: { isComplete: false, missing: ["profile", "goal"] }
 */
public class SetupStatusDto {
    private boolean isComplete;
    private List<String> missing;

    public SetupStatusDto(boolean isComplete, List<String> missing) {
        this.isComplete = isComplete;
        this.missing = missing;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public List<String> getMissing() {
        return missing;
    }
}
