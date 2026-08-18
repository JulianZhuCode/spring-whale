package io.github.springwhale.framework.event.metrics;

import io.github.springwhale.framework.event.EventMetricsCollector;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Micrometer-based implementation of {@link EventMetricsCollector}.
 * <p>Exposes the following counters for Prometheus / Grafana / Datadog dashboards:</p>
 * <ul>
 *   <li>{@value #METRIC_PUBLISH} — tags: {@code topic}, {@code business}, {@code result}</li>
 *   <li>{@value #METRIC_CONSUME} — tags: {@code business}, {@code listener}, {@code result}</li>
 *   <li>{@value #METRIC_RETRY} — tags: {@code listener}, {@code result}</li>
 * </ul>
 * <p>Counters are cached by tag combination to avoid repeated registry lookups.</p>
 */
public class MicrometerEventMetricsCollector implements EventMetricsCollector {

    private static final String METRIC_PUBLISH = "springwhale.event.publish";
    private static final String METRIC_CONSUME = "springwhale.event.consume";
    private static final String METRIC_RETRY = "springwhale.event.retry";

    private static final String TAG_TOPIC = "topic";
    private static final String TAG_BUSINESS = "business";
    private static final String TAG_LISTENER = "listener";
    private static final String TAG_RESULT = "result";

    private static final String RESULT_SUCCESS = "success";
    private static final String RESULT_FAILURE = "failure";
    private static final String RESULT_SCHEDULED = "scheduled";
    private static final String RESULT_EXHAUSTED = "exhausted";

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counterCache = new ConcurrentHashMap<>();

    public MicrometerEventMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void onPublishSuccess(String topic, String businessName) {
        counter(METRIC_PUBLISH, TAG_TOPIC, topic, TAG_BUSINESS, businessName, TAG_RESULT, RESULT_SUCCESS).increment();
    }

    @Override
    public void onPublishFailure(String topic, String businessName, Throwable error) {
        counter(METRIC_PUBLISH, TAG_TOPIC, topic, TAG_BUSINESS, businessName, TAG_RESULT, RESULT_FAILURE).increment();
    }

    @Override
    public void onConsumeSuccess(String businessName, String listenerName) {
        counter(METRIC_CONSUME, TAG_BUSINESS, businessName, TAG_LISTENER, listenerName, TAG_RESULT, RESULT_SUCCESS).increment();
    }

    @Override
    public void onConsumeFailure(String businessName, String listenerName, Throwable error) {
        counter(METRIC_CONSUME, TAG_BUSINESS, businessName, TAG_LISTENER, listenerName, TAG_RESULT, RESULT_FAILURE).increment();
    }

    @Override
    public void onRetryScheduled(String messageId, String listenerName, int retryCount) {
        counter(METRIC_RETRY, TAG_LISTENER, listenerName, TAG_RESULT, RESULT_SCHEDULED).increment();
    }

    @Override
    public void onRetrySuccess(String messageId, String listenerName) {
        counter(METRIC_RETRY, TAG_LISTENER, listenerName, TAG_RESULT, RESULT_SUCCESS).increment();
    }

    @Override
    public void onRetryExhausted(String messageId, String listenerName, int retryCount) {
        counter(METRIC_RETRY, TAG_LISTENER, listenerName, TAG_RESULT, RESULT_EXHAUSTED).increment();
    }

    private Counter counter(String name, String... tags) {
        String cacheKey = name + ":" + String.join(",", tags);
        return counterCache.computeIfAbsent(cacheKey, k -> Counter.builder(name)
                .tags(tags)
                .register(meterRegistry));
    }
}