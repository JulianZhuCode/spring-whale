package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.entity.MenuEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 菜单实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface MenuMapper {

    /**
     * Entity → VO
     */
    MenuVO toVO(MenuEntity entity);

    /**
     * VO → Entity
     */
    MenuEntity toEntity(MenuVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<MenuVO> toVOList(List<MenuEntity> entities);
}
