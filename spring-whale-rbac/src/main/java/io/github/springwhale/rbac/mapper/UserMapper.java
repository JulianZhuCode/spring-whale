package io.github.springwhale.rbac.mapper;

import io.github.springwhale.rbac.dto.UserDTO;
import io.github.springwhale.rbac.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户实体与DTO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Entity → DTO
     */
    UserDTO toDTO(UserEntity entity);

    /**
     * DTO → Entity
     */
    UserEntity toEntity(UserDTO dto);

    /**
     * Entity列表 → DTO列表
     */
    List<UserDTO> toDTOList(List<UserEntity> entities);
}
