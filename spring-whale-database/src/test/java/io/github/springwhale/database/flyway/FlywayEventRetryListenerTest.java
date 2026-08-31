package io.github.springwhale.database.flyway;

import io.github.springwhale.framework.event.EventContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlywayEventRetryListenerTest {

    @Mock
    private ResilientFlywayMigrationStrategy strategy;

    private FlywayEventRetryListener listener;

    @BeforeEach
    void setUp() {
        listener = new FlywayEventRetryListener(strategy);
    }

    @Test
    @DisplayName("Should call retry when event type is RETRY_REQUESTED")
    void shouldCallRetryWhenRetryRequested() {
        FlywayMigrationEvent event = new FlywayMigrationEvent(this, FlywayEventType.RETRY_REQUESTED);
        EventContext context = EventContext.builder().build();

        listener.doEvent(event, context);

        verify(strategy).retry();
    }

    @Test
    @DisplayName("Should not call retry when event type is MIGRATION_FAILED")
    void shouldNotCallRetryWhenMigrationFailed() {
        FlywayMigrationEvent event = new FlywayMigrationEvent(this, FlywayEventType.MIGRATION_FAILED);
        EventContext context = EventContext.builder().build();

        listener.doEvent(event, context);

        verify(strategy, never()).retry();
    }
}