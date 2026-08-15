package io.github.springwhale.framework.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Abstract base class for event listener.
 * <p>Note: Do NOT use anonymous inner class to inherit this class, generic type resolution will fail.</p>
 *
 * @param <T> target event model type
 */
@Slf4j
public abstract class AbstractEventListener<T> {

    private volatile String businessName;
    private volatile Class<?> eventClass;

    public abstract void onEvent(T event);

    public boolean isRetry() {
        return false;
    }


    public Class<?> getEventClass() {
        if (eventClass != null) {
            return eventClass;
        }
        synchronized (this) {
            if (eventClass != null) {
                return eventClass;
            }
            var type = getEventType();
            if (!(type instanceof Class<?>)) {
                throw new IllegalArgumentException("Event generic argument must be concrete Class, actual type:" + type);
            }
            eventClass = (Class<?>) type;
            return eventClass;
        }
    }

    private Type getEventType() {
        Type genericSuper = this.getClass().getGenericSuperclass();
        if (!(genericSuper instanceof ParameterizedType pt)) {
            throw new IllegalArgumentException("Superclass must be parameterized with event type, do not omit generic argument.");
        }
        Type[] actualTypes = pt.getActualTypeArguments();
        if (actualTypes.length == 0) {
            throw new IllegalArgumentException("No generic type argument found for event listener.");
        }
        return actualTypes[0];
    }

    public String getBusinessName() {
        if (businessName != null) {
            return businessName;
        }
        synchronized (this) {
            if (businessName != null) {
                return businessName;
            }
            Class<?> clazz = getEventClass();
            Event event = AnnotationUtils.findAnnotation(clazz, Event.class);
            if (event == null) {
                log.info("No event annotation found for {}", clazz.getSimpleName());
                businessName = clazz.getSimpleName();
            } else if (StringUtils.hasText(event.value())) {
                log.info("Resolved businessName from @Event annotation: {}", event.value());
                businessName = event.value();
            }
            if (StringUtils.hasText(businessName)) {
                return businessName;
            }
            throw new IllegalArgumentException("businessName cannot be resolved from @Event annotation, value must not be blank");
        }
    }
}
