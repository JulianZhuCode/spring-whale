package io.github.springwhale.framework.event.local;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMessageConsumer;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
public class LocalEventMessageConsumer extends EventMessageConsumer {

    private final ApplicationEventPublisher applicationEventPublisher;

    public LocalEventMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                     List<EventMetricsCollector> metricsCollectors,
                                     Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                     ApplicationEventPublisher applicationEventPublisher) {
        super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap);
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Main local event listener.
     * <p>Receives {@link EventMessage} via Spring's event mechanism and dispatches
     * to matching listeners. Unlike the MQ consumer which receives raw JSON and
     * deserializes, this listener receives the already-built {@link EventMessage}
     * directly from {@link LocalEventPublisher}.</p>
     * <p>{@code @Async} ensures the publisher thread is not blocked — the event
     * is processed in a separate thread pool, matching the async nature of MQ
     * consumers.</p>
     * <p>FAIL messages are silently ignored by {@link #handleMessage} (returns
     * {@code false}), and are instead handled by the recovery module's
     * {@code LocalEventConsumeFailedListener}.</p>
     */
    @EventListener
    @Async
    public void onLocalEvent(EventMessage message) {
        try {
            log.debug("Consuming local event message: {}", message.getData());
            EventContext context = EventContext.builder()
                    .timestamp(System.currentTimeMillis())
                    .topic(message.getTopic())
                    .authenticationContext(message.getAuthenticationContext())
                    .build();
            handleMessage(message, context);
        } catch (Exception e) {
            log.error("Exception occurred while consuming local event message", e);
        }
    }

    /**
     * Send the failed-event message via Spring's event mechanism.
     * <p>The message is published as a Spring application event. The recovery
     * module's {@code LocalEventConsumeFailedListener} picks it up via
     * {@code @EventListener} and persists it for retry processing.</p>
     */
    @Override
    protected void sendToFailedTopic(EventMessage message) {
        applicationEventPublisher.publishEvent(message);
    }

}