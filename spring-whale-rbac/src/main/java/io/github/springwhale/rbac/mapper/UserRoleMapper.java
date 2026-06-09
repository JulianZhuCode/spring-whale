package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.UserRoleVO;
import io.github.springwhale.rbac.entity.UserRoleEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 用户角色关联实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    /**
     * Entity → VO
     */
    UserRoleVO toVO(UserRoleEntity entity);

    /**
     * VO → Entity
     */
    UserRoleEntity toEntity(UserRoleVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<UserRoleVO> toVOList(List<UserRoleEntity> entities);
}
