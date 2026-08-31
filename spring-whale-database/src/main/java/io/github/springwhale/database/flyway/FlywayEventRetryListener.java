package io.github.springwhale.database.flyway;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlywayEventRetryListener extends AbstractEventListener<FlywayMigrationEvent> {

    private final ResilientFlywayMigrationStrategy resilientFlywayMigrationStrategy;

    public FlywayEventRetryListener(ResilientFlywayMigrationStrategy resilientFlywayMigrationStrategy) {
        super(FlywayMigrationEvent.class);
        this.resilientFlywayMigrationStrategy = resilientFlywayMigrationStrategy;
    }

    @Override
    public void doEvent(FlywayMigrationEvent event, EventContext eventContext) {
        if (event.getType() != FlywayEventType.RETRY_REQUESTED) {
            return;
        }
        log.info("Flyway migration retry event received");
        resilientFlywayMigrationStrategy.retry();
    }
}
