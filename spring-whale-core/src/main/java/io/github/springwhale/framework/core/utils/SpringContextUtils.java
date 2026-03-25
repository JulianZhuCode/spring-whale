package io.github.springwhale.framework.core.utils;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring context utility class for obtaining beans in non-Spring managed classes
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        SpringContextUtils.applicationContext = context;
    }

    /**
     * Get bean instance by name
     *
     * @param name Bean name
     * @return Bean instance
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * Get bean instance by type
     *
     * @param clazz Bean type
     * @param <T>   Generic type
     * @return Bean instance
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * Get bean instance by name and type
     *
     * @param name  Bean name
     * @param clazz Bean type
     * @param <T>   Generic type
     * @return Bean instance
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

}
