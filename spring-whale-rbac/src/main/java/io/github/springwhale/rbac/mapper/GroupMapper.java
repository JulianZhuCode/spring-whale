package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.GroupVO;
import io.github.springwhale.rbac.entity.GroupEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 分组实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

    /**
     * Entity → VO
     */
    GroupVO toVO(GroupEntity entity);

    /**
     * VO → Entity
     */
    GroupEntity toEntity(GroupVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<GroupVO> toVOList(List<GroupEntity> entities);
}
