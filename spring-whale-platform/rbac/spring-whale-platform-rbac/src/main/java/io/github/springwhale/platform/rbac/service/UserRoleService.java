package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import io.github.springwhale.platform.rbac.event.UserChangedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User-role association service
 */
@Transactional(readOnly = true)
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final EventPublisher eventPublisher;

    public UserRoleService(UserRoleRepository userRoleRepository,
                           EventPublisher eventPublisher) {
        this.userRoleRepository = userRoleRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Get role IDs assigned to a user
     */
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
    }

    /**
     * Batch add roles to a user — skips already existing associations
     */
    @Transactional
    public void addRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (Long roleId : roleIds) {
            if (userRoleRepository.findByUserIdAndRoleId(userId, roleId).isEmpty()) {
                UserRoleEntity entity = new UserRoleEntity();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                userRoleRepository.save(entity);
                changed = true;
            }
        }
        if (changed) {
            eventPublisher.publishAfterCommit(new UserChangedEvent(userId));
        }
    }

    /**
     * Batch remove roles from a user — only removes existing associations
     */
    @Transactional
    public void removeRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (Long roleId : roleIds) {
            Optional<UserRoleEntity> existing = userRoleRepository.findByUserIdAndRoleId(userId, roleId);
            if (existing.isPresent()) {
                userRoleRepository.delete(existing.get());
                changed = true;
            }
        }
        if (changed) {
            eventPublisher.publishAfterCommit(new UserChangedEvent(userId));
        }
    }
}