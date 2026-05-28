package io.github.springwhale.rbac.service;

import io.github.springwhale.rbac.dto.UserDTO;
import io.github.springwhale.rbac.entity.UserEntity;
import io.github.springwhale.rbac.mapper.UserMapper;
import io.github.springwhale.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 分页查询所有用户
     */
    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    /**
     * 根据ID查询用户
     */
    public Optional<UserDTO> findById(Integer id) {
        return userRepository.findById(id).map(userMapper::toDTO);
    }

    /**
     * 根据用户名精确查询
     */
    public Optional<UserDTO> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDTO);
    }

    /**
     * 根据邮箱精确查询
     */
    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toDTO);
    }

    /**
     * 根据手机号精确查询
     */
    public Optional<UserDTO> findByPhone(String phone) {
        return userRepository.findByPhone(phone).map(userMapper::toDTO);
    }

    /**
     * 搜索用户（支持用户名或真实姓名模糊查询）
     */
    public List<UserDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<UserEntity> byUsername = userRepository.findByUsernameContaining(keyword);
        List<UserEntity> byRealName = userRepository.findByRealNameContaining(keyword);
        // 合并去重
        return userMapper.toDTOList(byUsername.stream()
                .filter(u -> !byRealName.contains(u))
                .toList());
    }

    /**
     * 根据部门ID查询
     */
    public List<UserDTO> findByGroupId(Integer groupId) {
        return userMapper.toDTOList(userRepository.findByGroupId(groupId));
    }

    /**
     * 根据状态查询
     */
    public List<UserDTO> findByStatus(Integer status) {
        return userMapper.toDTOList(userRepository.findByStatus(status));
    }

    /**
     * 创建用户
     */
    @Transactional
    public UserDTO create(UserDTO userDTO) {
        UserEntity entity = userMapper.toEntity(userDTO);
        return userMapper.toDTO(userRepository.save(entity));
    }

    /**
     * 更新用户
     */
    @Transactional
    public UserDTO update(Integer id, UserDTO userDTO) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));

        user.setUsername(userDTO.getUsername());
        user.setRealName(userDTO.getRealName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        user.setStatus(userDTO.getStatus());
        user.setGroupId(userDTO.getGroupId());

        return userMapper.toDTO(userRepository.save(user));
    }

    /**
     * 删除用户
     */
    @Transactional
    public void delete(Integer id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
        userRepository.delete(user);
    }
}
