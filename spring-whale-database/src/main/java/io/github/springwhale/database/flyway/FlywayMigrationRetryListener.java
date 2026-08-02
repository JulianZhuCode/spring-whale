package io.github.springwhale.database.flyway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationListener;

@Slf4j
@RequiredArgsConstructor
public class FlywayMigrationRetryListener implements ApplicationListener<FlywayMigrationEvent> {

    private final ResilientFlywayMigrationStrategy resilientFlywayMigrationStrategy;

    @Override
    public void onApplicationEvent(@NonNull FlywayMigrationEvent event) {
        if (event.getType() != FlywayEventType.RETRY_REQUESTED) {
            return;
        }
        log.info("Flyway migration retry event received");
        resilientFlywayMigrationStrategy.retry();
    }
}