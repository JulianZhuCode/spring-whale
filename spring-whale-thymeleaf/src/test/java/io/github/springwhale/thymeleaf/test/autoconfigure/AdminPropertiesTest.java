package io.github.springwhale.thymeleaf.test.autoconfigure;

import io.github.springwhale.framework.thymeleaf.autoconfigure.AdminProperties;
import io.github.springwhale.thymeleaf.test.TestSecurityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.whale.thymeleaf.admin.brand-name=Test Brand",
        "spring.whale.thymeleaf.admin.short-name=TB",
        "spring.whale.thymeleaf.admin.copyright=Test Corp",
        "spring.whale.thymeleaf.admin.version=9.9.9"
})
@Import(TestSecurityConfiguration.class)
class AdminPropertiesTest {

    @Autowired
    private AdminProperties adminProperties;

    @Test
    @DisplayName("should bind custom properties")
    void customProperties() {
        assertEquals("Test Brand", adminProperties.getBrandName());
        assertEquals("TB", adminProperties.getShortName());
        assertEquals("Test Corp", adminProperties.getCopyright());
        assertEquals("9.9.9", adminProperties.getVersion());
    }

    @Test
    @DisplayName("getters should return non-null values")
    void notNull() {
        assertNotNull(adminProperties.getBrandName());
        assertNotNull(adminProperties.getShortName());
        assertNotNull(adminProperties.getCopyright());
        assertNotNull(adminProperties.getVersion());
    }
}

@SpringBootTest
@Import(TestSecurityConfiguration.class)
class AdminPropertiesDefaultTest {

    @Autowired
    private AdminProperties adminProperties;

    @Test
    @DisplayName("should use default values when not configured")
    void defaultValues() {
        assertEquals("Spring Whale", adminProperties.getBrandName());
        assertEquals("SW Admin", adminProperties.getShortName());
        assertEquals("Spring Whale Framework", adminProperties.getCopyright());
        assertEquals("0.0.2", adminProperties.getVersion());
    }
}