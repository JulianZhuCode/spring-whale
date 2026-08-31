package io.github.springwhale.framework.webmvc.security;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * A {@code OncePerRequestFilter} that extracts a JWT from the incoming request,
 * validates it, and sets both the Spring Security authentication context and
 * the custom {@link AuthenticationContextHolder}.
 *
 * <h3>Authentication flow</h3>
 * <ol>
 *   <li>Extract JWT from the {@code Authorization} header or the configured cookie</li>
 *   <li>Validate the token signature and expiration</li>
 *   <li>Load {@link UserDetails} and set {@link SecurityContextHolder}</li>
 *   <li>Set {@link AuthenticationContextHolder} for downstream use</li>
 * </ol>
 *
 * <p>Missing or invalid tokens do not block the request — the filter chain
 * continues, and the {@link org.springframework.security.web.AuthenticationEntryPoint} (configured in
 * {@code SecurityAutoConfiguration}) decides whether to redirect (admin pages)
 * or return 401 (REST APIs).</p>
 *
 * <p>The {@link AuthenticationContextHolder} is always cleared in {@code finally}
 * to prevent thread-local leaks.</p>
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ADMIN_PATH_PREFIX = "/admin";
    private static final Set<String> ADMIN_STATIC_PATH_PREFIXES = Set.of("/admin/css", "/admin/js");
    private static final int TOKEN_PREVIEW_MAX_LENGTH = 30;

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        try {
            String jwt = jwtUtil.extractJwtFromRequest(request);
            if (jwt == null) {
                handleMissingJwt(requestURI);
                filterChain.doFilter(request, response);
                return;
            }

            authenticateWithJwt(jwt, request, requestURI);
        } catch (Exception e) {
            log.error("Authentication failed for request: {}", requestURI, e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthenticationContextHolder.clearContext();
            SecurityContextHolder.clearContext();
        }
    }

    private void handleMissingJwt(String requestURI) {
        if (isAdminPage(requestURI)) {
            log.warn("JWT not found in request: {}", requestURI);
        }
    }

    private void authenticateWithJwt(String jwt, HttpServletRequest request, String requestURI) {
        if (!jwtUtil.validateToken(jwt)) {
            log.warn("JWT validation failed for request: {}, token preview: {}...",
                    requestURI, truncateToken(jwt));
            return;
        }

        String username = jwtUtil.getUsernameFromToken(jwt);
        Integer userId = jwtUtil.getUserIdFromToken(jwt);
        Object tenantId = jwtUtil.getTenantIdFromToken(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        setSpringSecurityAuthentication(userDetails, request);
        setApplicationContext(userId, username, tenantId);

        log.debug("Authenticated user '{}' for request: {}", username, requestURI);
    }

    private void setSpringSecurityAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setApplicationContext(Integer userId, String username, Object tenantId) {
        AuthenticationContextHolder.setContext(new AuthenticationContext(userId, username, tenantId));
    }

    private static boolean isAdminPage(String requestURI) {
        if (!requestURI.startsWith(ADMIN_PATH_PREFIX)) {
            return false;
        }
        return ADMIN_STATIC_PATH_PREFIXES.stream().noneMatch(requestURI::startsWith);
    }

    private static String truncateToken(String token) {
        int length = Math.min(token.length(), TOKEN_PREVIEW_MAX_LENGTH);
        return token.substring(0, length);
    }
}