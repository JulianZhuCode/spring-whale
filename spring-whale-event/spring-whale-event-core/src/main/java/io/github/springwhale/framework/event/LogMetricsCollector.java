package io.github.springwhale.framework.event;

import lombok.extern.slf4j.Slf4j;

/**
 * Default {@link EventMetricsCollector} implementation that logs all metrics at DEBUG level.
 * <p>Not auto-registered as a Spring bean. To enable, declare it explicitly:</p>
 * <pre>{@code
 * @Bean
 * public LogMetricsCollector logMetricsCollector() {
 *     return new LogMetricsCollector();
 * }
 * }</pre>
 * <p>For production monitoring, implement {@link EventMetricsCollector} with Micrometer instead.</p>
 */
@Slf4j
public class LogMetricsCollector implements EventMetricsCollector {

    @Override
    public void onPublishSuccess(String topic, String businessName) {
        log.debug("Event published: topic={}, businessName={}", topic, businessName);
    }

    @Override
    public void onPublishFailure(String topic, String businessName, Throwable error) {
        log.warn("Event publish failed: topic={}, businessName={}", topic, businessName, error);
    }

    @Override
    public void onConsumeSuccess(String businessName, String listenerName) {
        log.debug("Event consumed: businessName={}, listener={}", businessName, listenerName);
    }

    @Override
    public void onConsumeFailure(String businessName, String listenerName, Throwable error) {
        log.warn("Event consume failed: businessName={}, listener={}", businessName, listenerName, error);
    }

    @Override
    public void onRetryScheduled(String messageId, String listenerName, int retryCount) {
        log.debug("Retry scheduled: messageId={}, listener={}, retryCount={}", messageId, listenerName, retryCount);
    }

    @Override
    public void onRetrySuccess(String messageId, String listenerName) {
        log.info("Retry success: messageId={}, listener={}", messageId, listenerName);
    }

    @Override
    public void onRetryExhausted(String messageId, String listenerName, int retryCount) {
        log.warn("Retry exhausted: messageId={}, listener={}, retryCount={}", messageId, listenerName, retryCount);
    }

    @Override
    public void onConsumeLatency(String businessName, String listenerName, long durationMs, boolean success) {
        log.debug("Consume latency: businessName={}, listener={}, durationMs={}, success={}",
                businessName, listenerName, durationMs, success);
    }

}