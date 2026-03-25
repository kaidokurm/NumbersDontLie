package ee.kaidokurm.ndl.common.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(OffsetDateTime timestamp, int status, String error, String errorCode, String message,
        String path,
        List<FieldError> fieldErrors) {
    public record FieldError(String field, String message) {
    }
}
