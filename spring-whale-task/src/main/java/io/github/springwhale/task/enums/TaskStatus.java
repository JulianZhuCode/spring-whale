package io.github.springwhale.task.enums;

import lombok.Getter;

/**
 * Batch task status enum.
 */
@Getter
public enum TaskStatus {
    PENDING("PENDING", "待执行"),
    RUNNING("RUNNING", "执行中"),
    PAUSED("PAUSED", "已暂停"),
    COMPLETED("COMPLETED", "已完成"),
    FAILED("FAILED", "执行失败"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String label;

    TaskStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
