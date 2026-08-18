package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.core.utils.SpringContextUtils;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EventPublisher {
    protected final Map<Class<?>, Event> eventAnnotations = new HashMap<>();
    @Autowired
    protected EventProperties properties;
    @Autowired
    protected ObjectMapper jsonMapper;

    public abstract void publish(@Valid Object event);

    public abstract void publish(@Valid Object event, PublishOption option);

    public abstract void publish(@Valid EventMessage message);

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
     * <p>Results are cached in a {@link ConcurrentHashMap}. Null results are NOT cached
     * because ConcurrentHashMap does not allow null values, so unannotated classes will
     * re-compute on each call (acceptable given Spring's internal annotation cache).</p>
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
        Event eventAnnotation = AnnotationUtils.findAnnotation(clazz, Event.class);
        if (eventAnnotation != null) {
            eventAnnotations.put(clazz, eventAnnotation);
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

}