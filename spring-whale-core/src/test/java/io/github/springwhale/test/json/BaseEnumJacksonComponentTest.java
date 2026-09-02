package io.github.springwhale.test.json;

import io.github.springwhale.test.json.JsonTestRecords.EnumTestRecord;
import io.github.springwhale.test.json.JsonTestRecords.StatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseEnumJacksonComponentTest extends BaseJacksonTest {

    @Test
    @DisplayName("Should serialize enum with default description when i18n is disabled")
    public void testEnumSerializationWithoutI18n() {
        jsonProperties.setUseI18n(false);
        EnumTestRecord record = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("Active", node.get("desc").asString());
        assertEquals("ACTIVE", node.get("id").asString());
    }

    @Test
    @DisplayName("Should throw exception when i18n key is missing and fallback is disabled")
    public void testEnumSerializationWithMissingI18nKeyAndNoFallback() {
        jsonProperties.setFallbackToDefaultDesc(false);
        EnumTestRecord record = new EnumTestRecord(StatusEnum.DELETED);

        assertThrows(DatabindException.class, () -> mapper.writeValueAsString(record));
    }

    @Test
    @DisplayName("Should serialize enum with English description when Japanese locale is set and translation is missing")
    public void testEnumSerializationWithJapaneseLocaleAndMissingTranslation() {
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);

        EnumTestRecord record = new EnumTestRecord(StatusEnum.DELETED);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("Deleted", node.get("desc").asString());
        assertEquals("DELETED", node.get("id").asString());
    }

    @Test
    @DisplayName("Should serialize enum with Japanese description when translation exists")
    public void testEnumSerializationWithJapaneseLocaleAndTranslation() {
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);

        EnumTestRecord record = new EnumTestRecord(StatusEnum.INACTIVE);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("非アクティブ", node.get("desc").asString());
        assertEquals("INACTIVE", node.get("id").asString());
    }

    @Test
    @DisplayName("Should deserialize enum from object format with id and desc")
    public void testEnumDeserializationFromObjectFormat() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = "{\"status\":{\"id\":\"ACTIVE\",\"desc\":\"Active\"}}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should deserialize enum from string format using id")
    public void testEnumDeserializationFromStringFormat() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = "{\"status\":\"ACTIVE\"}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should deserialize enum from integer format using ordinal")
    public void testEnumDeserializationFromIntFormat() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = "{\"status\":0}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should throw exception when deserializing from negative integer")
    public void testEnumDeserializationWithInvalidNegativeInt() {
        String json = "{\"status\":-1}";

        assertThrows(Exception.class, () -> mapper.readValue(json, EnumTestRecord.class));
    }

    @Test
    @DisplayName("Should throw exception when deserializing from float number")
    public void testEnumDeserializationWithInvalidFloat() {
        String json = "{\"status\":1.0}";

        assertThrows(Exception.class, () -> mapper.readValue(json, EnumTestRecord.class));
    }

    @Test
    @DisplayName("Should throw exception when deserializing from invalid id")
    public void testEnumDeserializationWithInvalidId() {
        String json = "{\"status\":\"ACTIVE2\"}";

        assertThrows(Exception.class, () -> mapper.readValue(json, EnumTestRecord.class));
    }

    @Test
    @DisplayName("Should serialize enum with name field")
    public void testEnumSerializationIncludesNameField() {
        jsonProperties.setUseI18n(false);
        EnumTestRecord record = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("ACTIVE", node.get("id").asString());
        assertEquals("ACTIVE", node.get("name").asString());
        assertEquals("Active", node.get("desc").asString());
    }

    @Test
    @DisplayName("Should serialize enum with i18n and still include name field")
    public void testEnumSerializationWithI18nIncludesNameField() {
        jsonProperties.setUseI18n(true);
        jsonProperties.setFallbackToDefaultDesc(true);
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);

        EnumTestRecord record = new EnumTestRecord(StatusEnum.INACTIVE);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("INACTIVE", node.get("id").asString());
        assertEquals("INACTIVE", node.get("name").asString());
        assertEquals("非アクティブ", node.get("desc").asString());
    }

    @Test
    @DisplayName("Should deserialize enum from object format using name field")
    public void testEnumDeserializationFromObjectUsingName() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = "{\"status\":{\"name\":\"ACTIVE\"}}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should deserialize enum from object format using name field when id is missing")
    public void testEnumDeserializationFromObjectUsingNameOnly() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.PENDING);
        String json = "{\"status\":{\"name\":\"PENDING\",\"desc\":\"Pending\"}}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should deserialize enum from string format using enum name")
    public void testEnumDeserializationFromStringUsingName() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.DELETED);
        String json = "{\"status\":\"DELETED\"}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should throw exception when deserializing from object with invalid name")
    public void testEnumDeserializationWithInvalidName() {
        String json = "{\"status\":{\"name\":\"INVALID_NAME\"}}";

        assertThrows(Exception.class, () -> mapper.readValue(json, EnumTestRecord.class));
    }

}