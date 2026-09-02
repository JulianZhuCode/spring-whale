package io.github.springwhale.database.datascope;

import java.lang.annotation.*;

/**
 * Marks an entity class as representing a user itself, where the specified
 * fields serve as the user ID for data scope filtering.
 *
 * <p>Use this annotation when the entity {@code id} (or other fields) is the
 * user identifier — e.g. a user profile entity whose primary key is the user ID.
 * This avoids the need to redeclare {@code @Id} / {@code @GeneratedValue}
 * just to add {@code @UserIdField} on a field already defined in a superclass.</p>
 *
 * <p>Works alongside {@link UserIdField} — both can be used on the same entity.
 * The resolved user fields are <b>unioned</b> (OR semantics).</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @UserIdScope   // defaults to {"id"}
 * public class UserEntity extends BaseEntity { ... }
 *
 * @UserIdScope({"userId", "ownerId"})  // custom field names
 * public class SomeEntity extends BaseEntity { ... }
 * }</pre>
 *
 * @see UserIdField
 * @see DataScopeHelper
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserIdScope {

    /**
     * Field names that serve as user IDs.
     * Defaults to {@code {"id"}} for the common case where the entity's
     * primary key is the user identifier.
     */
    String[] value() default {"id"};
}