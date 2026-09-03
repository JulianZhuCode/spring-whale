package io.github.springwhale.platform.rbac.event;

import io.github.springwhale.framework.event.Event;

/**
 * Published when a group's hierarchy (parentId) changes or the group is deleted.
 * <p>Consumed by {@code DataScopeCacheInvalidationListener} to evict
 * the data scope cache of all users in this group and its descendants.</p>
 */
@Event(businessName = "GroupChanged")
public record GroupChangedEvent(Long groupId) {
}