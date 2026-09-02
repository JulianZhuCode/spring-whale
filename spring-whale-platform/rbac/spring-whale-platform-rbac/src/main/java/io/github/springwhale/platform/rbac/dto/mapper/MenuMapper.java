package io.github.springwhale.platform.rbac.dto.mapper;

import io.github.springwhale.platform.rbac.dao.entity.MenuEntity;
import io.github.springwhale.platform.rbac.dto.vo.MenuVO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu entity/VO converter
 */
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