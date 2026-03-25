package io.github.springwhale.test.enums;

import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum StatusEnum implements BaseEnum {
    ACTIVE("ACTIVE", "Active"),
    INACTIVE("INACTIVE", "Inactive"),
    PENDING("PENDING", "Pending"),
    DELETED("DELETED", "Deleted");

    @Getter
    private final String id;
    @Getter
    private final String desc;
}
