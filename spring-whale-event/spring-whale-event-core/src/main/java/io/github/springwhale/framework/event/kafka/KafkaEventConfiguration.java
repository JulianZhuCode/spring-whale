package io.github.springwhale.framework.event.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaEventConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "spring.whale.event.kafka")
    public KafkaEventProperties kafkaEventProperties() {
        return new KafkaEventProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventPublisher kafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventKafkaMessageConsumer eventKafkaMessageConsumer(KafkaTemplate<String, String> kafkaTemplate) {
        return new EventKafkaMessageConsumer(kafkaTemplate);
    }

}