package io.github.springwhale.platform.task.mapper;

import io.github.springwhale.platform.task.dao.entity.TaskBatchEntity;
import io.github.springwhale.platform.task.dto.vo.TaskVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual mapper for Task entity/VO conversion.
 * <p>
 * Only handles direct field mapping. Computed fields
 * (progress, estimatedRemainingSeconds, labels) should
 * be populated by the service layer via {@code enrichVO}.
 */
public class TaskMapper {

    public TaskVO toVO(TaskBatchEntity entity) {
        if (entity == null) {
            return null;
        }
        TaskVO vo = new TaskVO();
        vo.setId(entity.getId());
        vo.setTaskType(entity.getTaskType());
        vo.setStatus(entity.getStatus());
        vo.setTotalCount(entity.getTotalCount());
        vo.setSuccessCount(entity.getSuccessCount());
        vo.setFailCount(entity.getFailCount());
        vo.setSkippedCount(entity.getSkippedCount());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setParams(entity.getParams());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setCreateBy(entity.getCreateBy());
        return vo;
    }

    public List<TaskVO> toVOList(List<TaskBatchEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}