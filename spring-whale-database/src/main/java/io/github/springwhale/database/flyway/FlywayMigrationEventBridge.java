package io.github.springwhale.database.flyway;

import io.github.springwhale.framework.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

/**
 * Bridges Spring {@link FlywayMigrationEvent} to the spring-whale-event framework.
 *
 * <p>Listens to Spring's {@link FlywayMigrationEvent} via {@link EventListener} and
 * forwards it to {@link EventPublisher}, enabling event-framework-based listeners
 * such as {@link FlywayEventRetryListener} to consume the event.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class FlywayMigrationEventBridge {

    private final EventPublisher eventPublisher;

    @EventListener
    public void onFlywayMigrationEvent(FlywayMigrationEvent event) {
        log.info("Bridging Flyway migration event [{}] to event framework", event.getType());
        eventPublisher.publish(event);
    }
}