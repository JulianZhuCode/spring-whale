package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.RoleMenuVO;
import io.github.springwhale.rbac.entity.RoleMenuEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色菜单关联实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface RoleMenuMapper {

    RoleMenuMapper INSTANCE = Mappers.getMapper(RoleMenuMapper.class);

    /**
     * Entity → VO
     */
    RoleMenuVO toVO(RoleMenuEntity entity);

    /**
     * VO → Entity
     */
    RoleMenuEntity toEntity(RoleMenuVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<RoleMenuVO> toVOList(List<RoleMenuEntity> entities);
}
