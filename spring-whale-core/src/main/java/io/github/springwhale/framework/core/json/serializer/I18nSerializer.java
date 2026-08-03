package io.github.springwhale.framework.core.json.serializer;

import io.github.springwhale.framework.core.json.SpringWhaleJsonProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Locale;

@Component
public class I18nSerializer extends ValueSerializer<String> implements ApplicationContextAware {

    private static MessageSource messageSource;
    private static SpringWhaleJsonProperties properties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        messageSource = applicationContext.getBean(MessageSource.class);
        properties = applicationContext.getBean(SpringWhaleJsonProperties.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (!properties.isUseI18n()) {
            gen.writeString(value);
            return;
        }
        Locale locale = LocaleContextHolder.getLocale();
        try {
            String translateText = messageSource.getMessage(value, null, locale);
            gen.writeString(translateText);
        } catch (NoSuchMessageException e) {
            if (properties.isFallbackToDefaultDesc()) {
                gen.writeString(value);
            } else {
                throw e;
            }
        }

    }

}
