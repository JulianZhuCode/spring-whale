package io.github.springwhale.database.datascope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeServerInterceptorTest {

    private DataScopeProperties properties;
    private DataScopeSigner signer;
    private DataScopeServerInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        properties = new DataScopeProperties();
        properties.setTimestampWindow(Duration.ofMinutes(5));
        signer = new DataScopeSigner(null, properties.getTimestampWindow().toMillis());
        interceptor = new DataScopeServerInterceptor(properties, signer);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    @Test
    @DisplayName("Should push scope from valid headers and return true")
    void shouldPushScopeFromValidHeaders() {
        request.addHeader("X-DataScope-Type", "DEPT");
        request.addHeader("X-DataScope-Module", "order");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(DataScopeContext.hasScope()).isTrue();
        assertThat(DataScopeContext.getScope().getScopeType()).isEqualTo(DataScopeType.DEPT);
        assertThat(DataScopeContext.getScope().getModule()).isEqualTo("order");
    }

    @Test
    @DisplayName("Should not push scope when transmit is disabled")
    void shouldNotPushScopeWhenTransmitDisabled() {
        properties.setTransmitEnabled(false);
        request.addHeader("X-DataScope-Type", "DEPT");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("Should not push scope when no headers present")
    void shouldNotPushScopeWhenNoHeaders() {
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("Should not push scope when scope type header is empty")
    void shouldNotPushScopeWhenScopeTypeEmpty() {
        request.addHeader("X-DataScope-Type", "");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("Should not push scope when scope type is invalid")
    void shouldNotPushScopeWhenScopeTypeInvalid() {
        request.addHeader("X-DataScope-Type", "INVALID_TYPE");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("Should push scope without module when module header is missing")
    void shouldPushScopeWithoutModule() {
        request.addHeader("X-DataScope-Type", "SELF");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(DataScopeContext.hasScope()).isTrue();
        assertThat(DataScopeContext.getScope().getScopeType()).isEqualTo(DataScopeType.SELF);
        assertThat(DataScopeContext.getScope().getModule()).isNull();
    }

    @Test
    @DisplayName("Should clear scope after completion when scope was pushed")
    void shouldClearScopeAfterCompletion() {
        request.addHeader("X-DataScope-Type", "DEPT");
        interceptor.preHandle(request, response, new Object());

        assertThat(DataScopeContext.hasScope()).isTrue();

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("Should always clear context after completion, even when scope was pushed externally")
    void shouldAlwaysClearContextAfterCompletion() {
        DataScopeResult scope = new DataScopeResult();
        scope.setScopeType(DataScopeType.SELF);
        DataScopeContext.pushScope(scope);

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("Should use custom header names from properties")
    void shouldUseCustomHeaderNames() {
        properties.setScopeTypeHeader("X-Custom-Type");
        properties.setModuleHeader("X-Custom-Module");
        request.addHeader("X-Custom-Type", "DEPT_AND_CHILD");
        request.addHeader("X-Custom-Module", "report");

        interceptor.preHandle(request, response, new Object());

        assertThat(DataScopeContext.hasScope()).isTrue();
        assertThat(DataScopeContext.getScope().getScopeType()).isEqualTo(DataScopeType.DEPT_AND_CHILD);
        assertThat(DataScopeContext.getScope().getModule()).isEqualTo("report");
    }
}