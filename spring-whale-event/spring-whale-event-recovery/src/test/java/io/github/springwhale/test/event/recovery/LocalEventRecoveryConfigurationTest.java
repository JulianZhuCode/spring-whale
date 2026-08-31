package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.recovery.local.LocalEventConsumeFailedListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.application.name=local-test",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
                "io.github.springwhale.framework.event.autoconfigure.KafkaEventConfiguration," +
                "io.github.springwhale.framework.event.autoconfigure.RabbitEventConfiguration," +
                "io.github.springwhale.framework.event.autoconfigure.LocalEventConfiguration"
})
@Import(TestRecoveryConfiguration.class)
class LocalEventRecoveryConfigurationTest {

    @Autowired
    private LocalEventConsumeFailedListener localEventConsumeFailedListener;

    @Test
    @DisplayName("Should auto-configure LocalEventConsumeFailedListener when mode is local")
    void testLocalEventConsumeFailedListener() {
        assertNotNull(localEventConsumeFailedListener);
    }
}