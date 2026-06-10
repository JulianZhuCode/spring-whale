package io.github.springwhale.framework.webmvc.security;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt == null || !StringUtils.hasText(jwt)) {
                // Only log for admin pages (skip static resources to reduce noise)
                if (requestURI.startsWith("/admin") && !requestURI.startsWith("/admin/css") && !requestURI.startsWith("/admin/js")) {
                    log.warn("JWT not found in request: {}", requestURI);
                }
                filterChain.doFilter(request, response);
                return;
            }

            log.info("JWT found for request: {}, token length: {}, token preview: {}...", 
                     requestURI, jwt.length(), jwt.substring(0, Math.min(30, jwt.length())));

            if (jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.getUsernameFromToken(jwt);
                Integer userId = jwtUtil.getUserIdFromToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                AuthenticationContextHolder.setContext(new SimpleAuthenticationContext(userId, username));

                log.info("Set authentication for user: {} on request: {}", username, requestURI);
            } else {
                log.warn("JWT validation failed for request: {}, token preview: {}...", 
                         requestURI, jwt.substring(0, Math.min(30, jwt.length())));
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context for request: {}", requestURI, e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthenticationContextHolder.clearContext();
        }
    }

    /**
     * Extract JWT from request, checking both the Authorization header
     * (for REST API clients) and the "sw_token" cookie (for admin console).
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Check Authorization header first (API clients)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // Fall back to cookie (admin console)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sw_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
