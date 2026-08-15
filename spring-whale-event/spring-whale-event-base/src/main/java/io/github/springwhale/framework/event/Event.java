package io.github.springwhale.framework.event;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Event {

    @AliasFor("businessName")
    String value();

    @AliasFor("value")
    String businessName();

    String topic() default "";

}
