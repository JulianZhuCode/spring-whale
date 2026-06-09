package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.entity.RoleEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 角色实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Entity → VO
     */
    RoleVO toVO(RoleEntity entity);

    /**
     * VO → Entity
     */
    RoleEntity toEntity(RoleVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<RoleVO> toVOList(List<RoleEntity> entities);
}
