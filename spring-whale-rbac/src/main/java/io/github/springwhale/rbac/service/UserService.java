package io.github.springwhale.rbac.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.UserRequest;
import io.github.springwhale.rbac.dto.vo.UserVO;
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
 * User service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Find all users with pagination
     */
    public Page<UserVO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toVO);
    }

    /**
     * Find user by ID
     */
    public Optional<UserVO> findById(Integer id) {
        return userRepository.findById(id).map(userMapper::toVO);
    }

    /**
     * Find by exact username
     */
    public Optional<UserVO> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toVO);
    }

    /**
     * Find by exact email
     */
    public Optional<UserVO> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toVO);
    }

    /**
     * Find by exact phone
     */
    public Optional<UserVO> findByPhone(String phone) {
        return userRepository.findByPhone(phone).map(userMapper::toVO);
    }

    /**
     * Search users by username or real name (fuzzy)
     */
    public List<UserVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<UserEntity> byUsername = userRepository.findByUsernameContaining(keyword);
        List<UserEntity> byRealName = userRepository.findByRealNameContaining(keyword);
        // Merge and deduplicate
        return userMapper.toVOList(byUsername.stream()
                .filter(u -> !byRealName.contains(u))
                .toList());
    }

    /**
     * Find users by department ID
     */
    public List<UserVO> findByGroupId(Integer groupId) {
        return userMapper.toVOList(userRepository.findByGroupId(groupId));
    }

    /**
     * Find by status
     */
    public List<UserVO> findByStatus(Integer status) {
        return userMapper.toVOList(userRepository.findByStatus(status));
    }

    /**
     * Create user
     */
    @Transactional
    public UserVO create(UserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setUsername(request.getUsername());
        entity.setRealName(request.getRealName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setAvatar(request.getAvatar());
        entity.setStatus(request.getStatus());
        entity.setGroupId(request.getGroupId());
        return userMapper.toVO(userRepository.save(entity));
    }

    /**
     * Update user
     */
    @Transactional
    public UserVO update(Integer id, UserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found, ID: " + id));

        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus());
        user.setGroupId(request.getGroupId());

        return userMapper.toVO(userRepository.save(user));
    }

    /**
     * Delete user
     */
    @Transactional
    public void delete(Integer id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found, ID: " + id));
        userRepository.delete(user);
    }
}
