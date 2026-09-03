package io.github.springwhale.database.datascope.annotation;

import io.github.springwhale.database.datascope.DataScopeHelper;

import java.lang.annotation.*;

/**
 * Marks an entity class as representing a tenant itself, where the specified
 * fields serve as the tenant ID for tenant isolation filtering.
 *
 * <p>Use this annotation when the entity {@code id} (or other fields) is the
 * tenant identifier — e.g. a tenant entity whose primary key is the tenant ID.
 * This avoids the need to redeclare {@code @Id} / {@code @GeneratedValue}
 * just to add {@code @TenantIdField} on a field already defined in a superclass.</p>
 *
 * <p>Works alongside {@link TenantIdField} — both can be used on the same entity.
 * The resolved tenant fields are <b>unioned</b> (OR semantics).</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @TenantIdScope // defaults to {"id"}
 * public class TenantEntity extends BaseEntity { ... }
 *
 * @TenantIdScope({"tenantId", "ownerTenantId"})  // custom field names
 * public class SomeEntity extends BaseEntity { ... }
 * }</pre>
 *
 * @see TenantIdField
 * @see DataScopeHelper
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIdScope {

    /**
     * Field names that serve as tenant IDs.
     * Defaults to {@code {"id"}} for the common case where the entity's
     * primary key is the tenant identifier.
     */
    String[] value() default {"id"};
}