package io.github.springwhale.framework.event.server.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaEventServerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventConsumeFailedListener kafkaEventConsumeFailedListener() {
        return new KafkaEventConsumeFailedListener();
    }

}