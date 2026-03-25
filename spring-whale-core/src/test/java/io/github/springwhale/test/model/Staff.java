package io.github.springwhale.test.model;

import io.github.springwhale.test.enums.StatusEnum;

public record Staff(String name, Integer age, StatusEnum status) {
}
