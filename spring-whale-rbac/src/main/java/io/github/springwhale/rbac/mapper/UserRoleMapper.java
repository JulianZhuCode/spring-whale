package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.UserRoleVO;
import io.github.springwhale.rbac.entity.UserRoleEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for UserRole entity/VO conversion
 */
@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    /**
     * Entity to VO
     */
    UserRoleVO toVO(UserRoleEntity entity);

    /**
     * VO to Entity
     */
    UserRoleEntity toEntity(UserRoleVO vo);

    /**
     * Entity list to VO list
     */
    List<UserRoleVO> toVOList(List<UserRoleEntity> entities);
}
