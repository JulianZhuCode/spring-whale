package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.platform.rbac.dao.entity.RoleDeptEntity;
import io.github.springwhale.platform.rbac.dao.repository.RoleDeptRepository;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.platform.rbac.event.RoleChangedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public class RoleDeptService {

    private final RoleDeptRepository roleDeptRepository;
    private final EventPublisher eventPublisher;

    public RoleDeptService(RoleDeptRepository roleDeptRepository,
                           EventPublisher eventPublisher) {
        this.roleDeptRepository = roleDeptRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Integer> getDeptIdsByRoleId(Integer roleId) {
        return roleDeptRepository.findByRoleId(roleId).stream()
                .map(RoleDeptEntity::getGroupId)
                .toList();
    }

    @Transactional
    public void addDepts(Integer roleId, List<Integer> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (Integer deptId : deptIds) {
            if (roleDeptRepository.findByRoleIdAndGroupId(roleId, deptId).isEmpty()) {
                RoleDeptEntity entity = new RoleDeptEntity();
                entity.setRoleId(roleId);
                entity.setGroupId(deptId);
                roleDeptRepository.save(entity);
                changed = true;
            }
        }
        if (changed) {
            eventPublisher.publishAfterCommit(new RoleChangedEvent(roleId));
        }
    }

    @Transactional
    public void removeDepts(Integer roleId, List<Integer> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (Integer deptId : deptIds) {
            Optional<RoleDeptEntity> existing = roleDeptRepository.findByRoleIdAndGroupId(roleId, deptId);
            if (existing.isPresent()) {
                roleDeptRepository.delete(existing.get());
                changed = true;
            }
        }
        if (changed) {
            eventPublisher.publishAfterCommit(new RoleChangedEvent(roleId));
        }
    }
}