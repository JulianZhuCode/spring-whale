package io.github.springwhale.test.json;

import io.github.springwhale.test.json.JsonTestRecords.I18nTestRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class I18nSerializerTest extends BaseJacksonTest {

    @Test
    @DisplayName("Should output null when value is null")
    public void testNullValue() {
        I18nTestRecord record = new I18nTestRecord("test.i18n.hello", null);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("nullableLabel");

        assertTrue(node.isNull());
    }

    @Test
    @DisplayName("Should output original value when i18n is disabled")
    public void testI18nDisabled() {
        jsonProperties.setUseI18n(false);
        I18nTestRecord record = new I18nTestRecord("test.i18n.hello", null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("label");

        assertEquals("test.i18n.hello", node.asString());
    }

    @Test
    @DisplayName("Should output translated value when i18n is enabled and key exists")
    public void testI18nWithTranslation() {
        I18nTestRecord record = new I18nTestRecord("test.i18n.hello", null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("label");

        assertEquals("Hello", node.asString());
    }

    @Test
    @DisplayName("Should output Japanese translation when locale is set to Japanese")
    public void testI18nWithJapaneseTranslation() {
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);
        I18nTestRecord record = new I18nTestRecord("test.i18n.hello", null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("label");

        assertEquals("こんにちは", node.asString());
    }

    @Test
    @DisplayName("Should output original value when key not found and fallback is enabled")
    public void testI18nKeyNotFoundWithFallback() {
        jsonProperties.setFallbackToDefaultDesc(true);
        I18nTestRecord record = new I18nTestRecord("test.i18n.unknown", null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("label");

        assertEquals("test.i18n.unknown", node.asString());
    }

    @Test
    @DisplayName("Should throw exception when key not found and fallback is disabled")
    public void testI18nKeyNotFoundWithoutFallback() {
        jsonProperties.setFallbackToDefaultDesc(false);
        I18nTestRecord record = new I18nTestRecord("test.i18n.unknown", null);

        assertThrows(DatabindException.class, () -> mapper.writeValueAsString(record));
    }

}