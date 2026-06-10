package io.github.springwhale.framework.thymeleaf.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin login page controller.
 * <p>
 * The login page is publicly accessible. On successful authentication
 * the JWT token is stored in a cookie so that subsequent admin page
 * requests are automatically authenticated by {@code JwtAuthenticationFilter}.
 * </p>
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    private static final String TOKEN_COOKIE = "sw_token";
    private static final int COOKIE_MAX_AGE = 86400; // 24 hours

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error,
                            @RequestParam(name = "reason", required = false) String reason,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (reason != null) {
            log.warn("Redirected to login page with reason: {}", reason);
            model.addAttribute("reason", reason);
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam(name = "token") String token,
                               @RequestParam(name = "redirect", required = false) String redirect,
                               HttpServletResponse response) {
        log.info("Processing login: token length={}, redirect={}", 
                 token != null ? token.length() : 0, redirect);

        Cookie cookie = new Cookie(TOKEN_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setSecure(false); // Set to true in production with HTTPS
        response.addCookie(cookie);
        log.info("Set cookie sw_token, maxAge={}, path=/", COOKIE_MAX_AGE);

        if (redirect != null && !redirect.isBlank() && !redirect.equals("/admin/login")) {
            log.info("Redirecting to: {}", redirect);
            return "redirect:" + redirect;
        }
        log.info("Redirecting to: /admin");
        return "redirect:/admin";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/admin/login?logout";
    }
}
