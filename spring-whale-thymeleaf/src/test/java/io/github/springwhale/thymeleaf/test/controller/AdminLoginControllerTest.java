package io.github.springwhale.thymeleaf.test.controller;

import io.github.springwhale.thymeleaf.test.TestSecurityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
class AdminLoginControllerTest {

    @Autowired
    private MockMvc mvc;

    @Nested
    @DisplayName("GET /admin/login")
    class LoginPage {

        @Test
        @DisplayName("should return login page with adminProps")
        void loginPage() throws Exception {
            mvc.perform(get("/admin/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/login"))
                    .andExpect(model().attributeExists("adminProps"))
                    .andExpect(model().attributeDoesNotExist("error", "reason"));
        }

        @Test
        @DisplayName("should expose error parameter to model")
        void loginPageWithError() throws Exception {
            mvc.perform(get("/admin/login").param("error", "true"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("error", "Invalid username or password"));
        }

        @Test
        @DisplayName("should expose reason=no_token to model")
        void loginPageWithNoToken() throws Exception {
            mvc.perform(get("/admin/login").param("reason", "no_token"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("reason", "no_token"));
        }

        @Test
        @DisplayName("should expose reason=token_invalid to model")
        void loginPageWithTokenInvalid() throws Exception {
            mvc.perform(get("/admin/login").param("reason", "token_invalid"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("reason", "token_invalid"));
        }

        @Test
        @DisplayName("should expose reason=auth_required to model")
        void loginPageWithAuthRequired() throws Exception {
            mvc.perform(get("/admin/login").param("reason", "auth_required"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("reason", "auth_required"));
        }
    }

    @Nested
    @DisplayName("POST /admin/login")
    class ProcessLogin {

        @Test
        @DisplayName("should set JWT cookie and redirect to /admin")
        void processLogin() throws Exception {
            mvc.perform(post("/admin/login")
                            .param("token", "eyJhbGciOiJIUzI1NiJ9.test-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin"))
                    .andExpect(cookie().exists("sw_token"))
                    .andExpect(cookie().httpOnly("sw_token", true))
                    .andExpect(cookie().path("sw_token", "/"));
        }

        @Test
        @DisplayName("should redirect to safe relative path")
        void processLoginWithRedirect() throws Exception {
            mvc.perform(post("/admin/login")
                            .param("token", "test-token")
                            .param("redirect", "/admin/rbac/users"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/rbac/users"));
        }

        @Test
        @DisplayName("should reject protocol-relative redirect (//evil.com)")
        void rejectProtocolRelativeRedirect() throws Exception {
            mvc.perform(post("/admin/login")
                            .param("token", "test-token")
                            .param("redirect", "//evil.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin"));
        }

        @Test
        @DisplayName("should reject absolute URL redirect")
        void rejectAbsoluteRedirect() throws Exception {
            mvc.perform(post("/admin/login")
                            .param("token", "test-token")
                            .param("redirect", "https://evil.com/phishing"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin"));
        }

        @Test
        @DisplayName("should reject redirect to /admin/login to prevent loop")
        void rejectLoginRedirectLoop() throws Exception {
            mvc.perform(post("/admin/login")
                            .param("token", "test-token")
                            .param("redirect", "/admin/login"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin"));
        }

        @Test
        @DisplayName("should fall back to /admin when redirect is blank")
        void blankRedirect() throws Exception {
            mvc.perform(post("/admin/login")
                            .param("token", "test-token")
                            .param("redirect", "   "))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin"));
        }
    }

    @Nested
    @DisplayName("GET /admin/logout")
    class Logout {

        @Test
        @DisplayName("should clear cookie and redirect to login page")
        void logout() throws Exception {
            mvc.perform(get("/admin/logout"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/login?logout"))
                    .andExpect(cookie().value("sw_token", ""))
                    .andExpect(cookie().maxAge("sw_token", 0));
        }
    }
}