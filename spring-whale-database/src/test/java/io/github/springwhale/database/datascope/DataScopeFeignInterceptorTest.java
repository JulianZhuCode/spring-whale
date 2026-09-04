package io.github.springwhale.database.datascope;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeFeignInterceptorTest {

    private DataScopeProperties properties;
    private DataScopeSigner signer;
    private DataScopeFeignInterceptor interceptor;
    private RequestTemplate template;

    @BeforeEach
    void setUp() {
        properties = new DataScopeProperties();
        properties.setTimestampWindow(Duration.ofMinutes(5));
        signer = new DataScopeSigner(null, properties.getTimestampWindow().toMillis());
        interceptor = new DataScopeFeignInterceptor(properties, signer);
        template = new RequestTemplate();
    }

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    @Test
    @DisplayName("Should transmit scope type and module headers when scope is present")
    void shouldTransmitHeadersWhenScopePresent() {
        DataScopeResult scope = new DataScopeResult();
        scope.setScopeType(DataScopeType.DEPT);
        scope.setModule("order");
        DataScopeContext.pushScope(scope);

        interceptor.apply(template);

        Collection<String> typeHeaders = template.headers().get(properties.getScopeTypeHeader());
        Collection<String> moduleHeaders = template.headers().get(properties.getModuleHeader());

        assertThat(typeHeaders).isNotNull().containsExactly("DEPT");
        assertThat(moduleHeaders).isNotNull().containsExactly("order");
    }

    @Test
    @DisplayName("Should not transmit headers when transmit is disabled")
    void shouldNotTransmitWhenDisabled() {
        properties.setTransmitEnabled(false);

        DataScopeResult scope = new DataScopeResult();
        scope.setScopeType(DataScopeType.DEPT);
        scope.setModule("order");
        DataScopeContext.pushScope(scope);

        interceptor.apply(template);

        Collection<String> typeHeaders = template.headers().get(properties.getScopeTypeHeader());
        Collection<String> moduleHeaders = template.headers().get(properties.getModuleHeader());

        assertThat(typeHeaders).isNull();
        assertThat(moduleHeaders).isNull();
    }

    @Test
    @DisplayName("Should not transmit headers when scope is null")
    void shouldNotTransmitWhenScopeNull() {
        DataScopeContext.clear();

        interceptor.apply(template);

        Collection<String> typeHeaders = template.headers().get(properties.getScopeTypeHeader());
        Collection<String> moduleHeaders = template.headers().get(properties.getModuleHeader());

        assertThat(typeHeaders).isNull();
        assertThat(moduleHeaders).isNull();
    }

    @Test
    @DisplayName("Should not transmit headers when scope type is null")
    void shouldNotTransmitWhenScopeTypeNull() {
        DataScopeResult scope = new DataScopeResult();
        scope.setModule("order");
        DataScopeContext.pushScope(scope);

        interceptor.apply(template);

        Collection<String> typeHeaders = template.headers().get(properties.getScopeTypeHeader());
        Collection<String> moduleHeaders = template.headers().get(properties.getModuleHeader());

        assertThat(typeHeaders).isNull();
        assertThat(moduleHeaders).isNull();
    }

    @Test
    @DisplayName("Should transmit only scope type header when module is empty")
    void shouldTransmitOnlyTypeHeaderWhenModuleEmpty() {
        DataScopeResult scope = new DataScopeResult();
        scope.setScopeType(DataScopeType.SELF);
        DataScopeContext.pushScope(scope);

        interceptor.apply(template);

        Collection<String> typeHeaders = template.headers().get(properties.getScopeTypeHeader());
        Collection<String> moduleHeaders = template.headers().get(properties.getModuleHeader());

        assertThat(typeHeaders).isNotNull().containsExactly("SELF");
        assertThat(moduleHeaders).isNull();
    }

    @Test
    @DisplayName("Should use custom header names from properties")
    void shouldUseCustomHeaderNames() {
        properties.setScopeTypeHeader("X-Custom-Type");
        properties.setModuleHeader("X-Custom-Module");

        DataScopeResult scope = new DataScopeResult();
        scope.setScopeType(DataScopeType.DEPT);
        scope.setModule("order");
        DataScopeContext.pushScope(scope);

        interceptor.apply(template);

        Collection<String> typeHeaders = template.headers().get("X-Custom-Type");
        Collection<String> moduleHeaders = template.headers().get("X-Custom-Module");

        assertThat(typeHeaders).isNotNull().containsExactly("DEPT");
        assertThat(moduleHeaders).isNotNull().containsExactly("order");
    }
}