package io.github.springwhale.framework.thymeleaf.controller;

import io.github.springwhale.framework.thymeleaf.config.AdminProperties;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Error page controller for the admin console.
 * <p>
 * Returns friendly HTML error pages instead of the default JSON
 * error responses from {@code SpringWhaleWebMvcExceptionHandler}.
 * Uses a single generic error template for all status codes.
 * Error messages support i18n via {@link MessageSource}.
 * </p>
 */
@Controller
@RequiredArgsConstructor
public class AdminErrorController implements ErrorController {

    private final AdminProperties adminProperties;
    private final MessageSource messageSource;

    @ModelAttribute("adminProps")
    public AdminProperties adminProps() {
        return adminProperties;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute(
                RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == null) {
            statusCode = 500;
        }

        model.addAttribute("status", statusCode);

        String messageKey = switch (statusCode) {
            case 404 -> "http.error.404";
            case 403 -> "http.error.403";
            case 401 -> "http.error.401";
            case 500 -> "http.error.500";
            default -> "http.error.unknown";
        };
        String message = messageSource.getMessage(messageKey, null,
                "An unexpected error occurred.", LocaleContextHolder.getLocale());
        model.addAttribute("message", message);

        return "error/error";
    }
}
