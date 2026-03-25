package ee.kaidokurm.ndl.common.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record ApiSuccess<T>(
        T data,
        @JsonProperty("timestamp") OffsetDateTime timestamp) {

    public static <T> ApiSuccess<T> of(T data) {
        return new ApiSuccess<>(data, OffsetDateTime.now());
    }
}
