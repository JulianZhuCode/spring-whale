package io.github.springwhale.test.json;


import io.github.springwhale.framework.core.json.SpringWhaleJsonConfig;
import io.github.springwhale.test.enums.StatusEnum;
import io.github.springwhale.test.model.Staff;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON serialization and deserialization of enums
 */
@SpringBootTest
public class JsonTest {

    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    private SpringWhaleJsonConfig jsonConfig;
    
    private Locale originalDefaultLocale;
    private Locale originalContextLocale;

    @BeforeEach
    void setUp() {
        // Save original locale settings
        originalDefaultLocale = Locale.getDefault();
        originalContextLocale = LocaleContextHolder.getLocale();
    }

    @AfterEach
    void tearDown() {
        // Restore original locale settings
        Locale.setDefault(originalDefaultLocale);
        LocaleContextHolder.setLocale(originalContextLocale);
        // Reset config to default values
        jsonConfig.setUseI18n(true);
        jsonConfig.setFallbackToDefaultDesc(true);
    }

    /**
     * Test enum serialization without i18n
     */
    @Test
    @DisplayName("Should serialize enum with default description when i18n is disabled")
    public void testEnumSerializationWithoutI18n() {
        jsonConfig.setUseI18n(false);
        Staff staff = new Staff("John Doe", 30, StatusEnum.ACTIVE);
        String json = mapper.writeValueAsString(staff);
        JsonNode node = mapper.readTree(json).get("status");
        
        assertEquals("Active", node.get("desc").asString());
        assertEquals("ACTIVE", node.get("id").asString());
    }

    /**
     * Test that exception is thrown when i18n key is missing and fallback is disabled
     */
    @Test
    @DisplayName("Should throw exception when i18n key is missing and fallback is disabled")
    public void testEnumSerializationWithMissingI18nKeyAndNoFallback() {
        jsonConfig.setFallbackToDefaultDesc(false);
        Staff staff = new Staff("John Doe", 30, StatusEnum.DELETED);
        
        assertThrows(DatabindException.class, () -> mapper.writeValueAsString(staff));
    }

    /**
     * Test enum serialization with Japanese locale (fallback to English)
     */
    @Test
    @DisplayName("Should serialize enum with English description when Japanese locale is set and translation is missing")
    public void testEnumSerializationWithJapaneseLocaleAndMissingTranslation() {
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);
        
        Staff staff = new Staff("John Doe", 30, StatusEnum.DELETED);
        String json = mapper.writeValueAsString(staff);
        JsonNode node = mapper.readTree(json).get("status");
        
        assertEquals("Deleted", node.get("desc").asString());
        assertEquals("DELETED", node.get("id").asString());
    }

    /**
     * Test enum serialization with Japanese locale (has translation)
     */
    @Test
    @DisplayName("Should serialize enum with Japanese description when translation exists")
    public void testEnumSerializationWithJapaneseLocaleAndTranslation() {
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);
        
        Staff staff = new Staff("John Doe", 30, StatusEnum.INACTIVE);
        String json = mapper.writeValueAsString(staff);
        JsonNode node = mapper.readTree(json).get("status");
        
        assertEquals("非アクティブ", node.get("desc").asString());
        assertEquals("INACTIVE", node.get("id").asString());
    }

    /**
     * Test enum deserialization from object format
     */
    @Test
    @DisplayName("Should deserialize enum from object format with id and desc")
    public void testEnumDeserializationFromObjectFormat() {
        Staff expected = new Staff("John Doe", 30, StatusEnum.ACTIVE);
        String json = "{\"name\":\"John Doe\",\"age\":30,\"status\":{\"id\":\"ACTIVE\",\"desc\":\"Active\"}}";
        
        Staff actual = mapper.readValue(json, Staff.class);
        
        assertEquals(expected, actual);
    }

    /**
     * Test enum deserialization from string format
     */
    @Test
    @DisplayName("Should deserialize enum from string format using id")
    public void testEnumDeserializationFromStringFormat() {
        Staff expected = new Staff("John Doe", 30, StatusEnum.ACTIVE);
        String json = "{\"name\":\"John Doe\",\"age\":30,\"status\":\"ACTIVE\"}";
        
        Staff actual = mapper.readValue(json, Staff.class);
        
        assertEquals(expected, actual);
    }

    /**
     * Test enum deserialization from integer format (ordinal)
     */
    @Test
    @DisplayName("Should deserialize enum from integer format using ordinal")
    public void testEnumDeserializationFromIntFormat() {
        Staff expected = new Staff("John Doe", 30, StatusEnum.ACTIVE);
        String json = "{\"name\":\"John Doe\",\"age\":30,\"status\":0}";
        
        Staff actual = mapper.readValue(json, Staff.class);
        
        assertEquals(expected, actual);
    }

    /**
     * Test enum deserialization with invalid integer (negative)
     */
    @Test
    @DisplayName("Should throw exception when deserializing from negative integer")
    public void testEnumDeserializationWithInvalidNegativeInt() {
        String json = "{\"name\":\"John Doe\",\"age\":30,\"status\":-1}";
        
        assertThrows(Exception.class, () -> mapper.readValue(json, Staff.class));
    }

    /**
     * Test enum deserialization with invalid float number
     */
    @Test
    @DisplayName("Should throw exception when deserializing from float number")
    public void testEnumDeserializationWithInvalidFloat() {
        String json = "{\"name\":\"John Doe\",\"age\":30,\"status\":1.0}";
        
        assertThrows(Exception.class, () -> mapper.readValue(json, Staff.class));
    }

    /**
     * Test enum deserialization with invalid id
     */
    @Test
    @DisplayName("Should throw exception when deserializing from invalid id")
    public void testEnumDeserializationWithInvalidId() {
        String json = "{\"name\":\"John Doe\",\"age\":30,\"status\":\"ACTIVE2\"}";
        
        assertThrows(Exception.class, () -> mapper.readValue(json, Staff.class));
    }
}
