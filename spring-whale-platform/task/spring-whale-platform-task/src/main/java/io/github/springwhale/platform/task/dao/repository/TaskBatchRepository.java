package io.github.springwhale.platform.task.dao.repository;

import io.github.springwhale.platform.task.dao.entity.TaskBatchEntity;
import io.github.springwhale.platform.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskBatchRepository extends JpaRepository<TaskBatchEntity, Long>, JpaSpecificationExecutor<TaskBatchEntity> {

    List<TaskBatchEntity> findByStatusIn(List<TaskStatus> statuses);

    Page<TaskBatchEntity> findByTaskType(String taskType, Pageable pageable);

    Page<TaskBatchEntity> findByStatus(TaskStatus status, Pageable pageable);

    boolean existsByIdAndStatus(Long id, TaskStatus status);

    List<TaskBatchEntity> findByTaskTypeAndStatusNotIn(String taskType, List<TaskStatus> statuses);
}