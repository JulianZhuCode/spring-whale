package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.core.utils.SpringContextUtils;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EventPublisher {
    protected final Map<Class<?>, Event> eventAnnotations = new ConcurrentHashMap<>();
    private final Set<Class<?>> noAnnotationClasses = ConcurrentHashMap.newKeySet();
    protected final EventProperties properties;
    protected final ObjectMapper jsonMapper;
    private final List<EventMetricsCollector> metricsCollectors;

    public EventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                          List<EventMetricsCollector> metricsCollectors) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.metricsCollectors = metricsCollectors != null ? metricsCollectors : Collections.emptyList();
    }

    /**
     * Publish an event object. The {@link Event} annotation is optional —
     * if not present, defaults are derived from the class name and properties.
     */
    public void publish(@Valid Object event) {
        Assert.notNull(event, "event must not be null");
        Event eventAnnotation = findEventAnnotation(event);
        String businessName = buildBusinessName(event, eventAnnotation);
        String topic = buildTopic(eventAnnotation);
        send(event, businessName, topic);
    }

    /**
     * Publish an event with explicit overrides. Non-null fields in {@link PublishOption}
     * take precedence over annotation values and defaults.
     */
    public void publish(@Valid Object event, PublishOption option) {
        Assert.notNull(event, "event must not be null");
        if (option == null) {
            publish(event);
            return;
        }
        String businessName = option.businessName();
        String topic = option.topic();
        if (StringUtils.hasText(businessName) && StringUtils.hasText(topic)) {
            send(event, businessName, topic);
            return;
        }
        Event eventAnnotation = findEventAnnotation(event);
        if (!StringUtils.hasText(businessName)) {
            businessName = buildBusinessName(event, eventAnnotation);
        }
        if (!StringUtils.hasText(topic)) {
            topic = buildTopic(eventAnnotation);
        }
        send(event, businessName, topic);
    }

    /**
     * Publish a pre-built {@link EventMessage} directly.
     */
    public void publish(@Valid EventMessage message) {
        Assert.notNull(message, "message must not be null");
        send(message);
    }

    protected String buildBusinessName(Object event, Event eventAnnotation) {
        return (eventAnnotation != null && StringUtils.hasText(eventAnnotation.businessName())) ? eventAnnotation.businessName() : event.getClass().getSimpleName();
    }

    protected String buildTopic(Event eventAnnotation) {
        return (eventAnnotation != null && StringUtils.hasText(eventAnnotation.topic())) ? eventAnnotation.topic() : properties.getEventTopic();
    }

    /**
     * Find the {@link Event} annotation on the given event class.
     * <p>The {@code @Event} annotation is optional. If not present, this method returns {@code null}
     * and the caller should fall back to default values (class simple name as business name,
     * default topic from properties).</p>
     * <p>Both positive (annotation found) and negative (no annotation) results are cached.
     * Positive results are stored in {@link #eventAnnotations}, negative results are tracked
     * via {@code noAnnotationClasses} (a {@link ConcurrentHashMap}-backed {@link Set}),
     * so that unannotated classes also avoid repeated annotation lookups.</p>
     *
     * @param event the event object
     * @return the {@link Event} annotation, or {@code null} if not annotated
     */
    @Nullable
    protected Event findEventAnnotation(Object event) {
        Class<?> clazz = event.getClass();
        if (eventAnnotations.containsKey(clazz)) {
            return eventAnnotations.get(clazz);
        }
        if (noAnnotationClasses.contains(clazz)) {
            return null;
        }
        Event eventAnnotation = AnnotationUtils.findAnnotation(clazz, Event.class);
        if (eventAnnotation != null) {
            eventAnnotations.put(clazz, eventAnnotation);
        } else {
            noAnnotationClasses.add(clazz);
        }
        return eventAnnotation;
    }

    protected @NonNull EventMessage buildEventMessage(Object event, String businessName, String topic) {
        EventMessage message = new EventMessage();
        message.setSource(SpringContextUtils.getApplicationName());
        message.setBusinessName(businessName);
        message.setTopic(topic);
        message.setData(jsonMapper.writeValueAsString(event));
        message.setAuthenticationContext(AuthenticationContextHolder.getContext());
        return message;
    }

    private void send(Object event, String businessName, String topic) {
        EventMessage message = buildEventMessage(event, businessName, topic);
        send(message);
    }

    /**
     * Send the event message to the MQ broker synchronously via {@link #doSend(EventMessage)}.
     * <p>Wraps the MQ-specific send with metrics collection (success/failure).</p>
     */
    private void send(EventMessage message) {
        try {
            doSend(message);
            onPublishSuccess(message);
        } catch (Exception e) {
            onPublishFailure(message, e);
            throw new RuntimeException("send event to MQ failed", e);
        }
    }

    /**
     * MQ-specific send implementation.
     * <p>Subclasses must implement the actual send to the target MQ broker
     * (Kafka, RocketMQ, RabbitMQ, etc.). The call must be synchronous with
     * a bounded timeout.</p>
     *
     * @param message the event message to send
     * @throws Exception if the send fails
     */
    protected abstract void doSend(EventMessage message) throws Exception;

    /**
     * Notify all registered {@link EventMetricsCollector}s of a successful publish.
     */
    protected void onPublishSuccess(EventMessage message) {
        metricsCollectors.forEach(c -> c.onPublishSuccess(message.getTopic(), message.getBusinessName()));
    }

    /**
     * Notify all registered {@link EventMetricsCollector}s of a failed publish.
     */
    protected void onPublishFailure(EventMessage message, Throwable error) {
        metricsCollectors.forEach(c -> c.onPublishFailure(message.getTopic(), message.getBusinessName(), error));
    }

}