package io.github.springwhale.database.datascope;

import java.lang.annotation.*;

/**
 * Marks an entity class as representing a department itself, where the specified
 * fields serve as the department ID for data scope filtering.
 *
 * <p>Use this annotation when the entity {@code id} (or other fields) is the
 * department identifier — e.g. {@code GroupEntity} whose primary key is the
 * department ID. This avoids the need to redeclare {@code @Id} / {@code @GeneratedValue}
 * just to add {@code @DeptIdField} on a field already defined in a superclass.</p>
 *
 * <p>Works alongside {@link DeptIdField} — both can be used on the same entity.
 * The resolved department fields are <b>unioned</b> (OR semantics).</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @DeptIdScope   // defaults to {"id"}
 * public class GroupEntity extends BaseEntity { ... }
 *
 * @DeptIdScope({"groupId", "deptId"})  // custom field names
 * public class SomeEntity extends BaseEntity { ... }
 * }</pre>
 *
 * @see DeptIdField
 * @see DataScopeHelper
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeptIdScope {

    /**
     * Field names that serve as department IDs.
     * Defaults to {@code {"id"}} for the common case where the entity's
     * primary key is the department identifier.
     */
    String[] value() default {"id"};
}