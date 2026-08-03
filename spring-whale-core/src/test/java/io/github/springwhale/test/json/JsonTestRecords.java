package io.github.springwhale.test.json;

import io.github.springwhale.framework.core.enums.BaseEnum;
import io.github.springwhale.framework.core.json.serializer.I18nSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public final class JsonTestRecords {

    private JsonTestRecords() {
    }

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

    public record EnumTestRecord(StatusEnum status) {
    }

    public record TimeRecord(Date date, LocalDateTime localDateTime, LocalDate localDate, LocalTime localTime) {
    }

    public record BigDecimalRecord(BigDecimal decimal) {
    }

    public record LongRecord(Long value) {
    }

    public record IntRecord(Integer value) {
    }

    public record DoubleRecord(Double value) {
    }

    public record FloatRecord(Float value) {
    }

    public record I18nTestRecord(
            @JsonSerialize(using = I18nSerializer.class)
            String label,

            @JsonSerialize(using = I18nSerializer.class)
            String nullableLabel
    ) {
    }

}