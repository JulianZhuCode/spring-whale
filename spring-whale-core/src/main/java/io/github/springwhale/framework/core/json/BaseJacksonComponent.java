package io.github.springwhale.framework.core.json;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

/**
 * Base class for Jackson component classes.
 * <p>
 * Provides shared static access to {@link SpringWhaleJsonProperties} and
 * {@link MessageSource} for all inner serializer/deserializer classes.
 * Subclasses are annotated with {@code @JacksonComponent} so Spring Boot
 * auto-registers their inner serializer/deserializer classes.
 * </p>
 */
public abstract class BaseJacksonComponent implements ApplicationContextAware {

    @Getter
    protected static SpringWhaleJsonProperties properties;
    @Getter
    protected static MessageSource messageSource;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        BaseJacksonComponent.properties = context.getBean(SpringWhaleJsonProperties.class);
        BaseJacksonComponent.messageSource = context.getBean(MessageSource.class);
    }

}