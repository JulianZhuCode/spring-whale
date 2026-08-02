package io.github.springwhale.database.flyway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationListener;

@Slf4j
@RequiredArgsConstructor
public class FlywayMigrationRunListener implements ApplicationListener<FlywayMigrationRunEvent> {

    private final ResilientFlywayMigrationStrategy resilientFlywayMigrationStrategy;

    @Override
    public void onApplicationEvent(@NonNull FlywayMigrationRunEvent event) {
        log.info("Flyway migration run event received");
        resilientFlywayMigrationStrategy.retry();
    }
}
