package io.github.springwhale.database.flyway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlywayMigrationRetryListenerTest {

    @Mock
    private ResilientFlywayMigrationStrategy strategy;

    private FlywayMigrationRetryListener listener;

    @BeforeEach
    void setUp() {
        listener = new FlywayMigrationRetryListener(strategy);
    }

    @Test
    @DisplayName("Should call retry when event is RETRY_REQUESTED")
    void shouldCallRetryWhenRetryRequested() {
        FlywayMigrationEvent event = new FlywayMigrationEvent(this, FlywayEventType.RETRY_REQUESTED);

        listener.onApplicationEvent(event);

        verify(strategy).retry();
    }

    @Test
    @DisplayName("Should not call retry when event is MIGRATION_FAILED")
    void shouldNotCallRetryWhenMigrationFailed() {
        FlywayMigrationEvent event = new FlywayMigrationEvent(this, FlywayEventType.MIGRATION_FAILED);

        listener.onApplicationEvent(event);

        verify(strategy, never()).retry();
    }
}