package io.github.springwhale.thymeleaf.test.controller;

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
class AdminConsoleControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /admin should return dashboard with menuGroups and adminProps")
    void dashboard() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("menuGroups"))
                .andExpect(model().attributeExists("currentPath"))
                .andExpect(model().attributeExists("adminProps"))
                .andExpect(model().attributeExists("userAuthorities"))
                .andExpect(model().attribute("currentPath", "/admin"));
    }
}