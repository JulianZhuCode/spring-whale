package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.rabbit.RabbitEventMessageConsumer;
import io.github.springwhale.framework.event.rabbit.RabbitEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest(properties = {
        "spring.whale.event.mode=rabbit",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
@Import(RabbitEventConfigurationTest.RabbitTestConfig.class)
class RabbitEventConfigurationTest {

    @TestConfiguration
    static class RabbitTestConfig {
        @Bean
        public RabbitTemplate rabbitTemplate() {
            return mock(RabbitTemplate.class);
        }
    }

    @Autowired
    private RabbitEventPublisher rabbitEventPublisher;

    @Autowired
    private RabbitEventMessageConsumer rabbitEventMessageConsumer;

    @Autowired
    private EventProperties eventProperties;

    @Test
    @DisplayName("Should auto-configure RabbitEventPublisher when mode is rabbit")
    void testRabbitEventPublisher() {
        assertNotNull(rabbitEventPublisher);
    }

    @Test
    @DisplayName("Should auto-configure RabbitEventMessageConsumer when mode is rabbit")
    void testRabbitEventMessageConsumer() {
        assertNotNull(rabbitEventMessageConsumer);
    }

    @Test
    @DisplayName("Should auto-configure EventProperties")
    void testEventProperties() {
        assertNotNull(eventProperties);
    }
}