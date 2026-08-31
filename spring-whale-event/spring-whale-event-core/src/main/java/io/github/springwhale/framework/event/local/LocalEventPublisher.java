package io.github.springwhale.framework.event.local;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public class LocalEventPublisher extends EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public LocalEventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                               List<EventMetricsCollector> metricsCollectors,
                               ApplicationEventPublisher applicationEventPublisher) {
        super(properties, jsonMapper, metricsCollectors);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Send the event message via Spring's {@link ApplicationEventPublisher}.
     * <p>Unlike MQ-based publishers, this publishes the message as a Spring
     * application event. The {@link LocalEventMessageConsumer} picks it up
     * via {@code @EventListener} and dispatches to matching listeners.</p>
     * <p>The publish is synchronous: the caller blocks until all synchronous
     * listeners have processed the event. Async listeners execute in a
     * separate thread pool.</p>
     * <p>{@code partitionKey} is ignored in local mode — events are inherently
     * ordered by the calling thread.</p>
     */
    @Override
    protected void doSend(EventMessage message, String partitionKey) {
        applicationEventPublisher.publishEvent(message);
    }

}