package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import lombok.Builder;

@Builder
public class EventContext {
    private long timestamp;
    private String topic;
    private AuthenticationContext authenticationContext;
}
