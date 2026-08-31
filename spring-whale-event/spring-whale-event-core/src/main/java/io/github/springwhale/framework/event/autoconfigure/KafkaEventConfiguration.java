package io.github.springwhale.framework.event.autoconfigure;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.kafka.EventKafkaMessageConsumer;
import io.github.springwhale.framework.event.kafka.KafkaEventProperties;
import io.github.springwhale.framework.event.kafka.KafkaEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name = "spring.whale.event.mode", havingValue = "kafka")
public class KafkaEventConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventProperties kafkaEventProperties() {
        return new KafkaEventProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventPublisher kafkaEventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                                                   List<EventMetricsCollector> metricsCollectors,
                                                   KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaEventPublisher(properties, jsonMapper, metricsCollectors, kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventKafkaMessageConsumer eventKafkaMessageConsumer(ObjectMapper jsonMapper,
                                                               EventProperties eventProperties,
                                                               List<EventMetricsCollector> metricsCollectors,
                                                               Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                                               KafkaTemplate<String, String> kafkaTemplate) {
        return new EventKafkaMessageConsumer(jsonMapper, eventProperties, metricsCollectors,
                springListenerBeanMap, kafkaTemplate);
    }

}