package io.github.springwhale.platform.rbac.mapper;

import io.github.springwhale.platform.rbac.dto.vo.UserRoleVO;
import io.github.springwhale.platform.rbac.entity.UserRoleEntity;
import org.springframework.beans.BeanUtils;


import java.util.List;
import java.util.stream.Collectors;

/**
 * UserRole entity/VO converter
 */
public class UserRoleMapper {

    /**
     * Entity to VO
     */
    public UserRoleVO toVO(UserRoleEntity entity) {
        if (entity == null) {
            return null;
        }
        UserRoleVO vo = new UserRoleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO to Entity
     */
    public UserRoleEntity toEntity(UserRoleVO vo) {
        if (vo == null) {
            return null;
        }
        UserRoleEntity entity = new UserRoleEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity list to VO list
     */
    public List<UserRoleVO> toVOList(List<UserRoleEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}