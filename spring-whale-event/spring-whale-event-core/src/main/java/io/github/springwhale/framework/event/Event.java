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

    /**
     * Alias for {@link #businessName()}.
     */
    @AliasFor("businessName")
    String value();

    /**
     * The business name used for listener routing. Must be unique per event type.
     */
    @AliasFor("value")
    String businessName();

    /**
     * Override the default topic. If empty, the topic from {@code spring.whale.event.event-topic} is used.
     */
    String topic() default "";

}