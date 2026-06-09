package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Entity → VO
     */
    UserVO toVO(UserEntity entity);

    /**
     * VO → Entity
     */
    UserEntity toEntity(UserVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<UserVO> toVOList(List<UserEntity> entities);
}
