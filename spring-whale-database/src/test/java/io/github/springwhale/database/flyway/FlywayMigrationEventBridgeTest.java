package io.github.springwhale.database.flyway;

import io.github.springwhale.framework.event.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlywayMigrationEventBridgeTest {

    @Mock
    private EventPublisher eventPublisher;

    private FlywayMigrationEventBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new FlywayMigrationEventBridge(eventPublisher);
    }

    @Test
    @DisplayName("Should forward FlywayMigrationEvent to EventPublisher")
    void shouldForwardEventToEventPublisher() {
        FlywayMigrationEvent event = new FlywayMigrationEvent(this, FlywayEventType.MIGRATION_FAILED);

        bridge.onFlywayMigrationEvent(event);

        verify(eventPublisher).publish(event);
    }

    @Test
    @DisplayName("Should forward RETRY_REQUESTED event to EventPublisher")
    void shouldForwardRetryRequestedEvent() {
        FlywayMigrationEvent event = new FlywayMigrationEvent(this, FlywayEventType.RETRY_REQUESTED);

        bridge.onFlywayMigrationEvent(event);

        verify(eventPublisher).publish(event);
    }
}