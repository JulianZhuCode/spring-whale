package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.platform.rbac.dao.entity.RoleDeptEntity;
import io.github.springwhale.platform.rbac.repository.RoleDeptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleDeptService {

    private final RoleDeptRepository roleDeptRepository;

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
        for (Integer deptId : deptIds) {
            if (roleDeptRepository.findByRoleIdAndGroupId(roleId, deptId).isEmpty()) {
                RoleDeptEntity entity = new RoleDeptEntity();
                entity.setRoleId(roleId);
                entity.setGroupId(deptId);
                roleDeptRepository.save(entity);
            }
        }
    }

    @Transactional
    public void removeDepts(Integer roleId, List<Integer> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        for (Integer deptId : deptIds) {
            roleDeptRepository.findByRoleIdAndGroupId(roleId, deptId)
                    .ifPresent(roleDeptRepository::delete);
        }
    }
}