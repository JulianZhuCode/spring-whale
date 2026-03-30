package io.github.springwhale.framework.core.json;

import io.github.springwhale.framework.core.enums.BaseEnum;
import io.github.springwhale.framework.core.utils.DateTimeFormats;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.boot.jackson.ObjectValueDeserializer;
import org.springframework.boot.jackson.ObjectValueSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@JacksonComponent
public class SpringWhaleJacksonComponent implements ApplicationContextAware {

    private static SpringWhaleJsonConfig jsonConfig = null;
    private static MessageSource messageSource = null;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        jsonConfig = context.getBean(SpringWhaleJsonConfig.class);
        messageSource = context.getBean(MessageSource.class);
    }

    // ==================== Date Serialization/Deserialization ====================

    @SuppressWarnings("unused")
    public static class DateSerializer extends ValueSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            if ("timestamp".equalsIgnoreCase(jsonConfig.getDateTimeFormat())) {
                gen.writeNumber(value.getTime());
            } else {
                gen.writeString(new SimpleDateFormat(jsonConfig.getDateTimeFormat()).format(value));
            }
        }
    }

    @SuppressWarnings("unused")
    public static class DateDeserializer extends ValueDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();

            if (node.isLong() || node.isInt()) {
                // Deserialize from timestamp (milliseconds)
                return new Date(node.asLong());
            } else if (node.isString()) {
                return DateTimeFormats.parseDateFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize date from: " + node);
        }
    }

    // ==================== LocalDate Serialization/Deserialization ====================

    @SuppressWarnings("unused")
    public static class LocalDateSerializer extends ValueSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(jsonConfig.getDateFormat());
            gen.writeString(value.format(formatter));
        }
    }

    @SuppressWarnings("unused")
    public static class LocalDateDeserializer extends ValueDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();

            if (node.isLong() || node.isInt()) {
                // Deserialize from timestamp (milliseconds)
                long timestamp = node.asLong();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalDate();
            } else if (node.isString()) {
                return DateTimeFormats.parseLocalDateFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize local date from: " + node);
        }
    }

    // ==================== LocalTime Serialization/Deserialization ====================

    @SuppressWarnings("unused")
    public static class LocalTimeSerializer extends ValueSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(jsonConfig.getTimeFormat());
            gen.writeString(value.format(formatter));
        }
    }

    @SuppressWarnings("unused")
    public static class LocalTimeDeserializer extends ValueDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();

            if (node.isLong() || node.isInt()) {
                // Deserialize from timestamp (milliseconds)
                long timestamp = node.asLong();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalTime();
            } else if (node.isString()) {
                return DateTimeFormats.parseLocalTimeFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize local time from: " + node);
        }
    }

    // ==================== LocalDateTime Serialization/Deserialization ====================

    @SuppressWarnings("unused")
    public static class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            if ("timestamp".equalsIgnoreCase(jsonConfig.getDateTimeFormat())) {
                gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(jsonConfig.getDateTimeFormat());
                gen.writeString(value.format(formatter));
            }
        }
    }

    @SuppressWarnings("unused")
    public static class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();

            if (node.isLong() || node.isInt()) {
                // Deserialize from timestamp (milliseconds)
                long timestamp = node.asLong();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            } else if (node.isString()) {
                return DateTimeFormats.parseLocalDateTimeFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize local date time from: " + node);
        }
    }

    // ==================== BaseEnum Serialization/Deserialization ====================

    @SuppressWarnings("unused")
    public static class BaseEnumSerializer extends ObjectValueSerializer<BaseEnum> {
        @Override
        public void serializeObject(BaseEnum value, JsonGenerator gen, @NonNull SerializationContext context) throws JacksonException {
            gen.writeStringProperty("id", value.getId());
            gen.writeStringProperty("desc", resolveDesc(value));
        }

        private String resolveDesc(BaseEnum value) {
            if (jsonConfig.isUseI18n()) {
                try {
                    return messageSource.getMessage(value.getId(), null, LocaleContextHolder.getLocale());
                } catch (NoSuchMessageException e) {
                    if (jsonConfig.isFallbackToDefaultDesc()) {
                        return value.getDesc();
                    }
                    throw e;
                }
            }
            return value.getDesc();
        }
    }

    public static class BaseEnumDeserializer extends ObjectValueDeserializer<BaseEnum> {

        private Class<? extends BaseEnum> enumClass;

        @Override
        @SuppressWarnings("unchecked")
        public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            this.enumClass = (Class<? extends BaseEnum>) context.getContextualType().getRawClass();
            return super.createContextual(context, property);
        }

        @Override
        public BaseEnum deserializeObject(@NonNull JsonParser jsonParser, @NonNull DeserializationContext context, @NonNull JsonNode node) throws JacksonException {
            if (node.isObject()) {
                return findEnumById(enumClass, nullSafeValue(node.get("id"), String.class));
            } else if (node.isString()) {
                return findEnumById(enumClass, node.asString());
            } else if (node.isInt()) {
                return findEnumById(enumClass, node.asInt());
            }
            throw new IllegalArgumentException("Cannot deserialize enum from: " + node);
        }

        private BaseEnum findEnumById(Class<?> enumClass, String id) {
            Object[] enumConstants = enumClass.getEnumConstants();
            for (Object constant : enumConstants) {
                BaseEnum baseEnum = (BaseEnum) constant;
                if (baseEnum.getId().equals(id)) {
                    return baseEnum;
                }
            }
            throw new IllegalArgumentException("No enum constant with id: " + id + " in class: " + enumClass.getName());
        }

        private BaseEnum findEnumById(Class<?> enumClass, int index) {
            Object[] enumConstants = enumClass.getEnumConstants();
            return (BaseEnum) enumConstants[index];
        }
    }

    // ==================== BigDecimal Serialization/Deserialization ====================

    @SuppressWarnings("unused")
    public static class BigDecimalSerializer extends ValueSerializer<BigDecimal> {

        @Override
        public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            if (!jsonConfig.isBigDecimalEnabled()) {
                gen.writeNumber(value);
                return;
            }
            BigDecimal scaledValue = value.setScale(
                    jsonConfig.getBigDecimalScale(),
                    jsonConfig.getBigDecimalRoundingMode()
            );
            // Serialize based on configuration
            if (jsonConfig.isBigDecimalAsString()) {
                gen.writeString(scaledValue.toPlainString());
            } else {
                gen.writeNumber(scaledValue);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class BigDecimalDeserializer extends ValueDeserializer<BigDecimal> {
        @Override
        public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();
            BigDecimal value;
            if (node.isString()) {
                try {
                    value = new BigDecimal(node.asString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Cannot deserialize big decimal from string: '" + node.asString() + "'", e);
                }
            } else if (node.isNumber()) {
                value = node.decimalValue();
            } else {
                throw new IllegalArgumentException("Cannot deserialize big decimal from: " + node);
            }
            return value;
        }
    }

    // ==================== Long/Integer Deserialization ====================

    @SuppressWarnings("unused")
    public static class LongDeserializer extends ValueDeserializer<Long> {

        @Override
        public Long deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
            if (p.getNumberType() == JsonParser.NumberType.BIG_INTEGER) {
                BigInteger bigInt = p.getBigIntegerValue();
                if (bigInt.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ||
                        bigInt.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
                    throw new IllegalArgumentException("Long value overflow: " + bigInt);
                }
            }
            return p.getLongValue();
        }
    }

    @SuppressWarnings("unused")
    public static class IntegerDeserializer extends ValueDeserializer<Integer> {

        @Override
        public Integer deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
            if (p.getNumberType() == JsonParser.NumberType.BIG_INTEGER) {
                BigInteger bigInt = p.getBigIntegerValue();
                if (bigInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0 ||
                        bigInt.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
                    throw new IllegalArgumentException("Integer value overflow: " + bigInt);
                }
            }
            return p.getIntValue();
        }
    }

    // ==================== Double/Float Serializer ====================

    @SuppressWarnings("unused")
    public static class DoubleSerializer extends ValueSerializer<Double> {
        @Override
        public void serialize(Double value, JsonGenerator gen, SerializationContext context) {
            BigDecimal bd = new BigDecimal(value).setScale(jsonConfig.getFloatPrecision(), jsonConfig.getBigDecimalRoundingMode());
            gen.writeNumber(bd.stripTrailingZeros());
        }
    }

    @SuppressWarnings("unused")
    public static class FloatSerializer extends ValueSerializer<Float> {
        @Override
        public void serialize(Float value, JsonGenerator gen, SerializationContext context) {
            BigDecimal bd = new BigDecimal(value.toString()).setScale(jsonConfig.getFloatPrecision(), jsonConfig.getBigDecimalRoundingMode());
            gen.writeNumber(bd.stripTrailingZeros());
        }
    }
}
