package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.entity.MenuEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Menu entity/VO conversion
 */
@Mapper(componentModel = "spring")
public interface MenuMapper {

    /**
     * Entity to VO
     */
    MenuVO toVO(MenuEntity entity);

    /**
     * VO to Entity
     */
    MenuEntity toEntity(MenuVO vo);

    /**
     * Entity list to VO list
     */
    List<MenuVO> toVOList(List<MenuEntity> entities);
}
