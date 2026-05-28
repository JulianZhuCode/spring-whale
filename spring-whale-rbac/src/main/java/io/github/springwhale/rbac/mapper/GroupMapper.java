package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.GroupDTO;
import io.github.springwhale.rbac.entity.GroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 分组实体与DTO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    /**
     * Entity → DTO
     */
    GroupDTO toDTO(GroupEntity entity);

    /**
     * DTO → Entity
     */
    GroupEntity toEntity(GroupDTO dto);

    /**
     * Entity列表 → DTO列表
     */
    List<GroupDTO> toDTOList(List<GroupEntity> entities);
}
