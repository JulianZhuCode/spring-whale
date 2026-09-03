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
@Table(name = "task_batch_item", indexes = {
        @Index(name = "idx_item_task_id", columnList = "taskId"),
        @Index(name = "idx_item_status", columnList = "status"),
        @Index(name = "idx_item_key", columnList = "itemKey")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskBatchItemEntity extends BaseEntity {

    @Column(nullable = false)
    private Integer taskId;

    @Column(nullable = false, length = 500)
    private String itemKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskItemStatus status = TaskItemStatus.PENDING;

    @Column(length = 2000)
    private String errorMessage;

    @Column
    private Integer retryCount = 0;
}
