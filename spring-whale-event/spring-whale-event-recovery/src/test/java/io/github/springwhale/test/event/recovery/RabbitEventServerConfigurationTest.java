package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.recovery.autoconfigure.RabbitEventServerConfiguration;
import io.github.springwhale.framework.event.recovery.rabbit.RabbitEventConsumeFailedListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.whale.event.mode=rabbit",
        "spring.application.name=rabbit-test"
})
@Import({TestRecoveryConfiguration.class, RabbitEventServerConfiguration.class})
class RabbitEventServerConfigurationTest {

    @Autowired
    private RabbitEventConsumeFailedListener rabbitEventConsumeFailedListener;

    @Test
    @DisplayName("Should auto-configure RabbitEventConsumeFailedListener when mode is rabbit")
    void testRabbitEventConsumeFailedListener() {
        assertNotNull(rabbitEventConsumeFailedListener);
    }
}