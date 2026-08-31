package io.github.springwhale.database.datascope;

import java.lang.annotation.*;

/**
 * Marks an entity field as a tenant ID column for tenant isolation.
 *
 * <p>Multiple fields can be annotated on the same entity to support
 * cross-tenant scenarios (e.g., {@code tenant_id} and {@code target_tenant_id}).</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIdField {
}