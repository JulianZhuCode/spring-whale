package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.UserRoleVO;
import io.github.springwhale.rbac.entity.UserRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户角色关联实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    UserRoleMapper INSTANCE = Mappers.getMapper(UserRoleMapper.class);

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
