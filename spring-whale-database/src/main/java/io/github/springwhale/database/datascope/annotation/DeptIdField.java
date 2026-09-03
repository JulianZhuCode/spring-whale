package io.github.springwhale.database.datascope.annotation;

import java.lang.annotation.*;

/**
 * Marks an entity field as a department ID column for data scope filtering.
 *
 * <p>Multiple fields can be annotated on the same entity to support
 * multi-department visibility scenarios.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeptIdField {
}