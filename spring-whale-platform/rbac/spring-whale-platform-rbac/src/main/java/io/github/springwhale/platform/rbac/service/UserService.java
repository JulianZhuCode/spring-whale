package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dto.request.UserRequest;
import io.github.springwhale.platform.rbac.dto.vo.UserVO;
import io.github.springwhale.platform.rbac.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User service
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserMapper userMapper;

    /**
     * Find all users with pagination
     */
    public Page<UserVO> findAll(Pageable pageable) {
        Page<UserVO> page = userRepository.findAll(pageable).map(userMapper::toVO);
        enrichGroupNames(page.getContent());
        return page;
    }

    /**
     * Find users with filter
     */
    public Page<UserVO> findWithFilter(String keyword, Integer status, Pageable pageable) {
        var spec = JpaQueryWrapper.of(UserEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(UserEntity::getUsername, keyword)
                        .likeIgnoreCase(UserEntity::getRealName, keyword)
                        .likeIgnoreCase(UserEntity::getEmail, keyword))
                .eq(status != null, UserEntity::getStatus, status)
                .buildSpec();
        Page<UserVO> page = userRepository.findAll(spec, pageable).map(userMapper::toVO);
        enrichGroupNames(page.getContent());
        return page;
    }

    /**
     * Find user by ID
     */
    public Optional<UserVO> findById(Integer id) {
        return userRepository.findById(id)
                .map(userMapper::toVO)
                .map(this::enrichGroupName);
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
        return enrichGroupName(userMapper.toVO(userRepository.save(entity)));
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

        return enrichGroupName(userMapper.toVO(userRepository.save(user)));
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

    // ==================== Group name enrichment ====================

    private void enrichGroupNames(List<UserVO> vos) {
        if (vos == null || vos.isEmpty()) return;
        List<Integer> groupIds = vos.stream()
                .map(UserVO::getGroupId)
                .filter(gid -> gid != null)
                .distinct()
                .toList();
        if (groupIds.isEmpty()) return;
        Map<Integer, String> groupNameMap = groupRepository.findAllByIdIn(groupIds).stream()
                .collect(Collectors.toMap(GroupEntity::getId, GroupEntity::getName));
        vos.forEach(vo -> {
            if (vo.getGroupId() != null) {
                vo.setGroupName(groupNameMap.get(vo.getGroupId()));
            }
        });
    }

    private UserVO enrichGroupName(UserVO vo) {
        if (vo != null && vo.getGroupId() != null) {
            groupRepository.findById(vo.getGroupId())
                    .ifPresent(g -> vo.setGroupName(g.getName()));
        }
        return vo;
    }
}