package io.github.springwhale.platform.rbac.listener;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import io.github.springwhale.platform.rbac.event.GroupChangedEvent;
import io.github.springwhale.platform.rbac.event.RoleChangedEvent;
import io.github.springwhale.platform.rbac.event.UserChangedEvent;
import io.github.springwhale.platform.rbac.security.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Objects;

/**
 * Listens to RBAC domain events and evicts the affected users'
 * {@code userDetails} cache entries.
 *
 * <p>Ensures that role/permission/status changes take effect immediately
 * instead of waiting for cache TTL (up to 30 minutes) to expire.</p>
 *
 * <h3>Event → Cache Eviction Mapping</h3>
 * <ul>
 *   <li>{@link RoleChangedEvent} → find all users with this role → evict for each</li>
 *   <li>{@link GroupChangedEvent} → find all users in this group and descendants → evict for each</li>
 *   <li>{@link UserChangedEvent} → evictUserCache(username) for the affected user (e.g., status change)</li>
 * </ul>
 */
@Slf4j
public class UserDetailsCacheInvalidationListener {

    private final UserDetailsService userDetailsService;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public UserDetailsCacheInvalidationListener(UserDetailsService userDetailsService,
                                                UserRoleRepository userRoleRepository,
                                                UserRepository userRepository,
                                                GroupRepository groupRepository) {
        this.userDetailsService = userDetailsService;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    private void evictByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        if (!(userDetailsService instanceof UserDetailsServiceImpl impl)) {
            log.warn("UserDetailsService is not UserDetailsServiceImpl, cannot evict cache for userId={}", userId);
            return;
        }
        userRepository.findById(userId).ifPresent(user -> {
            log.debug("Evicting userDetails cache for username={}", user.getUsername());
            impl.evictUserCache(user.getUsername());
        });
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
            log.debug("Evicting userDetails cache for {} users assigned to roleId={}",
                    userRoles.size(), event.roleId());
            userRoles.stream()
                    .map(UserRoleEntity::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(UserDetailsCacheInvalidationListener.this::evictByUserId);
        }
    }

    /**
     * Handles {@link GroupChangedEvent} — evicts the cache for users in this group and descendants.
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
            List<Long> affectedGroupIds = groupRepository.findByPathStartingWith(
                            "/" + event.groupId() + "/").stream()
                    .map(group -> group.getId())
                    .collect(java.util.stream.Collectors.toList());
            affectedGroupIds.add(event.groupId());

            List<Long> userIds = userRepository.findAll().stream()
                    .filter(user -> user.getGroupId() != null && affectedGroupIds.contains(user.getGroupId()))
                    .map(UserEntity::getId)
                    .collect(java.util.stream.Collectors.toList());

            if (userIds.isEmpty()) {
                return;
            }
            log.debug("Evicting userDetails cache for {} users due to groupId={} change",
                    userIds.size(), event.groupId());
            userIds.forEach(UserDetailsCacheInvalidationListener.this::evictByUserId);
        }
    }

    /**
     * Handles {@link UserChangedEvent} — evicts the cache for the affected user
     * (e.g., when the user is disabled/enabled).
     */
    public class UserChangedCacheListener extends AbstractEventListener<UserChangedEvent> {

        public UserChangedCacheListener() {
            super(UserChangedEvent.class);
        }

        @Override
        public void doEvent(UserChangedEvent event, EventContext eventContext) {
            if (event.userId() == null) {
                return;
            }
            log.debug("Evicting userDetails cache for userId={} due to UserChanged event", event.userId());
            evictByUserId(event.userId());
        }
    }
}