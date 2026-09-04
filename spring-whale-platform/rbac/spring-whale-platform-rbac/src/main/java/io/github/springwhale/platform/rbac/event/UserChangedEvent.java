package io.github.springwhale.platform.rbac.event;

import io.github.springwhale.framework.event.Event;

/**
 * Published when a user's properties change (status, group, etc.).
 * <p>Consumed by {@code UserDetailsCacheInvalidationListener} to evict
 * the affected user's authentication cache.</p>
 */
@Event(businessName = "UserChanged")
public record UserChangedEvent(Long userId) {
}