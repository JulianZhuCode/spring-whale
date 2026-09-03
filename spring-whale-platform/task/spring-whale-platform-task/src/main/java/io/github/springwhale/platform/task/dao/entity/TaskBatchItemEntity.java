package io.github.springwhale.platform.task.dao.entity;

import io.github.springwhale.database.BaseEntity;
import io.github.springwhale.platform.task.enums.TaskItemStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Batch task item entity - tracks individual item status within a batch task.
 */
@Entity
@Table(name = "task_batch_item")
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskBatchItemEntity extends BaseEntity {

    @Column(nullable = false)
    private Integer taskId;

    @Column(nullable = false)
    private String itemKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskItemStatus status = TaskItemStatus.PENDING;

    private String errorMessage;

    private Integer retryCount = 0;
}