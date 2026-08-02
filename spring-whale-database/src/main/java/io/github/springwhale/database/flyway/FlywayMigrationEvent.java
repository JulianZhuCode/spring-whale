package io.github.springwhale.database.flyway;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FlywayMigrationEvent extends ApplicationEvent {

    private final FlywayEventType type;

    public FlywayMigrationEvent(Object source, FlywayEventType type) {
        super(source);
        this.type = type;
    }
}