package io.github.springwhale.framework.core.json;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@JacksonComponent
public class NumberJacksonComponent extends BaseJacksonComponent {

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

    @SuppressWarnings("unused")
    public static class DoubleSerializer extends ValueSerializer<Double> {
        @Override
        public void serialize(Double value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            BigDecimal bd = new BigDecimal(value).setScale(properties.getFloatPrecision(), properties.getBigDecimalRoundingMode());
            gen.writeNumber(bd.stripTrailingZeros());
        }
    }

    @SuppressWarnings("unused")
    public static class FloatSerializer extends ValueSerializer<Float> {
        @Override
        public void serialize(Float value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            BigDecimal bd = new BigDecimal(value.toString()).setScale(properties.getFloatPrecision(), properties.getBigDecimalRoundingMode());
            gen.writeNumber(bd.stripTrailingZeros());
        }
    }
}