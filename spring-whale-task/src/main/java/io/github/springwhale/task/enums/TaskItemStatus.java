package io.github.springwhale.task.enums;

import lombok.Getter;

/**
 * Task item status enum.
 */
@Getter
public enum TaskItemStatus {
    PENDING("PENDING", "待执行"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    SKIPPED("SKIPPED", "已跳过");

    private final String code;
    private final String label;

    TaskItemStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
