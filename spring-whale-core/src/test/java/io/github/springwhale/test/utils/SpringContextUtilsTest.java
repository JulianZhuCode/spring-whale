package io.github.springwhale.test.utils;

import io.github.springwhale.framework.core.json.SpringWhaleJsonConfig;
import io.github.springwhale.framework.core.utils.SpringContextUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpringContextUtils
 */
@SpringBootTest
class SpringContextUtilsTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should return non-null ApplicationContext")
    void testGetApplicationContext() {
        ApplicationContext context = SpringContextUtils.getApplicationContext();
        assertNotNull(context, "ApplicationContext should not be null");
    }

    @Test
    @DisplayName("Should get bean by fully qualified class name")
    void testGetBeanByClassName() {
        String beanName = SpringWhaleJsonConfig.class.getName();
        Object bean = SpringContextUtils.getBean(beanName);
        
        assertNotNull(bean, "Bean should be found by class name");
        assertInstanceOf(SpringWhaleJsonConfig.class, bean, "Bean should be instance of SpringWhaleJsonConfig");
    }

    @Test
    @DisplayName("Should get bean by simple bean name")
    void testGetBeanBySimpleBeanName() {
        Object bean = SpringContextUtils.getBean(SpringWhaleJsonConfig.class.getName());
        
        assertNotNull(bean, "Bean should be found by simple name");
        assertInstanceOf(SpringWhaleJsonConfig.class, bean, "Bean should be instance of SpringWhaleJsonConfig");
    }

    @Test
    @DisplayName("Should get bean by type")
    void testGetBeanByType() {
        SpringWhaleJsonConfig config = SpringContextUtils.getBean(SpringWhaleJsonConfig.class);
        
        assertNotNull(config, "Bean should be found by type");
        assertTrue(config.isUseI18n(), "Default useI18n should be true");
        assertTrue(config.isFallbackToDefaultDesc(), "Default fallbackToDefaultDesc should be false");
    }

    @Test
    @DisplayName("Should get bean by name and type")
    void testGetBeanByNameAndType() {
        SpringWhaleJsonConfig config = SpringContextUtils.getBean(SpringWhaleJsonConfig.class.getName(), SpringWhaleJsonConfig.class);
        
        assertNotNull(config, "Bean should be found by name and type");
        assertInstanceOf(SpringWhaleJsonConfig.class, config, "Bean should be instance of SpringWhaleJsonConfig");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent bean by name")
    void testGetNonExistentBeanByName() {
        assertThrows(Exception.class, () -> SpringContextUtils.getBean("nonExistentBean"), "Should throw exception for non-existent bean");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent bean by type")
    void testGetNonExistentBeanByType() {
        assertThrows(Exception.class, () -> SpringContextUtils.getBean(String.class), "Should throw exception for non-existent bean type");
    }

    @Test
    @DisplayName("Should have same ApplicationContext instance as autowired one")
    void testSetApplicationContext() {
        ApplicationContext context = SpringContextUtils.getApplicationContext();
        
        assertNotNull(context, "ApplicationContext should be set by Spring");
        assertSame(applicationContext, context, "ApplicationContext should be the same instance as autowired one");
    }

    @Test
    @DisplayName("Should get ObjectMapper bean")
    void testGetObjectMapperBean() {
        tools.jackson.databind.ObjectMapper mapper = SpringContextUtils.getBean(tools.jackson.databind.ObjectMapper.class);
        assertNotNull(mapper, "ObjectMapper bean should exist");
    }
}
