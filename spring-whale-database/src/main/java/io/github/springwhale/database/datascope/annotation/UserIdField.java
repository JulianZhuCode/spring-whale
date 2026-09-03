package io.github.springwhale.database.datascope.annotation;

import java.lang.annotation.*;

/**
 * Marks an entity field as a user ID column for data scope filtering.
 *
 * <p>Multiple fields can be annotated on the same entity to support
 * multi-user visibility scenarios (e.g., {@code created_by} and {@code assigned_to}).</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserIdField {
}