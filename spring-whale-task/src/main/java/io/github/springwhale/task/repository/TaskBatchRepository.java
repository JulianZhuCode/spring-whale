package io.github.springwhale.task.repository;

import io.github.springwhale.task.entity.TaskBatchEntity;
import io.github.springwhale.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskBatchRepository extends JpaRepository<TaskBatchEntity, Integer>, JpaSpecificationExecutor<TaskBatchEntity> {

    List<TaskBatchEntity> findByStatusIn(List<TaskStatus> statuses);

    Page<TaskBatchEntity> findByTaskType(String taskType, Pageable pageable);

    Page<TaskBatchEntity> findByStatus(TaskStatus status, Pageable pageable);

    boolean existsByIdAndStatus(Integer id, TaskStatus status);

    List<TaskBatchEntity> findByTaskTypeAndStatusNotIn(String taskType, List<TaskStatus> statuses);
}
