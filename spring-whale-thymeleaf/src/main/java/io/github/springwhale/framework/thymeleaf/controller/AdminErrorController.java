package io.github.springwhale.framework.thymeleaf.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Error page controller for the admin console.
 * <p>
 * Returns friendly HTML error pages instead of the default JSON
 * error responses from {@code SpringWhaleWebMvcExceptionHandler}.
 * Uses a single generic error template for all status codes.
 * </p>
 */
@Controller
public class AdminErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute(
                RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == null) {
            statusCode = 500;
        }

        model.addAttribute("status", statusCode);

        String message = switch (statusCode) {
            case 404 -> "The page you are looking for does not exist.";
            case 403 -> "You do not have permission to access this page.";
            case 401 -> "Authentication is required to access this page.";
            case 500 -> "An internal server error occurred.";
            default -> "An unexpected error occurred.";
        };
        model.addAttribute("message", message);

        return "error/error";
    }
}
