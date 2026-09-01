package io.github.springwhale.platform.rbac.mapper;

import io.github.springwhale.platform.rbac.dto.vo.RoleMenuVO;
import io.github.springwhale.platform.rbac.entity.RoleMenuEntity;
import org.springframework.beans.BeanUtils;


import java.util.List;
import java.util.stream.Collectors;

/**
 * RoleMenu entity/VO converter
 */
public class RoleMenuMapper {

    /**
     * Entity to VO
     */
    public RoleMenuVO toVO(RoleMenuEntity entity) {
        if (entity == null) {
            return null;
        }
        RoleMenuVO vo = new RoleMenuVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO to Entity
     */
    public RoleMenuEntity toEntity(RoleMenuVO vo) {
        if (vo == null) {
            return null;
        }
        RoleMenuEntity entity = new RoleMenuEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity list to VO list
     */
    public List<RoleMenuVO> toVOList(List<RoleMenuEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}