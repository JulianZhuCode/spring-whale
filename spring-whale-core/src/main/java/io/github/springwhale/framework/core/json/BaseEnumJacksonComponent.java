package io.github.springwhale.framework.core.json;

import io.github.springwhale.framework.core.enums.BaseEnum;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.boot.jackson.ObjectValueDeserializer;
import org.springframework.boot.jackson.ObjectValueSerializer;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;

@JacksonComponent
public class BaseEnumJacksonComponent extends BaseJacksonComponent {

    @SuppressWarnings("unused")
    public static class BaseEnumSerializer extends ObjectValueSerializer<BaseEnum> {
        @Override
        public void serializeObject(BaseEnum value, JsonGenerator gen, @NonNull SerializationContext context) throws JacksonException {
            gen.writeStringProperty("id", value.getId());
            gen.writeStringProperty("desc", resolveDesc(value));
        }

        private String resolveDesc(BaseEnum value) {
            if (properties.isUseI18n()) {
                try {
                    return messageSource.getMessage(value.getId(), null, LocaleContextHolder.getLocale());
                } catch (NoSuchMessageException e) {
                    if (properties.isFallbackToDefaultDesc()) {
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
}