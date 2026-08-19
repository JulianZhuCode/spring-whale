package io.github.springwhale.framework.event.autoconfigure;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.rabbit.RabbitEventMessageConsumer;
import io.github.springwhale.framework.event.rabbit.RabbitEventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitEventConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RabbitEventPublisher rabbitEventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                                                     List<EventMetricsCollector> metricsCollectors,
                                                     RabbitTemplate rabbitTemplate) {
        return new RabbitEventPublisher(properties, jsonMapper, metricsCollectors, rabbitTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitEventMessageConsumer rabbitEventMessageConsumer(ObjectMapper jsonMapper,
                                                                 EventProperties eventProperties,
                                                                 List<EventMetricsCollector> metricsCollectors,
                                                                 Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                                                 RabbitTemplate rabbitTemplate) {
        return new RabbitEventMessageConsumer(jsonMapper, eventProperties, metricsCollectors,
                springListenerBeanMap, rabbitTemplate);
    }

}