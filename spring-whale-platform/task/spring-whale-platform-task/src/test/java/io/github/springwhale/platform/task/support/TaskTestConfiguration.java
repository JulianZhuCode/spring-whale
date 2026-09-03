package io.github.springwhale.platform.task.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Registers test-only {@link io.github.springwhale.platform.task.handler.TaskHandler} beans.
 */
@TestConfiguration
public class TaskTestConfiguration {

    @Bean
    public TestTaskHandler testTaskHandler() {
        return new TestTaskHandler();
    }

    @Bean
    public BlockingTaskHandler blockingTaskHandler() {
        return new BlockingTaskHandler();
    }
}
