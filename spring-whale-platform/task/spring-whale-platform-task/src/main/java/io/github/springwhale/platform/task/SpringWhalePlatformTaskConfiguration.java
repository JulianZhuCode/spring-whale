package io.github.springwhale.platform.task;

import io.github.springwhale.platform.task.config.TaskRecoveryInitializer;
import io.github.springwhale.platform.task.controller.TaskController;
import io.github.springwhale.platform.task.controller.TaskPageController;
import io.github.springwhale.platform.task.dao.repository.TaskBatchItemRepository;
import io.github.springwhale.platform.task.dao.repository.TaskBatchRepository;
import io.github.springwhale.platform.task.handler.TaskHandler;
import io.github.springwhale.platform.task.mapper.TaskMapper;
import io.github.springwhale.platform.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Auto-configuration for Spring Whale Platform Task module.
 * <p>
 * All beans are explicitly registered via {@code @Bean} methods
 * rather than component scanning.
 * </p>
 */
@AutoConfiguration
@EnableCaching
@EnableJpaRepositories
@EntityScan
@Slf4j
public class SpringWhalePlatformTaskConfiguration {

    // ==================== Mapper ====================

    @Bean
    @ConditionalOnMissingBean
    public TaskMapper taskMapper() {
        return new TaskMapper();
    }

    // ==================== Service ====================

    @Bean
    @ConditionalOnMissingBean
    public TaskService taskService(TaskBatchRepository taskRepository,
                                   TaskBatchItemRepository itemRepository,
                                   TaskMapper taskMapper,
                                   List<TaskHandler> handlers,
                                   PlatformTransactionManager transactionManager) {
        return new TaskService(taskRepository, itemRepository, taskMapper, handlers, transactionManager);
    }

    // ==================== Controllers ====================

    @Bean
    @ConditionalOnMissingBean
    public TaskController taskController(TaskService taskService) {
        return new TaskController(taskService);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskPageController taskPageController(TaskService taskService) {
        return new TaskPageController(taskService);
    }

    // ==================== Initializer ====================

    @Bean
    @ConditionalOnMissingBean
    public TaskRecoveryInitializer taskRecoveryInitializer(TaskService taskService) {
        return new TaskRecoveryInitializer(taskService);
    }
}