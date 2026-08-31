package io.github.springwhale.database.flyway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = true)
@Data
public class FlywayMigrationEvent extends ApplicationEvent {

    private FlywayEventType type;

    public FlywayMigrationEvent() {
        super("");
    }

    public FlywayMigrationEvent(Object source, FlywayEventType type) {
        super(source);
        this.type = type;
    }
}