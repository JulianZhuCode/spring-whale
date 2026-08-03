package io.github.springwhale.test.json;

import io.github.springwhale.test.json.JsonTestRecords.BigDecimalRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalJacksonComponentTest extends BaseJacksonTest {

    @Test
    @DisplayName("Should serialize BigDecimal correctly")
    public void testBigDecimalSerialization() {
        BigDecimalRecord record = new BigDecimalRecord(BigDecimal.valueOf(123.456));
        assertEquals("{\"decimal\":\"123.46\"}", mapper.writeValueAsString(record));
        jsonProperties.setBigDecimalAsString(false);
        assertEquals("{\"decimal\":123.46}", mapper.writeValueAsString(record));
        jsonProperties.setBigDecimalEnabled(false);
        assertEquals("{\"decimal\":123.456}", mapper.writeValueAsString(record));
    }

    @Test
    @DisplayName("Should deserialize BigDecimal correctly")
    public void testBigDecimalDeserialization() {
        assertEquals(new BigDecimal("123.123"), mapper.readValue("{\"decimal\":123.123}", BigDecimalRecord.class).decimal());
        assertEquals(new BigDecimal("123.123"), mapper.readValue("{\"decimal\":\"123.123\"}", BigDecimalRecord.class).decimal());
    }

}