package io.github.springwhale.framework.webmvc.security;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.core.utils.LogConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        try {
            String jwt = jwtUtil.extractJwtFromRequest(request);
            AuthenticationResult result = resolveAuthentication(jwt, request, requestURI);
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            SecurityContextHolder.getContext().setAuthentication(
                    result.authentication() != null ? result.authentication() : current);
            if (result.authentication() != null) {
                setApplicationContext(result.userId(), result.username(), result.tenantId());
            }
            filterChain.doFilter(request, response);
        } finally {
            AuthenticationContextHolder.clearContext();
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Resolves the authentication for the given JWT. Returns a null authentication
     * (and null context values) when the token is missing or invalid, leaving the
     * request unauthenticated — the security filter chain then enforces access.
     */
    private AuthenticationResult resolveAuthentication(String jwt, HttpServletRequest request, String requestURI) {
        if (jwt == null) {
            log.debug("JWT not found in request: {}", requestURI.replaceAll(LogConstants.LINE_BREAKS, LogConstants.PLACEHOLDER));
            return new AuthenticationResult(null, null, null, null);
        }

        if (!jwtUtil.validateToken(jwt)) {
            log.warn("JWT validation failed for request: {}, token length: {}",
                    requestURI.replaceAll(LogConstants.LINE_BREAKS, LogConstants.PLACEHOLDER), jwt.length());
            return new AuthenticationResult(null, null, null, null);
        }

        String username = jwtUtil.getUsernameFromToken(jwt);
        Long userId = jwtUtil.getUserIdFromToken(jwt);
        Long tenantId = jwtUtil.getTenantIdFromToken(jwt);

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.warn("User not found in token for request: {}, username: {}",
                    requestURI.replaceAll(LogConstants.LINE_BREAKS, LogConstants.PLACEHOLDER),
                    String.valueOf(username).replaceAll(LogConstants.LINE_BREAKS, LogConstants.PLACEHOLDER));
            return new AuthenticationResult(null, null, null, null);
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return new AuthenticationResult(authentication, userId, username, tenantId);
    }

    private void setApplicationContext(Long userId, String username, Long tenantId) {
        AuthenticationContextHolder.setContext(new AuthenticationContext(userId, username, tenantId));
    }

    private record AuthenticationResult(Authentication authentication, Long userId, String username, Long tenantId) {
    }
}
