package io.github.springwhale.framework.event;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for event listeners.
 * <p>A listener is bound to a specific event type {@code <T>} and a business name
 * (derived from the {@link Event} annotation on the event class).</p>
 * <p>Note: Not recommended to use anonymous inner classes to inherit this class.</p>
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

    /**
     * Entry point called by the framework. Validates the event and delegates to {@link #doEvent(Object, EventContext)}.
     *
     * @param event       the event object (nullable — null events are silently ignored)
     * @param eventContext the event context with metadata
     */
    public void onEvent(Object event, EventContext eventContext) {
        if (event == null) {
            log.info("No event received for {}", eventClass.getSimpleName());
            return;
        }
        doEvent((T) event, eventContext);
    }

    /**
     * Process the event. Subclasses must implement the business logic here.
     *
     * @param event       the event object (never null)
     * @param eventContext the event context with metadata
     */
    public abstract void doEvent(T event, EventContext eventContext);

    /**
     * Whether retry is enabled for this listener. Default is {@code false}.
     * <p>When enabled, if {@link #doEvent(Object, EventContext)} throws an exception,
     * the framework will re-publish the message to the failed topic for retry processing.</p>
     *
     * @return true if retry is enabled
     */
    public boolean retryEnabled() {
        return false;
    }
}