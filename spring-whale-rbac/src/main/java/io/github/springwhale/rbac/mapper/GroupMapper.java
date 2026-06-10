package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.GroupVO;
import io.github.springwhale.rbac.entity.GroupEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for Group entity/VO conversion
 */
@Mapper(componentModel = "spring")
public interface GroupMapper {

    /**
     * Entity to VO
     */
    GroupVO toVO(GroupEntity entity);

    /**
     * VO to Entity
     */
    GroupEntity toEntity(GroupVO vo);

    /**
     * Entity list to VO list
     */
    List<GroupVO> toVOList(List<GroupEntity> entities);
}
