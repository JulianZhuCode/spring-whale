package io.github.springwhale.thymeleaf.test.security;

import io.github.springwhale.thymeleaf.test.TestSecurityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
class ThymeleafSecurityConfigProviderTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("/admin/login should be publicly accessible")
    void loginPageAccessible() throws Exception {
        mvc.perform(get("/admin/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/admin/css/admin.css should be accessible")
    void staticCssAccessible() throws Exception {
        mvc.perform(get("/admin/css/admin.css"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/admin/js/admin.js should be accessible")
    void staticJsAccessible() throws Exception {
        mvc.perform(get("/admin/js/admin.js"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/admin/favicon.svg should be accessible")
    void faviconSvgAccessible() throws Exception {
        mvc.perform(get("/admin/favicon.svg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/webjars/** should be accessible")
    void webjarsAccessible() throws Exception {
        mvc.perform(get("/webjars/bootstrap/css/bootstrap.min.css"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/error should be accessible")
    void errorPageAccessible() throws Exception {
        mvc.perform(get("/error"))
                .andExpect(status().isOk());
    }
}