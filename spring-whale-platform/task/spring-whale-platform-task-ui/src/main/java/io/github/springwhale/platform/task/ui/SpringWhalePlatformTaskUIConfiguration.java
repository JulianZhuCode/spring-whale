package io.github.springwhale.platform.task.ui;

import io.github.springwhale.platform.task.service.TaskService;
import io.github.springwhale.platform.task.ui.controller.TaskPageController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
 
@AutoConfiguration
public class SpringWhalePlatformTaskUIConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public TaskPageController taskPageController(TaskService taskService) {
        return new TaskPageController(taskService);
    }

}