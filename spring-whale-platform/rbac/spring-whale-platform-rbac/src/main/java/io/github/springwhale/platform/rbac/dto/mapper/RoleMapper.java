package io.github.springwhale.platform.rbac.dto.mapper;

import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dto.vo.RoleVO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role entity/VO converter
 */
public class RoleMapper {

    private RoleMapper() {
    }

    /**
     * Entity to VO
     */
    public static RoleVO toVO(RoleEntity entity) {
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
    public static RoleEntity toEntity(RoleVO vo) {
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
    public static List<RoleVO> toVOList(List<RoleEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(RoleMapper::toVO)
                .collect(Collectors.toList());
    }
}