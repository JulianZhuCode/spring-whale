package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.RoleDTO;
import io.github.springwhale.rbac.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色实体与DTO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    /**
     * Entity → DTO
     */
    RoleDTO toDTO(RoleEntity entity);

    /**
     * DTO → Entity
     */
    RoleEntity toEntity(RoleDTO dto);

    /**
     * Entity列表 → DTO列表
     */
    List<RoleDTO> toDTOList(List<RoleEntity> entities);
}
