package io.github.springwhale.framework.event;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation to mark an event class and configure its routing metadata.
 * <p>Attach this annotation to any POJO that represents a domain event.
 * The {@code businessName} identifies the event type for listener routing,
 * and the {@code topic} overrides the default MQ topic.</p>
 * <p>If the annotation is absent, the framework uses the class simple name
 * as the business name and the default topic from properties.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Event {

    int DEFAULT_VERSION = 1;

    /**
     * Alias for {@link #businessName()}.
     */
    @AliasFor("businessName")
    String value() default "";

    /**
     * The business name used for listener routing. Must be unique per event type.
     */
    @AliasFor("value")
    String businessName() default "";

    /**
     * Override the default topic. If empty, the topic from {@code spring.whale.event.event-topic} is used.
     */
    String topic() default "";

    /**
     * Event schema version. Used to support multiple versions of the same event type.
     * <p>Listeners declare which versions they support via {@link AbstractEventListener#supportedVersions()}.
     * Events with a version not supported by the listener will be silently skipped.</p>
     *
     * @return the event version, defaults to {@link #DEFAULT_VERSION} (1)
     */
    int version() default DEFAULT_VERSION;

}