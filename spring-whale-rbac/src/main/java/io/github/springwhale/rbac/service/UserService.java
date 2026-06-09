package io.github.springwhale.rbac.service;

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
    public Page<UserVO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toVO);
    }

    /**
     * 根据ID查询用户
     */
    public Optional<UserVO> findById(Integer id) {
        return userRepository.findById(id).map(userMapper::toVO);
    }

    /**
     * 根据用户名精确查询
     */
    public Optional<UserVO> findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toVO);
    }

    /**
     * 根据邮箱精确查询
     */
    public Optional<UserVO> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toVO);
    }

    /**
     * 根据手机号精确查询
     */
    public Optional<UserVO> findByPhone(String phone) {
        return userRepository.findByPhone(phone).map(userMapper::toVO);
    }

    /**
     * 搜索用户（支持用户名或真实姓名模糊查询）
     */
    public List<UserVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<UserEntity> byUsername = userRepository.findByUsernameContaining(keyword);
        List<UserEntity> byRealName = userRepository.findByRealNameContaining(keyword);
        // 合并去重
        return userMapper.toVOList(byUsername.stream()
                .filter(u -> !byRealName.contains(u))
                .toList());
    }

    /**
     * 根据部门ID查询
     */
    public List<UserVO> findByGroupId(Integer groupId) {
        return userMapper.toVOList(userRepository.findByGroupId(groupId));
    }

    /**
     * 根据状态查询
     */
    public List<UserVO> findByStatus(Integer status) {
        return userMapper.toVOList(userRepository.findByStatus(status));
    }

    /**
     * 创建用户
     */
    @Transactional
    public UserVO create(UserVO userVO) {
        UserEntity entity = userMapper.toEntity(userVO);
        return userMapper.toVO(userRepository.save(entity));
    }

    /**
     * 更新用户
     */
    @Transactional
    public UserVO update(Integer id, UserVO userVO) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));

        user.setUsername(userVO.getUsername());
        user.setRealName(userVO.getRealName());
        user.setEmail(userVO.getEmail());
        user.setPhone(userVO.getPhone());
        user.setAvatar(userVO.getAvatar());
        user.setStatus(userVO.getStatus());
        user.setGroupId(userVO.getGroupId());

        return userMapper.toVO(userRepository.save(user));
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
