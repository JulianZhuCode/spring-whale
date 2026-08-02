package io.github.springwhale.platform.task;

import io.github.springwhale.platform.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EntityScan(basePackages = "io.github.springwhale.task.entity")
@EnableJpaRepositories(basePackages = "io.github.springwhale.task.repository")
@Slf4j
public class SpringWhaleTaskConfiguration {

    private final TaskService taskService;

    public SpringWhaleTaskConfiguration(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * On startup, recover any tasks that were running when the application last shut down.
     * This enables breakpoint resume after crashes.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        int recovered = taskService.recoverInterruptedTasks();
        if (recovered > 0) {
            log.warn("Recovered {} interrupted task(s). They are now PAUSED and can be resumed.", recovered);
        } else {
            log.info("No interrupted tasks to recover.");
        }
    }
}
