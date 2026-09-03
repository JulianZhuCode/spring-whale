package io.github.springwhale.test.security;

import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.framework.webmvc.security.SecurityProperties;
import io.github.springwhale.test.TestSecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@Import(TestSecurityConfiguration.class)
@DisplayName("JwtAuthenticationFilter integration tests")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = jwtUtil.generateToken("testuser", 1001L, null);
        assertTrue(jwtUtil.validateToken(validToken), "Token should be valid immediately after generation");
    }

    @Test
    @DisplayName("should authenticate when valid JWT is provided in Authorization header")
    void testAuthenticateWithValidJwt() throws Exception {
        mvc.perform(get("/api/secure")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.ctxUserId").value(1001))
                .andExpect(jsonPath("$.data.ctxUsername").value("testuser"));
    }

    @Test
    @DisplayName("should not authenticate when no JWT is provided")
    void testNoJwtProvided() throws Exception {
        mvc.perform(get("/api/secure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("anonymousUser"))
                .andExpect(jsonPath("$.data.ctxUserId").isEmpty());
    }

    @Test
    @DisplayName("should not authenticate when JWT is tampered")
    void testTamperedJwt() throws Exception {
        String tampered = validToken.substring(0, validToken.length() - 3) + "xyz";

        mvc.perform(get("/api/secure")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("anonymousUser"));
    }

    @Test
    @DisplayName("should not authenticate when JWT has expired")
    void testExpiredJwt() throws Exception {
        SecurityProperties shortLivedProps = new SecurityProperties();
        shortLivedProps.setJwtSecret("MyTestSecretKeyForJWT2024WithAtLeast32Bytes!");
        shortLivedProps.setJwtExpiration(1L);
        JwtUtil shortLivedJwtUtil = new JwtUtil(shortLivedProps);
        String expiredToken = shortLivedJwtUtil.generateToken("testuser", 1001L, null);

        Thread.sleep(10);

        mvc.perform(get("/api/secure")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("anonymousUser"));
    }

    @Test
    @DisplayName("should authenticate via token cookie for admin pages")
    void testAuthenticateViaCookie() throws Exception {
        mvc.perform(get("/admin/secure")
                        .cookie(new jakarta.servlet.http.Cookie("sw_token", validToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("should continue filter chain for public endpoints without JWT")
    void testPublicEndpointWithoutJwt() throws Exception {
        mvc.perform(get("/api/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("anonymousUser"));
    }

    @Test
    @DisplayName("should clear AuthenticationContextHolder after each request")
    void testContextClearedAfterRequest() throws Exception {
        mvc.perform(get("/api/secure")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));

        mvc.perform(get("/api/secure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("anonymousUser"))
                .andExpect(jsonPath("$.data.ctxUserId").isEmpty());
    }
}