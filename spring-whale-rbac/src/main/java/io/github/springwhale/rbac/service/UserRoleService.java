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
 * 用户角色关联服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    /**
     * 根据用户ID查询所有角色关联
     */
    public List<UserRoleVO> findByUserId(Integer userId) {
        List<UserRoleEntity> entities = userRoleRepository.findByUserId(userId);
        return userRoleMapper.toVOList(entities);
    }

    /**
     * 根据角色ID查询所有用户关联
     */
    public List<UserRoleVO> findByRoleId(Integer roleId) {
        List<UserRoleEntity> entities = userRoleRepository.findByRoleId(roleId);
        return userRoleMapper.toVOList(entities);
    }

    /**
     * 为用户分配角色
     */
    @Transactional
    public void assignRoleToUser(Integer userId, Integer roleId) {
        // 检查是否已存在
        if (userRoleRepository.findByUserIdAndRoleId(userId, roleId).isPresent()) {
            throw BusinessException.create("USER_ROLE_ALREADY_EXISTS", "用户已拥有该角色");
        }
        
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
    }

    /**
     * 批量为用户分配角色
     */
    @Transactional
    public void assignRolesToUser(Integer userId, List<Integer> roleIds) {
        // 先删除用户的所有角色关联
        userRoleRepository.deleteByUserId(userId);
        
        // 重新分配
        for (Integer roleId : roleIds) {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleRepository.save(userRole);
        }
    }

    /**
     * 移除用户的角色
     */
    @Transactional
    public void removeRoleFromUser(Integer userId, Integer roleId) {
        UserRoleEntity userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> BusinessException.create("USER_ROLE_NOT_FOUND", "用户角色关联不存在"));
        userRoleRepository.delete(userRole);
    }

    /**
     * 移除用户的所有角色
     */
    @Transactional
    public void removeAllRolesFromUser(Integer userId) {
        userRoleRepository.deleteByUserId(userId);
    }
}
