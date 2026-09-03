package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.platform.rbac.dao.entity.RoleDeptEntity;
import io.github.springwhale.platform.rbac.dao.repository.RoleDeptRepository;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.platform.rbac.event.RoleChangedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public List<Long> getDeptIdsByRoleId(Long roleId) {
        return roleDeptRepository.findByRoleId(roleId).stream()
                .map(RoleDeptEntity::getGroupId)
                .toList();
    }

    @Transactional
    public void addDepts(Long roleId, List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        List<RoleDeptEntity> toSave = new ArrayList<>();
        for (Long deptId : deptIds) {
            if (roleDeptRepository.findByRoleIdAndGroupId(roleId, deptId).isEmpty()) {
                RoleDeptEntity entity = new RoleDeptEntity();
                entity.setRoleId(roleId);
                entity.setGroupId(deptId);
                toSave.add(entity);
            }
        }
        if (!toSave.isEmpty()) {
            roleDeptRepository.saveAll(toSave);
            eventPublisher.publishAfterCommit(new RoleChangedEvent(roleId));
        }
    }

    @Transactional
    public void removeDepts(Long roleId, List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        List<RoleDeptEntity> toDelete = new ArrayList<>();
        for (Long deptId : deptIds) {
            Optional<RoleDeptEntity> existing = roleDeptRepository.findByRoleIdAndGroupId(roleId, deptId);
            existing.ifPresent(toDelete::add);
        }
        if (!toDelete.isEmpty()) {
            roleDeptRepository.deleteAll(toDelete);
            eventPublisher.publishAfterCommit(new RoleChangedEvent(roleId));
        }
    }
}