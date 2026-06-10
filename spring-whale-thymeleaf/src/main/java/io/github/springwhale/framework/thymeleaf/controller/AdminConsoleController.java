package io.github.springwhale.framework.thymeleaf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unified admin console page controller.
 * <p>
 * Handles the /admin entry point and serves the dashboard.
 * Individual module pages are handled by their own {@code @Controller} classes
 * under /admin/{module}/ paths.
 * </p>
 */
@AdminPage
@Controller
@RequestMapping("/admin")
public class AdminConsoleController {

    @GetMapping
    public String dashboard() {
        return "admin/dashboard";
    }
}
