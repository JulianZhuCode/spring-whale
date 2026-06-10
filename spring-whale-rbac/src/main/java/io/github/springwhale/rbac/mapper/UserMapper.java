package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for User entity/VO conversion
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Entity to VO
     */
    UserVO toVO(UserEntity entity);

    /**
     * VO to Entity
     */
    UserEntity toEntity(UserVO vo);

    /**
     * Entity list to VO list
     */
    List<UserVO> toVOList(List<UserEntity> entities);
}
