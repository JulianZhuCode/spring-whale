package io.github.springwhale.test.security;

import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.framework.webmvc.security.SecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtUtil unit tests")
class JwtUtilTest {

    private static final String TEST_SECRET = "MyTestSecretKeyForJWT2024WithAtLeast32Bytes!";
    private static final long TEST_EXPIRATION = 3600000L;

    private JwtUtil jwtUtil;
    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        securityProperties.setJwtSecret(TEST_SECRET);
        securityProperties.setJwtExpiration(TEST_EXPIRATION);
        jwtUtil = new JwtUtil(securityProperties);
    }

    @Test
    @DisplayName("generateToken should create a non-null JWT string")
    void testGenerateToken() {
        String token = jwtUtil.generateToken("testuser", 1001);

        assertNotNull(token);
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("getUsernameFromToken should return the correct username")
    void testGetUsernameFromToken() {
        String token = jwtUtil.generateToken("testuser", 1001);

        String username = jwtUtil.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("getUserIdFromToken should return the correct userId")
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateToken("testuser", 1001);

        Integer userId = jwtUtil.getUserIdFromToken(token);

        assertEquals(1001, userId);
    }

    @Test
    @DisplayName("getUserIdFromToken should return null when userId claim is not present")
    void testGetUserIdFromTokenMissing() {
        String token = jwtUtil.generateToken("testuser", null);

        Integer userId = jwtUtil.getUserIdFromToken(token);

        assertNull(userId);
    }

    @Test
    @DisplayName("validateToken should return true for a valid token")
    void testValidateTokenValid() {
        String token = jwtUtil.generateToken("testuser", 1001);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("validateToken should return false for null token")
    void testValidateTokenNull() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    @DisplayName("validateToken should return false for token without dots")
    void testValidateTokenNoDots() {
        assertFalse(jwtUtil.validateToken("notajwt"));
    }

    @Test
    @DisplayName("validateToken should return false for a tampered token")
    void testValidateTokenTampered() {
        String token = jwtUtil.generateToken("testuser", 1001);

        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    @DisplayName("validateToken should return false for an expired token")
    void testValidateTokenExpired() throws InterruptedException {
        securityProperties.setJwtExpiration(1L);
        jwtUtil = new JwtUtil(securityProperties);
        String token = jwtUtil.generateToken("testuser", 1001);

        Thread.sleep(10);

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("validateToken should return false for token signed with a different secret")
    void testValidateTokenDifferentSecret() {
        String token = jwtUtil.generateToken("testuser", 1001);

        SecurityProperties otherProps = new SecurityProperties();
        otherProps.setJwtSecret("AnotherDifferentSecretKeyForJWT2024!!");
        JwtUtil otherJwtUtil = new JwtUtil(otherProps);

        assertFalse(otherJwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("extractJwtFromRequest should extract token from Authorization header")
    void testExtractJwtFromHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer my.jwt.token");

        String result = jwtUtil.extractJwtFromRequest(request);

        assertEquals("my.jwt.token", result);
    }

    @Test
    @DisplayName("extractJwtFromRequest should extract token from cookie")
    void testExtractJwtFromCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie cookie = new Cookie("sw_token", "my.cookie.jwt");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String result = jwtUtil.extractJwtFromRequest(request);

        assertEquals("my.cookie.jwt", result);
    }

    @Test
    @DisplayName("extractJwtFromRequest should extract token from custom cookie name")
    void testExtractJwtFromCustomCookieName() {
        securityProperties.setTokenCookieName("custom_token");
        jwtUtil = new JwtUtil(securityProperties);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie customCookie = new Cookie("custom_token", "my.custom.jwt");
        when(request.getCookies()).thenReturn(new Cookie[]{customCookie});

        String result = jwtUtil.extractJwtFromRequest(request);

        assertEquals("my.custom.jwt", result);
    }

    @Test
    @DisplayName("extractJwtFromRequest should return null when no token is present")
    void testExtractJwtFromRequestNoToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        String result = jwtUtil.extractJwtFromRequest(request);

        assertNull(result);
    }

    @Test
    @DisplayName("extractJwtFromRequest should return null when header value is empty")
    void testExtractJwtFromRequestEmptyHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("");

        String result = jwtUtil.extractJwtFromRequest(request);

        assertNull(result);
    }

    @Test
    @DisplayName("extractJwtFromRequest should return null when header does not start with token prefix")
    void testExtractJwtFromRequestWrongPrefix() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Token my.jwt.token");

        String result = jwtUtil.extractJwtFromRequest(request);

        assertNull(result);
    }
}