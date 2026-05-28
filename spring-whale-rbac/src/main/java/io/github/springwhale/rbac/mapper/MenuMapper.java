package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.MenuDTO;
import io.github.springwhale.rbac.entity.MenuEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 菜单实体与DTO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface MenuMapper {

    MenuMapper INSTANCE = Mappers.getMapper(MenuMapper.class);

    /**
     * Entity → DTO
     */
    MenuDTO toDTO(MenuEntity entity);

    /**
     * DTO → Entity
     */
    MenuEntity toEntity(MenuDTO dto);

    /**
     * Entity列表 → DTO列表
     */
    List<MenuDTO> toDTOList(List<MenuEntity> entities);
}
