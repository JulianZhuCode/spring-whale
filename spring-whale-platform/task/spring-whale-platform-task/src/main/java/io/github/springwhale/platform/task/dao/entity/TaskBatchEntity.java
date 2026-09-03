package io.github.springwhale.platform.task.dao.entity;

import io.github.springwhale.database.BaseEntity;
import io.github.springwhale.platform.task.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Batch task entity - tracks the lifecycle of a batch operation.
 */
@Entity
@Table(name = "task_batch")
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskBatchEntity extends BaseEntity {

    @Column(nullable = false)
    private String taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    private Integer totalCount = 0;

    private Integer successCount = 0;

    private Integer failCount = 0;

    private Integer skippedCount = 0;

    private String params;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}