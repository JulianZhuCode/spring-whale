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
@Table(name = "task_batch", indexes = {
        @Index(name = "idx_task_type", columnList = "taskType"),
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_create_time", columnList = "createTime")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskBatchEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status = TaskStatus.PENDING;

    @Column
    private Integer totalCount = 0;

    @Column
    private Integer successCount = 0;

    @Column
    private Integer failCount = 0;

    @Column
    private Integer skippedCount = 0;

    @Column(columnDefinition = "text")
    private String params;

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
