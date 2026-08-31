package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.kafka.KafkaEventPublisher;
import io.github.springwhale.framework.event.kafka.KafkaEventProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.whale.event.mode=kafka")
@Import(TestEventConfiguration.class)
class KafkaEventConfigurationTest {

    @Autowired
    private KafkaEventProperties kafkaEventProperties;

    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

    @Autowired
    private EventProperties eventProperties;

    @Test
    @DisplayName("Should auto-configure KafkaEventProperties")
    void testKafkaEventProperties() {
        assertNotNull(kafkaEventProperties);
        assertTrue(kafkaEventProperties.getAutoOffsetReset().length() > 0);
    }

    @Test
    @DisplayName("Should auto-configure KafkaEventPublisher")
    void testKafkaEventPublisher() {
        assertNotNull(kafkaEventPublisher);
    }

    @Test
    @DisplayName("Should auto-configure EventProperties")
    void testEventProperties() {
        assertNotNull(eventProperties);
    }
}