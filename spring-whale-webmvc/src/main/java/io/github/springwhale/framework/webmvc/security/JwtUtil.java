package io.github.springwhale.framework.webmvc.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for creating, parsing, and validating JWT tokens.
 *
 * <p>Tokens are signed with HMAC-SHA using the secret configured via
 * {@link SecurityProperties#getJwtSecret()}. Claims include
 * {@code userId} and {@code username} alongside the standard JWT
 * {@code sub} and {@code exp} fields.</p>
 *
 * <p>Token extraction from HTTP requests supports both the
 * {@code Authorization} header (Bearer scheme) and cookies,
 * as configured in {@link SecurityProperties}.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    private final SecurityProperties securityProperties;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(securityProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Integer userId, Object tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        if (tenantId != null) {
            claims.put("tenantId", tenantId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + securityProperties.getJwtExpiration()))
                .signWith(getSignKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Integer.class);
    }

    public Object getTenantIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("tenantId");
    }

    public boolean validateToken(String token) {
        if (token == null || !token.contains(".")) {
            return false;
        }
        try {
            Claims claims = getClaimsFromToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        if (token == null || !token.contains(".")) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract JWT from request, checking both the Authorization header
     * (for REST API clients) and the token cookie (for admin console).
     * <p>Uses the configured {@link SecurityProperties#getTokenHeader()},
     * {@link SecurityProperties#getTokenPrefix()}, and
     * {@link SecurityProperties#getTokenCookieName()} values.</p>
     */
    public String extractJwtFromRequest(HttpServletRequest request) {
        String headerValue = request.getHeader(securityProperties.getTokenHeader());
        String tokenPrefix = securityProperties.getTokenPrefix();

        if (StringUtils.hasText(headerValue) && headerValue.startsWith(tokenPrefix)) {
            return headerValue.substring(tokenPrefix.length());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String cookieName = securityProperties.getTokenCookieName();
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}