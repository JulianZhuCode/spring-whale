package io.github.springwhale.framework.core.json;

import io.github.springwhale.framework.core.enums.BaseEnum;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;

@JacksonComponent
public class SpringWhaleJacksonComponent implements ApplicationContextAware {

    private static SpringWhaleJsonConfig jsonConfig = null;
    private static MessageSource messageSource = null;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        jsonConfig = context.getBean(SpringWhaleJsonConfig.class);
        messageSource = context.getBean(MessageSource.class);
    }

    public static class BaseEnumSerializer extends ValueSerializer<BaseEnum> {
        @Override
        public void serialize(BaseEnum value, JsonGenerator jsonGenerator, SerializationContext context) throws JacksonException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringProperty("id", value.getId());
            jsonGenerator.writeStringProperty("desc", resolveDesc(value));
            jsonGenerator.writeEndObject();
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

    public static class BaseEnumDeserializer extends ValueDeserializer<BaseEnum> {

        private Class<? extends BaseEnum> enumClass;

        @Override
        public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            this.enumClass = (Class<? extends BaseEnum>) context.getContextualType().getRawClass();
            return super.createContextual(context, property);
        }

        @Override
        public BaseEnum deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
            JsonNode node = jsonParser.readValueAsTree();
            if (node.isObject()) {
                return findEnumById(enumClass, node.get("id").asString());
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
