package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.entity.RoleEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Role entity/VO conversion
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * Entity to VO
     */
    RoleVO toVO(RoleEntity entity);

    /**
     * VO to Entity
     */
    RoleEntity toEntity(RoleVO vo);

    /**
     * Entity list to VO list
     */
    List<RoleVO> toVOList(List<RoleEntity> entities);
}
