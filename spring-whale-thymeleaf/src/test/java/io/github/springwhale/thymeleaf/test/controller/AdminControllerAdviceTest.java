package io.github.springwhale.thymeleaf.test.controller;

import io.github.springwhale.framework.thymeleaf.controller.AdminPage;
import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import io.github.springwhale.thymeleaf.test.TestSecurityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestSecurityConfiguration.class, AdminControllerAdviceTest.TestMenuConfig.class,
        AdminControllerAdviceTest.TestAdviceController.class})
class AdminControllerAdviceTest {

    @TestConfiguration
    static class TestMenuConfig {
        @Bean
        public AdminMenuProvider testMenuProvider() {
            return () -> List.of(
                    MenuItem.group("test", "Test Module", "gear", 1),
                    MenuItem.leaf("test-public", "test", "Public Page",
                            "/test/public", 1),
                    MenuItem.leaf("test-admin", "test", "Admin Only",
                            "/test/admin", "shield", "test:admin", 2));
        }
    }

    @Controller
    @AdminPage
    @RequestMapping("/test-advice")
    static class TestAdviceController {
        @GetMapping
        public String index() {
            return "admin/dashboard";
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("should inject menuGroups, currentPath, userAuthorities, adminProps")
    void injectAllAttributes() throws Exception {
        mvc.perform(get("/test-advice").with(user("admin").authorities(
                () -> "test:admin")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("menuGroups"))
                .andExpect(model().attributeExists("currentPath"))
                .andExpect(model().attributeExists("userAuthorities"))
                .andExpect(model().attributeExists("adminProps"))
                .andExpect(model().attribute("currentPath", "/test-advice"));
    }

    @Test
    @DisplayName("should include public menus for unauthenticated user")
    void unauthenticatedMenuFilter() throws Exception {
        mvc.perform(get("/test-advice"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userAuthorities", empty()));
    }

    @Test
    @DisplayName("should filter menus by authority")
    void adminMenuFilter() throws Exception {
        mvc.perform(get("/test-advice").with(user("admin").authorities(
                () -> "test:admin")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userAuthorities", hasItem("test:admin")));
    }

    @Test
    @DisplayName("wildcard authority should see all menus")
    void wildcardAuthority() throws Exception {
        mvc.perform(get("/test-advice").with(user("super").authorities(
                () -> "*")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userAuthorities", hasItem("*")));
    }

    @Test
    @DisplayName("should not inject attributes on non-@AdminPage controllers")
    void nonAdminPageController() throws Exception {
        mvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("menuGroups"))
                .andExpect(model().attributeDoesNotExist("currentPath"));
    }
}