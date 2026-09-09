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
    private final EventDedupHandler dedupHandler;
    private final Map<String, AbstractEventListener<?>> springListenerBeanMap;

    @Getter
    private volatile Map<AbstractEventListener<?>, String> listenerInstanceToNameMap = Collections.emptyMap();

    @Getter
    private volatile Map<String, AbstractEventListener<?>> listenerNameToInstanceMap = Collections.emptyMap();

    @Getter
    private volatile Map<String, List<AbstractEventListener<?>>> listenerGroup = Collections.emptyMap();

    public EventMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                List<EventMetricsCollector> metricsCollectors,
                                Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                EventDedupHandler dedupHandler) {
        this.jsonMapper = jsonMapper;
        this.eventProperties = eventProperties;
        this.metricsCollectors = metricsCollectors != null ? metricsCollectors : Collections.emptyList();
        this.dedupHandler = dedupHandler;
        this.springListenerBeanMap = springListenerBeanMap;
        rebuildRouteTable();
    }

    /**
     * Check whether the message version is supported by the listener.
     * <p>A {@code null} version on the message is treated as version 1 for backward compatibility.</p>
     */
    private static boolean versionMatches(EventMessage message, AbstractEventListener<?> listener) {
        int eventVersion = message.getVersion() != null ? message.getVersion() : Event.DEFAULT_VERSION;
        int[] supported = listener.supportedVersions();
        for (int v : supported) {
            if (v == eventVersion) {
                return true;
            }
        }
        return false;
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
                .collect(Collectors.groupingBy(AbstractEventListener::businessName));

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
            // Wire the global idempotency handler and the registered name into the
            // listener. Name is set first: a reader can then never observe a handler
            // without its name (worst case it sees neither -> no dedup).
            listener.setIdempotencyName(name);
            listener.setDedupHandler(dedupHandler);
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
        if (listeners == null || listeners.isEmpty()) {
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
        boolean locked = false;
        if (dedupHandler != null) {
            try {
                dedupHandler.lock(message.getId());
                locked = true;
            } catch (Exception ex) {
                log.error("Message lock failed, proceed without lock: messageId={}", message.getId(), ex);
            }
        }
        try {
            if (message.getTraceId() != null) {
                MDC.put("traceId", message.getTraceId());
            }
            if (message.getAuthenticationContext() != null) {
                AuthenticationContextHolder.setContext(message.getAuthenticationContext());
            }
            for (AbstractEventListener<?> listener : listeners) {
                if (listener == null) {
                    // Multi-module/distributed scenario: the retry target may be
                    // registered on another module or instance. Skip, do not NPE,
                    // do not ack-discard or converge the failed record.
                    log.warn("Retry target listener not found, skip: messageId={}", message.getId());
                    continue;
                }
                String listenerName = getListenerInstanceToNameMap().get(listener);

                // Idempotency: delegate to the listener - the global EventDedupHandler
                // default or a per-listener override.
                if (listener.isProcessed(message)) {
                    log.debug("Duplicate event skipped: messageId={}, listener={}", message.getId(), listenerName);
                    if (MessageType.RETRY == message.getMessageType()) {
                        // The retry already succeeded but its completion signal may have been lost
                        // (e.g. sendToFailedTopic failed after success). Re-emit RETRY_SUCCESS so the
                        // failed record converges to REPLAY_SUCCESS instead of being stuck in PENDING_RETRY.
                        message.setMessageType(MessageType.RETRY_SUCCESS);
                        sendToFailedTopic(message);
                    }
                    continue;
                }

                long start = System.currentTimeMillis();
                boolean success = true;
                try {
                    var event = jsonMapper.readValue(message.getData(), listener.getEventClass());
                    if (!versionMatches(message, listener)) {
                        log.debug("Listener [{}] skipped event due to version mismatch: event version={}, supported={}",
                                listener.businessName(), message.getVersion(), listener.supportedVersions());
                        continue;
                    }
                    if (!listener.accept(event)) {
                        log.debug("Listener [{}] skipped event due to accept filter", listener.businessName());
                        continue;
                    }
                    listener.onEvent(event, context);
                    // Mark after successful processing (fail-open by the listener default).
                    listener.markProcessed(message);
                    onConsumeSuccess(message.getBusinessName(), listenerName);
                } catch (Exception e) {
                    success = false;
                    log.error("Listener [{}] failed to consume message [{}].", listener.businessName(), message.getData(), e);
                    // Notify the listener (fail-open by the listener default) so claim-based
                    // implementations can release the claim and the retry is not treated as a duplicate.
                    listener.markFailed(message, e);
                    message.setErrorStack(ExceptionUtil.getStackTrace(e));
                    message.setRetryEnabled(listener.retryEnabled());
                    message.setFailListener(listenerName);
                    message.setMessageType(MessageType.FAIL);
                    sendToFailedTopic(message);
                    onConsumeFailure(message.getBusinessName(), listenerName, e);
                    continue;
                } finally {
                    long durationMs = System.currentTimeMillis() - start;
                    boolean finalSuccess = success;
                    metricsCollectors.forEach(c -> {
                        try {
                            c.onConsumeLatency(
                                    message.getBusinessName(), listenerName, durationMs, finalSuccess);
                        } catch (Exception ex) {
                            log.error("Metrics onConsumeLatency failed: businessName={}",
                                    message.getBusinessName(), ex);
                        }
                    });
                }
                if (MessageType.RETRY == message.getMessageType()) {
                    message.setMessageType(MessageType.RETRY_SUCCESS);
                    sendToFailedTopic(message);
                }
            }
        } finally {
            AuthenticationContextHolder.clearContext();
            MDC.remove("traceId");
            if (locked) {
                try {
                    dedupHandler.unlock(message.getId());
                } catch (Exception ex) {
                    log.error("Message unlock failed: messageId={}", message.getId(), ex);
                }
            }
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
        metricsCollectors.forEach(c -> {
            try {
                c.onConsumeSuccess(businessName, listenerName);
            } catch (Exception ex) {
                log.error("Metrics onConsumeSuccess failed: businessName={}", businessName, ex);
            }
        });
    }

    /**
     * Notify all registered {@link EventMetricsCollector}s of a failed consume.
     * <p>Called when a listener throws an exception.</p>
     */
    protected void onConsumeFailure(String businessName, String listenerName, Throwable error) {
        metricsCollectors.forEach(c -> {
            try {
                c.onConsumeFailure(businessName, listenerName, error);
            } catch (Exception ex) {
                log.error("Metrics onConsumeFailure failed: businessName={}", businessName, ex);
            }
        });
    }
}
