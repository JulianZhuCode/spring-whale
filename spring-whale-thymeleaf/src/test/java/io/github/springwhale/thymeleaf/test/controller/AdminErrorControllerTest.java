package io.github.springwhale.thymeleaf.test.controller;

import io.github.springwhale.thymeleaf.test.TestSecurityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
class AdminErrorControllerTest {

    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @CsvSource({
            "403, http.error.403",
            "404, http.error.404",
            "500, http.error.500",
    })
    @DisplayName("should return error page for common status codes")
    void errorPage(int statusCode, String expectedMessageKey) throws Exception {
        mvc.perform(get("/error")
                        .requestAttr("jakarta.servlet.error.status_code", statusCode))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("status", statusCode))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeExists("adminProps"));
    }

    @Test
    @DisplayName("should default to 500 when status code is null")
    void defaultTo500() throws Exception {
        mvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("status", 500))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    @DisplayName("should handle 401 status code")
    void unauthorized() throws Exception {
        mvc.perform(get("/error")
                        .requestAttr("jakarta.servlet.error.status_code", 401))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("status", 401));
    }

    @Test
    @DisplayName("should render i18n error message")
    void i18nMessage() throws Exception {
        mvc.perform(get("/error")
                        .requestAttr("jakarta.servlet.error.status_code", 404))
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", not(emptyString())));
    }
}