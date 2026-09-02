package io.github.springwhale.platform.rbac.security;

import io.github.springwhale.database.datascope.DataScopeHandler;
import io.github.springwhale.database.datascope.DataScopeType;
import io.github.springwhale.framework.core.utils.AuthUtil;
import io.github.springwhale.platform.rbac.constant.RbacConstants;
import io.github.springwhale.platform.rbac.dao.entity.*;
import io.github.springwhale.platform.rbac.dao.view.UserRoleScopeView;
import io.github.springwhale.platform.rbac.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RBACDataScopeHandler implements DataScopeHandler {

    private final UserRepository userRepository;
    private final UserRoleScopeViewRepository userRoleScopeViewRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final GroupRepository groupRepository;

    @Override
    public boolean skipTenantScope() {
        return true;
    }

    @Cacheable(value = "dataScope", key = "T(io.github.springwhale.framework.core.utils.AuthUtil).getUserId()")
    @Override
    public boolean skipDataScope() {
        Integer userId = AuthUtil.getUserId();
        if (userId == null) {
            return false;
        }

        List<UserRoleScopeView> rows = userRoleScopeViewRepository.findByUserId(userId);
        return rows.stream().anyMatch(r -> RbacConstants.SUPER_ADMIN_ROLE_CODE.equals(r.getRoleCode()));
    }

    @Cacheable(value = "dataScope", key = "T(io.github.springwhale.framework.core.utils.AuthUtil).getUserId() + ':' + #scopeType.name() + ':' + (#module ?: '')")
    @Override
    public List<Object> resolveDeptIds(DataScopeType scopeType, String module) {
        Integer userId = AuthUtil.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getGroupId() == null) {
            return Collections.emptyList();
        }

        return switch (scopeType) {
            case DEPT -> List.of(user.getGroupId());
            case DEPT_AND_CHILD -> resolveDeptAndChildren(user.getGroupId());
            case CUSTOM, AUTO -> resolveFromView(userId, module, user.getGroupId());
            case SELF, CALLER -> Collections.emptyList();
        };
    }

    private List<Object> resolveDeptAndChildren(Integer groupId) {
        Set<Object> ids = new LinkedHashSet<>();
        ids.add(groupId);
        ids.addAll(findDescendantIds(groupId));
        return new ArrayList<>(ids);
    }

    private List<Object> resolveFromView(Integer userId, String module, Integer userGroupId) {
        List<UserRoleScopeView> rows = userRoleScopeViewRepository.findByUserId(userId);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> moduleRoleIds = null;
        if (module != null && !module.isEmpty()) {
            moduleRoleIds = new HashSet<>(roleMenuRepository.findRoleIdsByMenuCode(module));
        }

        Set<Object> deptIds = new LinkedHashSet<>();
        List<Integer> descendantIds = null;

        Set<Integer> seenRoleIds = new HashSet<>();
        for (UserRoleScopeView row : rows) {
            Integer roleId = row.getRoleId();

            if (moduleRoleIds != null && !moduleRoleIds.isEmpty()
                    && !moduleRoleIds.contains(roleId)) {
                continue;
            }

            if (!seenRoleIds.add(roleId)) {
                continue;
            }

            DataScopeType dataScope = parseDataScope(row.getDataScope());
            if (dataScope == null) {
                continue;
            }

            switch (dataScope) {
                case DEPT -> deptIds.add(userGroupId);
                case DEPT_AND_CHILD -> {
                    deptIds.add(userGroupId);
                    if (descendantIds == null) {
                        descendantIds = findDescendantIds(userGroupId);
                    }
                    deptIds.addAll(descendantIds);
                }
                case CUSTOM -> {
                    for (UserRoleScopeView r : rows) {
                        if (r.getRoleId().equals(roleId) && r.getDeptGroupId() != 0) {
                            deptIds.add(r.getDeptGroupId());
                        }
                    }
                }
                case SELF -> {}
                default -> {}
            }
        }
        return new ArrayList<>(deptIds);
    }

    private DataScopeType parseDataScope(String dataScope) {
        if (dataScope == null) {
            return null;
        }
        try {
            return DataScopeType.valueOf(dataScope);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<Integer> findDescendantIds(Integer groupId) {
        GroupEntity group = groupRepository.findById(groupId).orElse(null);
        if (group == null || group.getPath() == null) {
            return Collections.emptyList();
        }
        String prefix = group.getPath() + groupId + "/";
        return groupRepository.findByPathStartingWith(prefix).stream()
                .map(GroupEntity::getId)
                .toList();
    }
}