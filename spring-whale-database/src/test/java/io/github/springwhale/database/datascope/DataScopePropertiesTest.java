package io.github.springwhale.database.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopePropertiesTest {

    @Test
    @DisplayName("default values are set correctly")
    void testDefaults() {
        DataScopeProperties properties = new DataScopeProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isTransmitEnabled()).isTrue();
        assertThat(properties.isTenantEnabled()).isTrue();
        assertThat(properties.getScopeTypeHeader()).isEqualTo("X-DataScope-Type");
        assertThat(properties.getModuleHeader()).isEqualTo("X-DataScope-Module");
        assertThat(properties.getTenantIdHeader()).isEqualTo("X-Tenant-Id");
        assertThat(properties.getRemoteRbacUrl()).isNull();
        assertThat(properties.isExposeRemoteApi()).isFalse();
    }

    @Test
    @DisplayName("cache default TTL values are set correctly")
    void testCacheDefaults() {
        DataScopeProperties.Cache cache = new DataScopeProperties.Cache();

        assertThat(cache.getSkipTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(cache.getDeptTtl()).isEqualTo(Duration.ofMinutes(2));
        assertThat(cache.getFallbackTtl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(cache.getNullTtl()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("enabled setter works correctly")
    void testEnabled() {
        DataScopeProperties properties = new DataScopeProperties();
        properties.setEnabled(false);
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("transmitEnabled setter works correctly")
    void testTransmitEnabled() {
        DataScopeProperties properties = new DataScopeProperties();
        properties.setTransmitEnabled(false);
        assertThat(properties.isTransmitEnabled()).isFalse();
    }

    @Test
    @DisplayName("tenantEnabled setter works correctly")
    void testTenantEnabled() {
        DataScopeProperties properties = new DataScopeProperties();
        properties.setTenantEnabled(false);
        assertThat(properties.isTenantEnabled()).isFalse();
    }

    @Test
    @DisplayName("custom header names are settable")
    void testCustomHeaders() {
        DataScopeProperties properties = new DataScopeProperties();
        properties.setScopeTypeHeader("X-Custom-Type");
        properties.setModuleHeader("X-Custom-Module");
        properties.setTenantIdHeader("X-Custom-Tenant");

        assertThat(properties.getScopeTypeHeader()).isEqualTo("X-Custom-Type");
        assertThat(properties.getModuleHeader()).isEqualTo("X-Custom-Module");
        assertThat(properties.getTenantIdHeader()).isEqualTo("X-Custom-Tenant");
    }

    @Test
    @DisplayName("remoteRbacUrl is settable")
    void testRemoteRbacUrl() {
        DataScopeProperties properties = new DataScopeProperties();
        properties.setRemoteRbacUrl("http://rbac:8080");
        assertThat(properties.getRemoteRbacUrl()).isEqualTo("http://rbac:8080");
    }

    @Test
    @DisplayName("exposeRemoteApi is settable")
    void testExposeRemoteApi() {
        DataScopeProperties properties = new DataScopeProperties();
        properties.setExposeRemoteApi(true);
        assertThat(properties.isExposeRemoteApi()).isTrue();
    }

    @Test
    @DisplayName("cache TTL custom values are settable")
    void testCustomCacheTtl() {
        DataScopeProperties.Cache cache = new DataScopeProperties.Cache();
        cache.setSkipTtl(Duration.ofMinutes(10));
        cache.setDeptTtl(Duration.ofMinutes(5));
        cache.setFallbackTtl(Duration.ofHours(1));
        cache.setNullTtl(Duration.ofMinutes(1));

        assertThat(cache.getSkipTtl()).isEqualTo(Duration.ofMinutes(10));
        assertThat(cache.getDeptTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(cache.getFallbackTtl()).isEqualTo(Duration.ofHours(1));
        assertThat(cache.getNullTtl()).isEqualTo(Duration.ofMinutes(1));
    }
}