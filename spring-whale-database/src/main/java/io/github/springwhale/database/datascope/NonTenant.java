package io.github.springwhale.database.datascope;

import java.lang.annotation.*;

/**
 * Marks a controller method to skip tenant-level SQL filtering.
 *
 * <p>Use on endpoints that access global data not tied to any tenant.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NonTenant {
}