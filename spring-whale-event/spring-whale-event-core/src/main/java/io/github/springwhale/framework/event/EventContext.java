package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import lombok.Builder;

/**
 * Context object passed to event listeners along with the event payload.
 * <p>Contains metadata from the MQ broker (timestamp, topic) and the
 * authentication context of the original publisher.</p>
 */
@Builder
public class EventContext {
    private long timestamp;
    private String topic;
    private AuthenticationContext authenticationContext;
}