package io.github.springwhale.platform.rbac.dto.mapper;

import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dto.vo.GroupVO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Group entity/VO converter
 */
public class GroupMapper {

    /**
     * Entity to VO
     */
    public GroupVO toVO(GroupEntity entity) {
        if (entity == null) {
            return null;
        }
        GroupVO vo = new GroupVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO to Entity
     */
    public GroupEntity toEntity(GroupVO vo) {
        if (vo == null) {
            return null;
        }
        GroupEntity entity = new GroupEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity list to VO list
     */
    public List<GroupVO> toVOList(List<GroupEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}