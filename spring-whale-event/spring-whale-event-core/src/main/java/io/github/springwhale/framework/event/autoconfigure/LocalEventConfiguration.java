package io.github.springwhale.framework.event.autoconfigure;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.local.LocalEventMessageConsumer;
import io.github.springwhale.framework.event.local.LocalEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@AutoConfiguration
@EnableAsync
@ConditionalOnProperty(name = "spring.whale.event.mode", havingValue = "local", matchIfMissing = true)
public class LocalEventConfiguration {

    public static final String LOCAL_EVENT_EXECUTOR = "localEventExecutor";

    @Bean(name = LOCAL_EVENT_EXECUTOR)
    @ConditionalOnMissingBean(name = LOCAL_EVENT_EXECUTOR)
    public Executor localEventExecutor(EventProperties properties) {
        int concurrency = properties.getConcurrency();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(concurrency);
        executor.setMaxPoolSize(concurrency);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setThreadNamePrefix("local-event-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalEventPublisher localEventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                                                   List<EventMetricsCollector> metricsCollectors,
                                                   ApplicationEventPublisher applicationEventPublisher) {
        return new LocalEventPublisher(properties, jsonMapper, metricsCollectors, applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalEventMessageConsumer localEventMessageConsumer(ObjectMapper jsonMapper,
                                                               EventProperties eventProperties,
                                                               List<EventMetricsCollector> metricsCollectors,
                                                               Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                                               ApplicationEventPublisher applicationEventPublisher) {
        return new LocalEventMessageConsumer(jsonMapper, eventProperties, metricsCollectors,
                springListenerBeanMap, applicationEventPublisher);
    }

}