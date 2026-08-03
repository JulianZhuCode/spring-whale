package io.github.springwhale.framework.core.json;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;

import java.math.BigDecimal;

@JacksonComponent
public class BigDecimalJacksonComponent extends BaseJacksonComponent {

    @SuppressWarnings("unused")
    public static class BigDecimalSerializer extends ValueSerializer<BigDecimal> {

        @Override
        public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext context) throws JacksonException {
            if (!properties.isBigDecimalEnabled()) {
                gen.writeNumber(value);
                return;
            }
            BigDecimal scaledValue = value.setScale(
                    properties.getBigDecimalScale(),
                    properties.getBigDecimalRoundingMode()
            );
            if (properties.isBigDecimalAsString()) {
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
}