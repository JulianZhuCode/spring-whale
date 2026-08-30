package io.github.springwhale.database.datascope;

import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DataScopeType implements BaseEnum {

    CUSTOM("CUSTOM", "Custom Dept Scope"),

    DEPT("DEPT", "Own Dept"),

    DEPT_AND_CHILD("DEPT_AND_CHILD", "Own Dept and Children"),

    SELF("SELF", "Own Data Only"),

    CALLER("CALLER", "Caller-Defined Scope"),

    AUTO("AUTO", "Auto Inferred");

    private final String id;
    private final String desc;
}