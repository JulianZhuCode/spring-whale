package io.github.springwhale.platform.rbac.event;

import io.github.springwhale.framework.event.Event;

/**
 * Published when a user-role association is created or deleted.
 * <p>Consumed by {@code DataScopeCacheInvalidationListener} to evict
 * the affected user's data scope cache.</p>
 */
@Event(businessName = "UserRoleChanged")
public record UserRoleChangedEvent(Integer userId, Integer roleId) {
}