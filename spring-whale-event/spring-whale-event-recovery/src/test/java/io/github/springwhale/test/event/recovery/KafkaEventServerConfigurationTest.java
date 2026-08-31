package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.recovery.autoconfigure.KafkaEventServerConfiguration;
import io.github.springwhale.framework.event.recovery.kafka.KafkaEventConsumeFailedListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.whale.event.mode=kafka",
        "spring.application.name=kafka-test"
})
@Import({TestRecoveryConfiguration.class, KafkaEventServerConfiguration.class})
class KafkaEventServerConfigurationTest {

    @Autowired
    private KafkaEventConsumeFailedListener kafkaEventConsumeFailedListener;

    @Test
    @DisplayName("Should auto-configure KafkaEventConsumeFailedListener")
    void testKafkaEventConsumeFailedListener() {
        assertNotNull(kafkaEventConsumeFailedListener);
    }
}