package io.github.springwhale.test.security;

import feign.RequestTemplate;
import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.framework.webmvc.security.SecurityFeignInterceptor;
import io.github.springwhale.framework.webmvc.security.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SecurityFeignInterceptor unit tests")
class SecurityFeignInterceptorTest {

    private SecurityProperties securityProperties;
    private JwtUtil jwtUtil;
    private SecurityFeignInterceptor interceptor;
    private MockedStatic<RequestContextHolder> requestContextHolderMock;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        jwtUtil = mock(JwtUtil.class);
        interceptor = new SecurityFeignInterceptor(securityProperties, jwtUtil);
        requestContextHolderMock = mockStatic(RequestContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        requestContextHolderMock.close();
    }

    @Test
    @DisplayName("should add Authorization header when JWT is present in request")
    void testApplyWithValidJwt() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtil.extractJwtFromRequest(request)).thenReturn("my.jwt.token");

        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
                .thenReturn(attributes);

        RequestTemplate template = new RequestTemplate();
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer my.jwt.token");

        interceptor.apply(template);

        Map<String, java.util.Collection<String>> headers = template.headers();
        assertTrue(headers.containsKey("Authorization"));
        assertTrue(headers.get("Authorization").contains("Bearer my.jwt.token"));
    }

    @Test
    @DisplayName("should skip when no request attributes are available")
    void testApplyNoRequestAttributes() {
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
                .thenReturn(null);

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().isEmpty());
    }

    @Test
    @DisplayName("should skip when request has no JWT")
    void testApplyNoJwt() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtil.extractJwtFromRequest(request)).thenReturn(null);

        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
                .thenReturn(attributes);

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().isEmpty());
    }

    @Test
    @DisplayName("should skip when JWT is empty string")
    void testApplyEmptyJwt() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtil.extractJwtFromRequest(request)).thenReturn("");

        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        requestContextHolderMock.when(RequestContextHolder::getRequestAttributes)
                .thenReturn(attributes);

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().isEmpty());
    }
}