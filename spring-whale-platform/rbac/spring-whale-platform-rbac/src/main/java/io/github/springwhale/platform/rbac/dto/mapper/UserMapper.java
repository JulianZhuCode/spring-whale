package io.github.springwhale.platform.rbac.dto.mapper;

import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dto.vo.UserVO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User entity/VO converter
 */
public class UserMapper {

    private UserMapper() {
    }

    /**
     * Entity to VO
     */
    public static UserVO toVO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO to Entity
     */
    public static UserEntity toEntity(UserVO vo) {
        if (vo == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity list to VO list
     */
    public static List<UserVO> toVOList(List<UserEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(UserMapper::toVO)
                .collect(Collectors.toList());
    }
}