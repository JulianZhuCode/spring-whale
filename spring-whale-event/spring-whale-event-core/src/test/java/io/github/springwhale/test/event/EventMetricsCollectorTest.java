package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.LogMetricsCollector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestEventConfiguration.class)
class EventMetricsCollectorTest {

    @Autowired
    private LogMetricsCollector logMetricsCollector;

    @Test
    @DisplayName("Should have LogMetricsCollector bean")
    void testLogMetricsCollectorBean() {
        assertNotNull(logMetricsCollector);
    }

    @Test
    @DisplayName("LogMetricsCollector should implement EventMetricsCollector SPI")
    void testLogMetricsCollectorImplementsSpi() {
        assertInstanceOf(EventMetricsCollector.class, logMetricsCollector);
    }

    @Test
    @DisplayName("Custom EventMetricsCollector should receive all lifecycle callbacks")
    void testCustomCollectorCallbacks() {
        List<String> events = new ArrayList<>();
        EventMetricsCollector customCollector = new EventMetricsCollector() {
            @Override
            public void onPublishSuccess(String topic, String businessName) {
                events.add("publishSuccess:" + topic + ":" + businessName);
            }

            @Override
            public void onPublishFailure(String topic, String businessName, Throwable error) {
                events.add("publishFailure:" + topic + ":" + businessName);
            }

            @Override
            public void onConsumeSuccess(String businessName, String listenerName) {
                events.add("consumeSuccess:" + businessName + ":" + listenerName);
            }

            @Override
            public void onConsumeFailure(String businessName, String listenerName, Throwable error) {
                events.add("consumeFailure:" + businessName + ":" + listenerName);
            }

            @Override
            public void onRetryScheduled(String messageId, String listenerName, int retryCount) {
                events.add("retryScheduled:" + messageId + ":" + listenerName + ":" + retryCount);
            }

            @Override
            public void onRetrySuccess(String messageId, String listenerName) {
                events.add("retrySuccess:" + messageId + ":" + listenerName);
            }

            @Override
            public void onRetryExhausted(String messageId, String listenerName, int retryCount) {
                events.add("retryExhausted:" + messageId + ":" + listenerName + ":" + retryCount);
            }

            @Override
            public void onConsumeLatency(String businessName, String listenerName, long durationMs, boolean success) {
                events.add("consumeLatency:" + businessName + ":" + listenerName + ":" + durationMs + ":" + success);
            }
        };

        customCollector.onPublishSuccess("topic1", "business1");
        customCollector.onPublishFailure("topic2", "business2", new RuntimeException("test"));
        customCollector.onConsumeSuccess("business3", "listener3");
        customCollector.onConsumeFailure("business4", "listener4", new RuntimeException("test"));
        customCollector.onRetryScheduled("msg-1", "listener5", 1);
        customCollector.onRetrySuccess("msg-2", "listener6");
        customCollector.onRetryExhausted("msg-3", "listener7", 3);
        customCollector.onConsumeLatency("business8", "listener8", 100, true);

        assertEquals(8, events.size());
        assertEquals("publishSuccess:topic1:business1", events.get(0));
        assertEquals("publishFailure:topic2:business2", events.get(1));
        assertEquals("consumeSuccess:business3:listener3", events.get(2));
        assertEquals("consumeFailure:business4:listener4", events.get(3));
        assertEquals("retryScheduled:msg-1:listener5:1", events.get(4));
        assertEquals("retrySuccess:msg-2:listener6", events.get(5));
        assertEquals("retryExhausted:msg-3:listener7:3", events.get(6));
        assertEquals("consumeLatency:business8:listener8:100:true", events.get(7));
    }

    @Test
    @DisplayName("Default EventMetricsCollector methods should not throw")
    void testDefaultMethodsNoOp() {
        EventMetricsCollector noop = new EventMetricsCollector() {};

        assertDoesNotThrow(() -> noop.onPublishSuccess("topic", "business"));
        assertDoesNotThrow(() -> noop.onPublishFailure("topic", "business", new RuntimeException()));
        assertDoesNotThrow(() -> noop.onConsumeSuccess("business", "listener"));
        assertDoesNotThrow(() -> noop.onConsumeFailure("business", "listener", new RuntimeException()));
        assertDoesNotThrow(() -> noop.onRetryScheduled("msg", "listener", 1));
        assertDoesNotThrow(() -> noop.onRetrySuccess("msg", "listener"));
        assertDoesNotThrow(() -> noop.onRetryExhausted("msg", "listener", 3));
        assertDoesNotThrow(() -> noop.onConsumeLatency("business", "listener", 100, true));
    }
}