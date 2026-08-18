package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.core.utils.ExceptionUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Abstract MQ event message consumer.
 * <p>Merges listeners from Spring container and manual registration, builds runtime routing tables.
 * {@code addListener} / {@code removeListener} support runtime modification; each call triggers
 * a full routing-table rebuild. All routing maps are replaced atomically via volatile field
 * assignment, ensuring consumer threads always see a consistent view.</p>
 */
@Slf4j
public abstract class EventMessageConsumer {

    protected final ObjectMapper jsonMapper;
    protected final EventProperties eventProperties;
    private final Map<String, AbstractEventListener<?>> customRegisterMap = new ConcurrentHashMap<>();
    private final List<EventMetricsCollector> metricsCollectors;
    private final Map<String, AbstractEventListener<?>> springListenerBeanMap;

    @Getter
    private volatile Map<AbstractEventListener<?>, String> listenerInstanceToNameMap = Collections.emptyMap();

    @Getter
    private volatile Map<String, AbstractEventListener<?>> listenerNameToInstanceMap = Collections.emptyMap();

    @Getter
    private volatile Map<String, List<AbstractEventListener<?>>> listenerGroup = Collections.emptyMap();

    public EventMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                List<EventMetricsCollector> metricsCollectors,
                                Map<String, AbstractEventListener<?>> springListenerBeanMap) {
        this.jsonMapper = jsonMapper;
        this.eventProperties = eventProperties;
        this.metricsCollectors = metricsCollectors != null ? metricsCollectors : Collections.emptyList();
        this.springListenerBeanMap = springListenerBeanMap;
        rebuildRouteTable();
    }

    public boolean listenerIsEmpty() {
        return CollectionUtils.isEmpty(this.listenerGroup);
    }

    public void addListener(String name, AbstractEventListener<?> listener) {
        customRegisterMap.put(name, listener);
        rebuildRouteTable();
    }

    public void removeListener(String name) {
        customRegisterMap.remove(name);
        rebuildRouteTable();
    }

    /**
     * Force rebuild all routing tables. Avoid frequent calls at runtime.
     */
    public void refreshListeners() {
        rebuildRouteTable();
    }

    /**
     * Rebuild all routing tables from combined listener sources.
     */
    private void rebuildRouteTable() {
        Map<String, AbstractEventListener<?>> allListenersMap = new HashMap<>(
                Optional.ofNullable(springListenerBeanMap).orElse(Collections.emptyMap())
        );
        allListenersMap.putAll(customRegisterMap);

        if (CollectionUtils.isEmpty(allListenersMap)) {
            this.listenerGroup = Collections.emptyMap();
            this.listenerNameToInstanceMap = Collections.emptyMap();
            this.listenerInstanceToNameMap = Collections.emptyMap();
            return;
        }

        Collection<AbstractEventListener<?>> allListeners = allListenersMap.values();
        Map<String, List<AbstractEventListener<?>>> groupMap = allListeners.stream()
                .collect(Collectors.groupingBy(AbstractEventListener::getBusinessName));

        Map<String, AbstractEventListener<?>> tempNameToInstance = new HashMap<>(allListenersMap.size());
        Map<AbstractEventListener<?>, String> tempInstanceToName = new HashMap<>(allListenersMap.size());

        for (Map.Entry<String, AbstractEventListener<?>> entry : allListenersMap.entrySet()) {
            String name = entry.getKey();
            AbstractEventListener<?> listener = entry.getValue();
            if (tempInstanceToName.containsKey(listener)) {
                throw new IllegalStateException(
                        "Listener instance already bound to name[" + tempInstanceToName.get(listener)
                                + "], cannot rebind to name[" + name + "]"
                );
            }
            tempNameToInstance.put(name, listener);
            tempInstanceToName.put(listener, name);
        }

        this.listenerGroup = Collections.unmodifiableMap(groupMap);
        this.listenerNameToInstanceMap = Collections.unmodifiableMap(tempNameToInstance);
        this.listenerInstanceToNameMap = Collections.unmodifiableMap(tempInstanceToName);
    }

    /**
     * Consume a raw payload string from the MQ broker.
     * <p>Handles empty-listener check, null-payload check, deserialization, and routing.
     * Deserialization failures are treated as unrecoverable — the message is logged and
     * the success callback is invoked (e.g. ack the malformed message).</p>
     * <p>If {@link #handleMessage} throws (e.g. database unavailable), the exception
     * propagates to the caller and the success callback is NOT invoked, allowing the
     * MQ broker to re-deliver the message.</p>
     *
     * @param rawPayload the raw JSON payload from the MQ broker
     * @param context    the event context built from MQ-specific metadata (timestamp, topic, etc.)
     * @param onSuccess  callback invoked on successful processing or unrecoverable failure (deserialization error)
     */
    protected void consumeRawMessage(String rawPayload, EventContext context, Runnable onSuccess) {
        if (listenerIsEmpty()) {
            onSuccess.run();
            return;
        }
        if (rawPayload == null) {
            onSuccess.run();
            return;
        }
        EventMessage message;
        try {
            message = jsonMapper.readValue(rawPayload, EventMessage.class);
        } catch (JacksonException e) {
            log.error("Failed to deserialize event message: {}", rawPayload, e);
            onSuccess.run();
            return;
        }
        handleMessage(message, context);
        onSuccess.run();
    }

    /**
     * Route the message to matching listeners and dispatch.
     * <p>MQ-specific subclasses call this after deserializing the raw message.</p>
     *
     * @param message the deserialized event message
     * @param context the event context (built from MQ-specific metadata)
     * @return true if the message was dispatched, false if it should be ignored
     */
    protected boolean handleMessage(EventMessage message, EventContext context) {
        List<AbstractEventListener<?>> listeners;
        switch (message.getMessageType()) {
            case EVENT:
                listeners = getListenerGroup().get(message.getBusinessName());
                break;
            case RETRY:
                listeners = Collections.singletonList(getListenerNameToInstanceMap().get(message.getFailListener()));
                break;
            default:
                return false;
        }
        if (listeners == null) {
            return false;
        }
        dispatchToListeners(context, listeners, message);
        return true;
    }

    /**
     * Dispatch the message to each matching listener.
     * <p>Each listener failure is handled independently: the exception is caught per-listener,
     * the error info is recorded on the message, and the message is sent to the failed topic
     * via {@link #sendToFailedTopic(EventMessage)} for retry processing.</p>
     * <p>Trace ID and authentication context are restored on the current thread before
     * dispatching (if present on the message) and cleared in the finally block,
     * ensuring no cross-message context leakage.</p>
     */
    private void dispatchToListeners(EventContext context, List<AbstractEventListener<?>> listeners, EventMessage message) {
        try {
            if (message.getTraceId() != null) {
                MDC.put("traceId", message.getTraceId());
            }
            if (message.getAuthenticationContext() != null) {
                AuthenticationContextHolder.setContext(message.getAuthenticationContext());
            }
            for (AbstractEventListener<?> listener : listeners) {
                long start = System.currentTimeMillis();
                boolean success = true;
                try {
                    var event = jsonMapper.readValue(message.getData(), listener.getEventClass());
                    listener.onEvent(event, context);
                    onConsumeSuccess(message.getBusinessName(), getListenerInstanceToNameMap().get(listener));
                } catch (Exception e) {
                    success = false;
                    log.error("Listener [{}] failed to consume message [{}].", listener.getBusinessName(), message.getData(), e);
                    onConsumeFailure(message.getBusinessName(), getListenerInstanceToNameMap().get(listener), e);
                    message.setErrorStack(ExceptionUtil.getStackTrace(e));
                    message.setRetryEnabled(listener.retryEnabled());
                    message.setFailListener(getListenerInstanceToNameMap().get(listener));
                    message.setMessageType(MessageType.FAIL);
                    sendToFailedTopic(message);
                } finally {
                    long durationMs = System.currentTimeMillis() - start;
                    boolean finalSuccess = success;
                    metricsCollectors.forEach(c -> c.onConsumeLatency(
                            message.getBusinessName(), getListenerInstanceToNameMap().get(listener), durationMs, finalSuccess));
                }
                if (MessageType.RETRY == message.getMessageType()) {
                    message.setRetrySuccess(true);
                    sendToFailedTopic(message);
                }
            }
        } finally {
            AuthenticationContextHolder.clearContext();
            MDC.remove("traceId");
        }
    }

    /**
     * Send the message to the failed topic for retry processing.
     * <p>Implemented by MQ-specific subclasses. The implementation must be synchronous
     * (blocking with bounded timeout) to guarantee the message is persisted before
     * the consumer acknowledges the original message.</p>
     *
     * @param message the event message to send to the failed topic
     * @throws RuntimeException if the send fails
     */
    protected abstract void sendToFailedTopic(EventMessage message);

    /**
     * Notify all registered {@link EventMetricsCollector}s of a successful consume.
     * <p>Called after a listener processes the event without error.</p>
     */
    protected void onConsumeSuccess(String businessName, String listenerName) {
        metricsCollectors.forEach(c -> c.onConsumeSuccess(businessName, listenerName));
    }

    /**
     * Notify all registered {@link EventMetricsCollector}s of a failed consume.
     * <p>Called when a listener throws an exception.</p>
     */
    protected void onConsumeFailure(String businessName, String listenerName, Throwable error) {
        metricsCollectors.forEach(c -> c.onConsumeFailure(businessName, listenerName, error));
    }
}