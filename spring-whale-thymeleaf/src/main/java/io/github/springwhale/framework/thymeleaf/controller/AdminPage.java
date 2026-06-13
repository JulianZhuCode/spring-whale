package io.github.springwhale.framework.thymeleaf.controller;

import java.lang.annotation.*;

/**
 * Marker annotation for admin console page controllers.
 * <p>
 * All {@code @Controller} classes serving admin pages should carry this annotation
 * so that {@link AdminControllerAdvice} can inject shared model attributes
 * (menu tree, current path) into every request handled by them.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminPage {
}
