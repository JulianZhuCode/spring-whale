package io.github.springwhale.platform.task.dao.repository;

import io.github.springwhale.platform.task.dao.entity.TaskBatchItemEntity;
import io.github.springwhale.platform.task.enums.TaskItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskBatchItemRepository extends JpaRepository<TaskBatchItemEntity, Long>, JpaSpecificationExecutor<TaskBatchItemEntity> {

    List<TaskBatchItemEntity> findByTaskIdOrderByIdAsc(Long taskId);

    List<TaskBatchItemEntity> findByTaskIdAndStatusOrderByIdAsc(Long taskId, TaskItemStatus status);

    List<TaskBatchItemEntity> findByTaskIdAndStatus(Long taskId, TaskItemStatus status, Pageable pageable);

    long countByTaskIdAndStatus(Long taskId, TaskItemStatus status);

    long countByTaskId(Long taskId);

    void deleteByTaskId(Long taskId);

    void deleteByTaskIdAndStatusIn(Long taskId, List<TaskItemStatus> statuses);
}