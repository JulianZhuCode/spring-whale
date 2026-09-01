package io.github.springwhale.framework.core.json.serializer;

import io.github.springwhale.framework.core.json.BaseJacksonComponent;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Locale;

public class I18nSerializer extends ValueSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (!BaseJacksonComponent.getProperties().isUseI18n()) {
            gen.writeString(value);
            return;
        }
        Locale locale = LocaleContextHolder.getLocale();
        try {
            String translateText = BaseJacksonComponent.getMessageSource().getMessage(value, null, locale);
            gen.writeString(translateText);
        } catch (NoSuchMessageException e) {
            if (BaseJacksonComponent.getProperties().isFallbackToDefaultDesc()) {
                gen.writeString(value);
            } else {
                throw e;
            }
        }
    }

}