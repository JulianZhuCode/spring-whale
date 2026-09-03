package io.github.springwhale.platform.task.dao.repository;

import io.github.springwhale.platform.task.dao.entity.TaskBatchItemEntity;
import io.github.springwhale.platform.task.enums.TaskItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskBatchItemRepository extends JpaRepository<TaskBatchItemEntity, Integer>, JpaSpecificationExecutor<TaskBatchItemEntity> {

    List<TaskBatchItemEntity> findByTaskIdOrderByIdAsc(Integer taskId);

    List<TaskBatchItemEntity> findByTaskIdAndStatusOrderByIdAsc(Integer taskId, TaskItemStatus status);

    List<TaskBatchItemEntity> findByTaskIdAndStatus(Integer taskId, TaskItemStatus status, Pageable pageable);

    long countByTaskIdAndStatus(Integer taskId, TaskItemStatus status);

    long countByTaskId(Integer taskId);

    void deleteByTaskId(Integer taskId);

    void deleteByTaskIdAndStatusIn(Integer taskId, List<TaskItemStatus> statuses);
}