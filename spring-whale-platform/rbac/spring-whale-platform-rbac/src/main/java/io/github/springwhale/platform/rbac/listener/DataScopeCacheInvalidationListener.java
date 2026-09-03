package io.github.springwhale.platform.rbac.listener;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleDeptRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import io.github.springwhale.platform.rbac.event.GroupChangedEvent;
import io.github.springwhale.platform.rbac.event.RoleChangedEvent;
import io.github.springwhale.platform.rbac.event.UserRoleChangedEvent;
import io.github.springwhale.platform.rbac.security.RBACDataScopeHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * Listens to RBAC domain events and evicts the affected users'
 * data scope cache entries.
 *
 * <p>Each event type is handled by a dedicated inner listener class
 * that extends {@code AbstractEventListener} for the corresponding event type.</p>
 *
 * <h3>Event → Cache Eviction Mapping</h3>
 * <ul>
 *   <li>{@link UserRoleChangedEvent} → evictUser(userId) — O(1)</li>
 *   <li>{@link RoleChangedEvent} → find all users with this role → evictUser for each</li>
 *   <li>{@link GroupChangedEvent} → find all users in this group and descendants → evictUser for each</li>
 * </ul>
 */
@Slf4j
public class DataScopeCacheInvalidationListener {

    private final RBACDataScopeHandler handler;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final RoleDeptRepository roleDeptRepository;

    public DataScopeCacheInvalidationListener(RBACDataScopeHandler handler,
                                              UserRoleRepository userRoleRepository,
                                              UserRepository userRepository,
                                              GroupRepository groupRepository,
                                              RoleDeptRepository roleDeptRepository) {
        this.handler = handler;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleDeptRepository = roleDeptRepository;
    }

    /**
     * Handles {@link UserRoleChangedEvent} — evicts the cache for the affected user.
     */
    public class UserRoleChangedCacheListener extends AbstractEventListener<UserRoleChangedEvent> {

        public UserRoleChangedCacheListener() {
            super(UserRoleChangedEvent.class);
        }

        @Override
        public void doEvent(UserRoleChangedEvent event, EventContext eventContext) {
            if (event.userId() == null) {
                return;
            }
            log.debug("Evicting data scope cache for userId={} due to UserRoleChanged event", event.userId());
            handler.evictUser(event.userId());
        }
    }

    /**
     * Handles {@link RoleChangedEvent} — evicts the cache for all users assigned to this role.
     */
    public class RoleChangedCacheListener extends AbstractEventListener<RoleChangedEvent> {

        public RoleChangedCacheListener() {
            super(RoleChangedEvent.class);
        }

        @Override
        public void doEvent(RoleChangedEvent event, EventContext eventContext) {
            if (event.roleId() == null) {
                return;
            }
            List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(event.roleId());
            if (userRoles.isEmpty()) {
                return;
            }
            log.debug("Evicting data scope cache for {} users assigned to roleId={}",
                    userRoles.size(), event.roleId());
            userRoles.stream()
                    .map(UserRoleEntity::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(handler::evictUser);
        }
    }

    /**
     * Handles {@link GroupChangedEvent} — evicts the cache for:
     * <ol>
     *   <li>Users whose {@code groupId} matches this group or its descendants</li>
     *   <li>Users assigned to roles with {@code dataScope = CUSTOM} that reference this group</li>
     * </ol>
     */
    public class GroupChangedCacheListener extends AbstractEventListener<GroupChangedEvent> {

        public GroupChangedCacheListener() {
            super(GroupChangedEvent.class);
        }

        @Override
        public void doEvent(GroupChangedEvent event, EventContext eventContext) {
            if (event.groupId() == null) {
                return;
            }
            List<Integer> affectedGroupIds = groupRepository.findByPathStartingWith(
                    "/" + event.groupId() + "/").stream()
                    .map(group -> group.getId())
                    .collect(java.util.stream.Collectors.toList());
            affectedGroupIds.add(event.groupId());

            List<Integer> userIds = userRepository.findAll().stream()
                    .filter(user -> user.getGroupId() != null && affectedGroupIds.contains(user.getGroupId()))
                    .map(UserEntity::getId)
                    .collect(java.util.stream.Collectors.toList());

            List<Integer> customDataScopeUserIds = roleDeptRepository.findByGroupId(event.groupId()).stream()
                    .flatMap(rd -> userRoleRepository.findByRoleId(rd.getRoleId()).stream())
                    .map(UserRoleEntity::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            userIds = java.util.stream.Stream.concat(userIds.stream(), customDataScopeUserIds.stream())
                    .distinct()
                    .toList();

            if (userIds.isEmpty()) {
                return;
            }
            log.debug("Evicting data scope cache for {} users due to groupId={} change",
                    userIds.size(), event.groupId());
            userIds.forEach(handler::evictUser);
        }
    }
}