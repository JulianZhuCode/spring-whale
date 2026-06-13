package io.github.springwhale.rbac.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.vo.UserRoleVO;
import io.github.springwhale.rbac.entity.UserRoleEntity;
import io.github.springwhale.rbac.mapper.UserRoleMapper;
import io.github.springwhale.rbac.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User-role association service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    /**
     * Find all role associations by user ID
     */
    public List<UserRoleVO> findByUserId(Integer userId) {
        List<UserRoleEntity> entities = userRoleRepository.findByUserId(userId);
        return userRoleMapper.toVOList(entities);
    }

    /**
     * Find all user associations by role ID
     */
    public List<UserRoleVO> findByRoleId(Integer roleId) {
        List<UserRoleEntity> entities = userRoleRepository.findByRoleId(roleId);
        return userRoleMapper.toVOList(entities);
    }

    /**
     * Assign role to user
     */
    @Transactional
    public void assignRoleToUser(Integer userId, Integer roleId) {
        // Check if already exists
        if (userRoleRepository.findByUserIdAndRoleId(userId, roleId).isPresent()) {
            throw BusinessException.create("USER_ROLE_ALREADY_EXISTS", "User already has this role");
        }

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
    }

    /**
     * Batch assign roles to user
     */
    @Transactional
    public void assignRolesToUser(Integer userId, List<Integer> roleIds) {
        // Remove all existing role associations for user first
        userRoleRepository.deleteByUserId(userId);

        // Reassign
        for (Integer roleId : roleIds) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleRepository.save(userRole);
        }
    }

    /**
     * Remove role from user
     */
    @Transactional
    public void removeRoleFromUser(Integer userId, Integer roleId) {
        UserRoleEntity userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> BusinessException.create("USER_ROLE_NOT_FOUND", "User-role association not found"));
        userRoleRepository.delete(userRole);
    }

    /**
     * Remove all roles from user
     */
    @Transactional
    public void removeAllRolesFromUser(Integer userId) {
        userRoleRepository.deleteByUserId(userId);
    }
}
