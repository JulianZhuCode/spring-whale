package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.entity.MenuEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu entity/VO converter
 */
@Component
public class MenuMapper {

    /**
     * Entity to VO
     */
    public MenuVO toVO(MenuEntity entity) {
        if (entity == null) {
            return null;
        }
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO to Entity
     */
    public MenuEntity toEntity(MenuVO vo) {
        if (vo == null) {
            return null;
        }
        MenuEntity entity = new MenuEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity list to VO list
     */
    public List<MenuVO> toVOList(List<MenuEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
