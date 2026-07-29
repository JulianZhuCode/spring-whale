package io.github.springwhale.task.dto.vo;

import io.github.springwhale.task.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * View object for batch task.
 */
@Data
public class TaskVO {

    private Integer id;

    private String taskType;

    private String taskTypeLabel;

    private TaskStatus status;

    private String statusLabel;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    private Integer skippedCount;

    /**
     * Progress percentage (0-100).
     */
    private Integer progress;

    private String errorMessage;

    private String params;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer createBy;

    private String createByName;

    /**
     * Estimated seconds remaining (rough estimate, null if not running).
     */
    private Long estimatedRemainingSeconds;
}
