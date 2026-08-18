package io.github.springwhale.framework.event;

/**
 * SPI interface for collecting event framework metrics.
 * <p>All methods are {@code default} with empty bodies — implementations only
 * override the methods they care about. The framework calls these hooks at
 * key lifecycle points.</p>
 * <p>Implementations are discovered via {@code @Autowired(required = false) List<EventMetricsCollector>}
 * and invoked in insertion order. A no-op default is used when no collector is registered.</p>
 */
public interface EventMetricsCollector {

    /** Called when an event is successfully published to the MQ broker. */
    default void onPublishSuccess(String topic, String businessName) {}

    /** Called when an event publish attempt fails. */
    default void onPublishFailure(String topic, String businessName, Throwable error) {}

    /** Called when a listener successfully processes an event. */
    default void onConsumeSuccess(String businessName, String listenerName) {}

    /** Called when a listener throws an exception during event processing. */
    default void onConsumeFailure(String businessName, String listenerName, Throwable error) {}

    /** Called when a retry message is scheduled (re-published to the MQ broker). */
    default void onRetryScheduled(String messageId, String listenerName, int retryCount) {}

    /** Called when a retry finally succeeds. */
    default void onRetrySuccess(String messageId, String listenerName) {}

    /** Called when retries are exhausted and the message is discarded. */
    default void onRetryExhausted(String messageId, String listenerName, int retryCount) {}

}