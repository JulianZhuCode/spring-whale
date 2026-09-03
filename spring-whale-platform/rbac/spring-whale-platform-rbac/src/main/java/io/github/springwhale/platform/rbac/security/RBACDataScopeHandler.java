package io.github.springwhale.platform.rbac.security;

import io.github.springwhale.database.datascope.DataScopeCacheKey;
import io.github.springwhale.database.datascope.DataScopeHandler;
import io.github.springwhale.database.datascope.DataScopeProperties;
import io.github.springwhale.database.datascope.DataScopeType;
import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.utils.AuthUtil;
import io.github.springwhale.platform.rbac.constant.RbacConstants;
import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleScopeViewRepository;
import io.github.springwhale.platform.rbac.dao.view.UserRoleScopeView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class RBACDataScopeHandler implements DataScopeHandler {

    private final WhaleCacheManager cacheManager;
    private final UserRepository userRepository;
    private final UserRoleScopeViewRepository userRoleScopeViewRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final GroupRepository groupRepository;
    private final Duration skipTtl;
    private final Duration deptTtl;

    public RBACDataScopeHandler(WhaleCacheManager cacheManager,
                                UserRepository userRepository,
                                UserRoleScopeViewRepository userRoleScopeViewRepository,
                                RoleMenuRepository roleMenuRepository,
                                GroupRepository groupRepository,
                                DataScopeProperties properties) {
        this.cacheManager = cacheManager;
        this.userRepository = userRepository;
        this.userRoleScopeViewRepository = userRoleScopeViewRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.groupRepository = groupRepository;
        DataScopeProperties.Cache cacheProps = properties.getCache();
        this.skipTtl = cacheProps.getSkipTtl();
        this.deptTtl = cacheProps.getDeptTtl();
    }

    @Override
    public boolean skipTenantScope() {
        return true;
    }

    @Override
    public boolean skipDataScope() {
        return skipDataScope(AuthUtil.getUserId());
    }

    public boolean skipDataScope(Integer userId) {
        if (userId == null) {
            return false;
        }
        WhaleCache cache = cacheManager.getCache("dataScope");
        return cache.get(DataScopeCacheKey.skipDataScope(userId), Boolean.class,
                () -> doSkipDataScope(userId), skipTtl);
    }

    public boolean skipTenantScope(Integer userId) {
        if (userId == null) {
            return false;
        }
        WhaleCache cache = cacheManager.getCache("dataScope");
        return cache.get(DataScopeCacheKey.skipTenantScope(userId), Boolean.class,
                () -> doSkipDataScope(userId), skipTtl);
    }

    private boolean doSkipDataScope(Integer userId) {
        List<UserRoleScopeView> rows = userRoleScopeViewRepository.findByUserId(userId);
        return rows.stream().anyMatch(r -> RbacConstants.SUPER_ADMIN_ROLE_CODE.equals(r.getRoleCode()));
    }

    @Override
    public List<Object> resolveDeptIds(DataScopeType scopeType, String module) {
        return resolveDeptIds(AuthUtil.getUserId(), scopeType, module);
    }

    public List<Object> resolveDeptIds(Integer userId, DataScopeType scopeType, String module) {
        if (userId == null) {
            return Collections.emptyList();
        }
        WhaleCache cache = cacheManager.getCache("dataScope");
        return cache.getList(DataScopeCacheKey.resolveDeptIds(userId, scopeType, module),
                () -> doResolveDeptIds(userId, scopeType, module), deptTtl);
    }

    private List<Object> doResolveDeptIds(Integer userId, DataScopeType scopeType, String module) {
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

        // Pre-group CUSTOM dept ids by roleId for O(1) lookup
        Map<Integer, List<Integer>> customDeptMap = new HashMap<>();
        for (UserRoleScopeView row : rows) {
            if (row.getDeptGroupId() != null && row.getDeptGroupId() != 0) {
                customDeptMap.computeIfAbsent(row.getRoleId(), k -> new ArrayList<>()).add(row.getDeptGroupId());
            }
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
                    List<Integer> customDeptIds = customDeptMap.get(roleId);
                    if (customDeptIds != null) {
                        deptIds.addAll(customDeptIds);
                    }
                }
                case SELF -> {
                }
                default -> {
                }
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

    /**
     * Evict all cached data scope results for the given user.
     *
     * <p>Call this method after any permission change that affects data scope:
     * <ul>
     *   <li>User role assignment / revocation</li>
     *   <li>Role permission changes</li>
     *   <li>User department / group changes</li>
     * </ul>
     *
     * <p><b>Limitation:</b> Due to the dynamic nature of {@code resolveDeptIds}
     * cache keys (which include {@code scopeType} and {@code module}), only the
     * {@code skipDataScope} keys are evicted immediately. The {@code resolveDeptIds}
     * results expire naturally via their short TTL (default: 2 minutes).</p>
     *
     * @param userId the user whose cache entries should be evicted
     */
    public void evictUser(Integer userId) {
        if (userId == null) {
            return;
        }
        WhaleCache cache = cacheManager.getCache("dataScope");
        cache.evict(DataScopeCacheKey.skipDataScope(userId));
        cache.evict(DataScopeCacheKey.fallbackSkipDataScope(userId));
        cache.evict(DataScopeCacheKey.skipTenantScope(userId));
        cache.evict(DataScopeCacheKey.fallbackSkipTenantScope(userId));
        log.debug("Evicted data scope cache for userId={}", userId);
    }
}