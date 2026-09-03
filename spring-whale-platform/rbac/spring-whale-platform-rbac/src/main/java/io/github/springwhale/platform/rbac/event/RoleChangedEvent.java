package io.github.springwhale.platform.rbac.event;

import io.github.springwhale.framework.event.Event;

/**
 * Published when a role's data-scope-affecting properties change.
 * <p>This includes changes to {@code dataScope}, {@code status},
 * role-menu associations, and role-dept associations.
 * Consumed by {@code DataScopeCacheInvalidationListener} to evict
 * the data scope cache of all users assigned to this role.</p>
 */
@Event(businessName = "RoleChanged")
public record RoleChangedEvent(Long roleId) {
}