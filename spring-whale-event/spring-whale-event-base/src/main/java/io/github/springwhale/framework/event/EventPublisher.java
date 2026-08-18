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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class EventPublisher {
    protected final Map<Class<?>, Event> eventAnnotations = new ConcurrentHashMap<>();
    private final Set<Class<?>> noAnnotationClasses = ConcurrentHashMap.newKeySet();
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

}