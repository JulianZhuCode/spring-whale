package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.core.utils.SpringContextUtils;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

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

    protected Event findEventAnnotation(Object event) {
        Event eventAnnotation;
        if (eventAnnotations.containsKey(event.getClass())) {
            eventAnnotation = eventAnnotations.get(event.getClass());
        } else {
            eventAnnotation = AnnotationUtils.findAnnotation(event.getClass(), Event.class);
            eventAnnotations.put(event.getClass(), eventAnnotation);
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
