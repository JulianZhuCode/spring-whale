package io.github.springwhale.framework.event;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for event listener.
 * <p>Note: Not recommend to use anonymous inner class to inherit this class.</p>
 *
 * @param <T> target event model type
 */
@Slf4j
public abstract class AbstractEventListener<T> {

    @Getter
    private final String businessName;
    @Getter
    private final Class<T> eventClass;

    protected AbstractEventListener(Class<T> eventClass) {
        this.eventClass = eventClass;
        this.businessName = computeBusinessName();
    }

    private String computeBusinessName() {
        Event event = AnnotationUtils.findAnnotation(eventClass, Event.class);
        if (event == null) {
            log.info("No event annotation found for {}", eventClass.getSimpleName());
            return eventClass.getSimpleName();
        }
        if (StringUtils.hasText(event.value())) {
            log.info("Resolved businessName from @Event annotation: {}", event.value());
            return event.value();
        }
        String errorMsg = "businessName cannot be resolved from @Event annotation, value must not be blank, eventClass:" + eventClass.getName();
        log.error(errorMsg);
        throw new IllegalArgumentException(errorMsg);
    }

    public abstract void onEvent(T event);

    public boolean isRetry() {
        return false;
    }
}
