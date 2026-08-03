package io.github.springwhale.framework.core.json;

import io.github.springwhale.framework.core.utils.DateTimeFormats;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@JacksonComponent
public class DateTimeJacksonComponent extends BaseJacksonComponent {

    @SuppressWarnings("unused")
    public static class DateSerializer extends ValueSerializer<Date> {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            if ("timestamp".equalsIgnoreCase(properties.getDateTimeFormat())) {
                gen.writeNumber(value.getTime());
            } else {
                gen.writeString(new SimpleDateFormat(properties.getDateTimeFormat()).format(value));
            }
        }
    }

    @SuppressWarnings("unused")
    public static class DateDeserializer extends ValueDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();

            if (node.isLong() || node.isInt()) {
                return new Date(node.asLong());
            } else if (node.isString()) {
                return DateTimeFormats.parseDateFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize date from: " + node);
        }
    }

    @SuppressWarnings("unused")
    public static class LocalDateSerializer extends ValueSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(properties.getDateFormat());
            gen.writeString(value.format(formatter));
        }
    }

    @SuppressWarnings("unused")
    public static class LocalDateDeserializer extends ValueDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();

            if (node.isLong() || node.isInt()) {
                long timestamp = node.asLong();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalDate();
            } else if (node.isString()) {
                return DateTimeFormats.parseLocalDateFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize local date from: " + node);
        }
    }

    @SuppressWarnings("unused")
    public static class LocalTimeSerializer extends ValueSerializer<LocalTime> {
        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(properties.getTimeFormat());
            gen.writeString(value.format(formatter));
        }
    }

    @SuppressWarnings("unused")
    public static class LocalTimeDeserializer extends ValueDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();

            if (node.isLong() || node.isInt()) {
                long timestamp = node.asLong();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalTime();
            } else if (node.isString()) {
                return DateTimeFormats.parseLocalTimeFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize local time from: " + node);
        }
    }

    @SuppressWarnings("unused")
    public static class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            if ("timestamp".equalsIgnoreCase(properties.getDateTimeFormat())) {
                gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(properties.getDateTimeFormat());
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
                long timestamp = node.asLong();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            } else if (node.isString()) {
                return DateTimeFormats.parseLocalDateTimeFromText(node.asString());
            }

            throw new IllegalArgumentException("Cannot deserialize local date time from: " + node);
        }
    }
}