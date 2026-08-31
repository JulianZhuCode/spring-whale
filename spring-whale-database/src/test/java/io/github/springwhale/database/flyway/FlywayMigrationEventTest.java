package io.github.springwhale.database.flyway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationEventTest {

    @Test
    @DisplayName("Should create event with MIGRATION_FAILED type")
    void shouldCreateMigrationFailedEvent() {
        Object source = new Object();
        FlywayMigrationEvent event = new FlywayMigrationEvent(source, FlywayEventType.MIGRATION_FAILED);

        assertThat(event.getSource()).isSameAs(source);
        assertThat(event.getType()).isEqualTo(FlywayEventType.MIGRATION_FAILED);
        assertThat(event).isInstanceOf(ApplicationEvent.class);
    }

    @Test
    @DisplayName("Should create event with RETRY_REQUESTED type")
    void shouldCreateRetryRequestedEvent() {
        Object source = new Object();
        FlywayMigrationEvent event = new FlywayMigrationEvent(source, FlywayEventType.RETRY_REQUESTED);

        assertThat(event.getSource()).isSameAs(source);
        assertThat(event.getType()).isEqualTo(FlywayEventType.RETRY_REQUESTED);
    }

    @Test
    @DisplayName("Should support no-arg constructor for deserialization")
    void shouldSupportNoArgsConstructor() {
        FlywayMigrationEvent event = new FlywayMigrationEvent();

        assertThat(event.getSource()).isEqualTo("");
        assertThat(event.getType()).isNull();
        event.setType(FlywayEventType.MIGRATION_FAILED);
        assertThat(event.getType()).isEqualTo(FlywayEventType.MIGRATION_FAILED);
    }
}