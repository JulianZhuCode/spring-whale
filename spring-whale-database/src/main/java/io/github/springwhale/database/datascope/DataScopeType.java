package io.github.springwhale.database.datascope;

import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data scope visibility levels.
 *
 * <table>
 *   <tr><th>Type</th><th>Visibility</th></tr>
 *   <tr><td>{@code SELF}</td><td>User's own data only</td></tr>
 *   <tr><td>{@code DEPT}</td><td>User's department</td></tr>
 *   <tr><td>{@code DEPT_AND_CHILD}</td><td>User's department and all child departments</td></tr>
 *   <tr><td>{@code CUSTOM}</td><td>Custom scope defined by {@link DataScopeHandler}</td></tr>
 *   <tr><td>{@code CALLER}</td><td>Delegates to the caller's scope (cross-service)</td></tr>
 *   <tr><td>{@code AUTO}</td><td>Inferred from the user's context</td></tr>
 * </table>
 */
@AllArgsConstructor
@Getter
public enum DataScopeType implements BaseEnum {

    CUSTOM("datascope.type.custom", "Custom Dept Scope"),

    DEPT("datascope.type.dept", "Own Dept"),

    DEPT_AND_CHILD("datascope.type.dept_and_child", "Own Dept and Children"),

    SELF("datascope.type.self", "Own Data Only"),

    CALLER("datascope.type.caller", "Caller-Defined Scope"),

    AUTO("datascope.type.auto", "Auto Inferred");

    private final String id;
    private final String desc;
}