package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.RoleMenuVO;
import io.github.springwhale.rbac.entity.RoleMenuEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for RoleMenu entity/VO conversion
 */
@Mapper(componentModel = "spring")
public interface RoleMenuMapper {

    /**
     * Entity to VO
     */
    RoleMenuVO toVO(RoleMenuEntity entity);

    /**
     * VO to Entity
     */
    RoleMenuEntity toEntity(RoleMenuVO vo);

    /**
     * Entity list to VO list
     */
    List<RoleMenuVO> toVOList(List<RoleMenuEntity> entities);
}
