package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.RetryStrategy;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestEventConfiguration.class)
class RetryStrategyRegistryTest {

    @Autowired
    private RetryStrategyRegistry registry;

    @Test
    @DisplayName("Should have built-in fixed strategy")
    void testFixedStrategy() {
        RetryStrategy strategy = registry.get("fixed");
        assertNotNull(strategy);

        long delay = strategy.calculateDelay(5, 300, 1);
        assertEquals(5, delay);

        delay = strategy.calculateDelay(5, 300, 10);
        assertEquals(5, delay);
    }

    @Test
    @DisplayName("Should have built-in exponential strategy")
    void testExponentialStrategy() {
        RetryStrategy strategy = registry.get("exponential");
        assertNotNull(strategy);

        long delay = strategy.calculateDelay(5, 300, 1);
        assertEquals(5, delay);

        delay = strategy.calculateDelay(5, 300, 2);
        assertEquals(10, delay);

        delay = strategy.calculateDelay(5, 300, 3);
        assertEquals(20, delay);
    }

    @Test
    @DisplayName("Should throw for unknown strategy name")
    void testUnknownStrategy() {
        assertThrows(IllegalArgumentException.class, () -> registry.get("unknown"));
    }

    @Test
    @DisplayName("Should throw for null strategy name")
    void testNullStrategyName() {
        assertThrows(NullPointerException.class, () -> registry.get(null));
    }

    @Test
    @DisplayName("Exponential strategy should cap at maxInterval")
    void testExponentialStrategyCapped() {
        RetryStrategy strategy = registry.get("exponential");

        long delay = strategy.calculateDelay(5, 10, 1);
        assertEquals(5, delay);

        delay = strategy.calculateDelay(5, 10, 2);
        assertEquals(10, delay);

        delay = strategy.calculateDelay(5, 10, 3);
        assertEquals(10, delay);
    }

    @Test
    @DisplayName("Exponential strategy with large retryCount should cap")
    void testExponentialStrategyLargeRetry() {
        RetryStrategy strategy = registry.get("exponential");

        long delay = strategy.calculateDelay(5, 300, 10);
        assertEquals(300, delay);
    }
}