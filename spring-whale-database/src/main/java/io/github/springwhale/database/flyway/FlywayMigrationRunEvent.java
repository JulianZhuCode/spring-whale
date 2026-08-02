package io.github.springwhale.database.flyway;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FlywayMigrationRunEvent extends ApplicationEvent {

    public FlywayMigrationRunEvent(Object source) {
        super(source);
    }
}
