package io.github.springwhale.test.json;

import io.github.springwhale.test.json.JsonTestRecords.DoubleRecord;
import io.github.springwhale.test.json.JsonTestRecords.FloatRecord;
import io.github.springwhale.test.json.JsonTestRecords.IntRecord;
import io.github.springwhale.test.json.JsonTestRecords.LongRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DatabindException;

import static org.junit.jupiter.api.Assertions.*;

public class NumberJacksonComponentTest extends BaseJacksonTest {

    @Test
    @DisplayName("Should deserialize Long correctly")
    public void testLongDeserialization() {
        assertEquals(Long.valueOf(1234567890123456789L), mapper.readValue("{\"value\":1234567890123456789}", LongRecord.class).value());
        DatabindException illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":1234567890123456789000000}", LongRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("1234567890123456789000000"));
        illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":-1234567890123456789000000}", LongRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("-1234567890123456789000000"));
    }

    @Test
    @DisplayName("Should deserialize Integer correctly")
    public void testIntegerDeserialization() {
        assertEquals(Integer.valueOf(123), mapper.readValue("{\"value\":123}", IntRecord.class).value());
        DatabindException illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":1234567890123456789000000}", IntRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("1234567890123456789000000"));
        illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":-1234567890123456789000000}", IntRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("-1234567890123456789000000"));
    }

    @Test
    @DisplayName("Should serializer double correctly")
    public void testDoubleSerialization() {
        assertEquals("{\"value\":123.12345679}", mapper.writeValueAsString(new DoubleRecord(123.123456789)));
    }

    @Test
    @DisplayName("Should serializer float correctly")
    public void testFloatSerialization() {
        assertEquals("{\"value\":123.12346}", mapper.writeValueAsString(new FloatRecord(123.123456789f)));
    }

}