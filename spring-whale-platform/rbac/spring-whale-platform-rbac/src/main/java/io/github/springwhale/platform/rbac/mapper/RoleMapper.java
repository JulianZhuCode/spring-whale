package io.github.springwhale.platform.rbac.mapper;

import io.github.springwhale.platform.rbac.dto.vo.RoleVO;
import io.github.springwhale.platform.rbac.entity.RoleEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role entity/VO converter
 */
@Component
public class RoleMapper {

    /**
     * Entity to VO
     */
    public RoleVO toVO(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO to Entity
     */
    public RoleEntity toEntity(RoleVO vo) {
        if (vo == null) {
            return null;
        }
        RoleEntity entity = new RoleEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity list to VO list
     */
    public List<RoleVO> toVOList(List<RoleEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
