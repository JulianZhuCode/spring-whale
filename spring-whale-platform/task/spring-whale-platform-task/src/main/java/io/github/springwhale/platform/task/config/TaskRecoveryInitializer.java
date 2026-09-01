package io.github.springwhale.platform.task.config;

import io.github.springwhale.platform.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Recovers interrupted tasks on application startup.
 * Tasks that were running when the application last shut down are set to PAUSED
 * so they can be manually resumed.
 */
@RequiredArgsConstructor
@Slf4j
public class TaskRecoveryInitializer {

    private final TaskService taskService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        int recovered = taskService.recoverInterruptedTasks();
        if (recovered > 0) {
            log.warn("Recovered {} interrupted task(s). They are now PAUSED and can be resumed.", recovered);
        } else {
            log.info("No interrupted tasks to recover.");
        }
    }
}