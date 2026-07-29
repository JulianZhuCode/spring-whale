package io.github.springwhale.task.mapper;

import io.github.springwhale.task.dto.vo.TaskVO;
import io.github.springwhale.task.entity.TaskBatchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Task entity/VO conversion.
 * <p>
 * Only handles direct field mapping. Computed fields
 * (progress, estimatedRemainingSeconds, labels) should
 * be populated by the service layer via {@code enrichVO}.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "taskTypeLabel", ignore = true)
    @Mapping(target = "statusLabel", ignore = true)
    @Mapping(target = "progress", ignore = true)
    @Mapping(target = "estimatedRemainingSeconds", ignore = true)
    @Mapping(target = "createByName", ignore = true)
    TaskVO toVO(TaskBatchEntity entity);

    List<TaskVO> toVOList(List<TaskBatchEntity> entities);
}
