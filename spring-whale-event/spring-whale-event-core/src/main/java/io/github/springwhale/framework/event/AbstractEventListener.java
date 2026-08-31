package io.github.springwhale.framework.event;

import jakarta.annotation.Nullable;
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
    private final Class<T> eventClass;
    @Nullable
    private final Event cachedEventAnnotation;

    protected AbstractEventListener(Class<T> eventClass) {
        this.eventClass = eventClass;
        this.cachedEventAnnotation = AnnotationUtils.findAnnotation(eventClass, Event.class);
        if (cachedEventAnnotation != null && !StringUtils.hasText(cachedEventAnnotation.value())) {
            String errorMsg = "businessName cannot be resolved from @Event annotation, value must not be blank, eventClass:"
                    + eventClass.getName();
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (cachedEventAnnotation == null) {
            log.info("No event annotation found for {}", eventClass.getSimpleName());
        } else {
            log.info("Resolved businessName from @Event annotation: {}", cachedEventAnnotation.value());
        }
    }

    /**
     * The business name used for listener routing.
     * <p>The default implementation reads the business name from the {@link Event} annotation
     * on the event class bound to this listener. When no {@code @Event} annotation is present,
     * it falls back to the event class simple name.</p>
     * <p>Override this method to customize the business name for this listener.</p>
     *
     * @return the business name
     */
    public String businessName() {
        if (cachedEventAnnotation != null) {
            return cachedEventAnnotation.value();
        }
        return eventClass.getSimpleName();
    }

    /**
     * Entry point called by the framework. Validates the event type and delegates to {@link #doEvent(Object, EventContext)}.
     * <p>Uses {@link Class#isInstance(Object)} and {@link Class#cast(Object)} for runtime type safety,
     * providing a clear error message when the deserialized object does not match the expected type {@code <T>}.</p>
     *
     * @param event        the event object (nullable — null events are silently ignored)
     * @param eventContext the event context with metadata
     * @throws ClassCastException if the event object is not an instance of {@code T}
     */
    public void onEvent(Object event, EventContext eventContext) {
        if (event == null) {
            log.info("No event received for {}", eventClass.getSimpleName());
            return;
        }
        if (!eventClass.isInstance(event)) {
            throw new ClassCastException(
                    "Event type mismatch: expected " + eventClass.getName()
                            + " but got " + event.getClass().getName());
        }
        doEvent(eventClass.cast(event), eventContext);
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

    /**
     * Whether to accept this event for processing. Default is {@code true} (accept all).
     * <p>Override this method to implement conditional filtering logic.
     * Events that are not accepted will be silently skipped without triggering
     * retry or failure handling.</p>
     * <p>Typical use cases: filter by event attributes (status, type, region, etc.),
     * or ignore events from certain sources.</p>
     *
     * @param event the event object (never null, already type-checked)
     * @return true if the event should be processed, false to skip
     */
    public boolean accept(Object event) {
        return true;
    }

    /**
     * The event versions this listener supports.
     * <p>The default implementation reads the version from the {@link Event} annotation
     * on the event class bound to this listener. When no {@code @Event} annotation is present,
     * it defaults to {@code {1}}.</p>
     * <p>Override this method to support multiple versions of an event type,
     * e.g. {@code return new int[] {1, 2}} to handle both v1 and v2.</p>
     *
     * @return the supported event versions
     */
    public int[] supportedVersions() {
        if (cachedEventAnnotation != null) {
            return new int[] { cachedEventAnnotation.version() };
        }
        return new int[] { Event.DEFAULT_VERSION };
    }
}