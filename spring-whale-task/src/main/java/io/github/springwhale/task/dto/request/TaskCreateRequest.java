package io.github.springwhale.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Request to create a new batch task.
 */
@Data
public class TaskCreateRequest {

    @NotBlank(message = "Task type is required")
    private String taskType;

    private Map<String, Object> params;

    /**
     * Maximum number of concurrent items to process.
     * If not specified, uses the system default.
     */
    private Integer concurrency;
}
