package io.github.springwhale.database.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeSignerTest {

    private static final String SECRET = "test-secret-key-for-hmac";
    private static final long WINDOW_MS = 300_000;

    private final DataScopeSigner signer = new DataScopeSigner(SECRET, WINDOW_MS);

    @Test
    @DisplayName("Should be enabled when secret is configured")
    void shouldBeEnabled() {
        assertThat(signer.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should be disabled when secret is null")
    void shouldBeDisabledWhenSecretNull() {
        DataScopeSigner disabled = new DataScopeSigner(null, WINDOW_MS);
        assertThat(disabled.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should be disabled when secret is blank")
    void shouldBeDisabledWhenSecretBlank() {
        DataScopeSigner disabled = new DataScopeSigner("   ", WINDOW_MS);
        assertThat(disabled.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should sign and verify successfully")
    void shouldSignAndVerify() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        String signature = signer.sign("DEPT", "order", "100", timestamp, nonce, path);

        assertThat(signature).isNotBlank();

        boolean valid = signer.verify(signature, "DEPT", "order", "100", timestamp, nonce, path);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Should reject tampered scope type")
    void shouldRejectTamperedScopeType() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        String signature = signer.sign("DEPT", "order", "100", timestamp, nonce, path);

        boolean valid = signer.verify(signature, "ALL", "order", "100", timestamp, nonce, path);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should reject tampered tenant id")
    void shouldRejectTamperedTenantId() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        String signature = signer.sign("DEPT", "order", "100", timestamp, nonce, path);

        boolean valid = signer.verify(signature, "DEPT", "order", "999", timestamp, nonce, path);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should reject tampered path")
    void shouldRejectTamperedPath() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();

        String signature = signer.sign("DEPT", "order", "100", timestamp, nonce, "/api/order/list");

        boolean valid = signer.verify(signature, "DEPT", "order", "100", timestamp, nonce, "/api/admin/users");
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should reject expired timestamp")
    void shouldRejectExpiredTimestamp() {
        long expiredTimestamp = System.currentTimeMillis() - WINDOW_MS - 1;
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        String signature = signer.sign("DEPT", "order", "100", expiredTimestamp, nonce, path);

        boolean valid = signer.verify(signature, "DEPT", "order", "100", expiredTimestamp, nonce, path);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should accept timestamp within window")
    void shouldAcceptTimestampWithinWindow() {
        long edgeTimestamp = System.currentTimeMillis() - WINDOW_MS + 1000;
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        String signature = signer.sign("DEPT", "order", "100", edgeTimestamp, nonce, path);

        boolean valid = signer.verify(signature, "DEPT", "order", "100", edgeTimestamp, nonce, path);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Should reject replayed nonce")
    void shouldRejectReplayedNonce() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        String signature = signer.sign("DEPT", "order", "100", timestamp, nonce, path);

        boolean first = signer.verify(signature, "DEPT", "order", "100", timestamp, nonce, path);
        assertThat(first).isTrue();

        boolean second = signer.verify(signature, "DEPT", "order", "100", timestamp, nonce, path);
        assertThat(second).isFalse();
    }

    @Test
    @DisplayName("Should handle null values in payload")
    void shouldHandleNullValues() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        String signature = signer.sign(null, null, "100", timestamp, nonce, path);

        boolean valid = signer.verify(signature, null, null, "100", timestamp, nonce, path);
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Should reject blank signature")
    void shouldRejectBlankSignature() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        boolean valid = signer.verify("", "DEPT", "order", "100", timestamp, nonce, path);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should reject null signature")
    void shouldRejectNullSignature() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/order/list";

        boolean valid = signer.verify(null, "DEPT", "order", "100", timestamp, nonce, path);
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should produce deterministic signature for same input")
    void shouldProduceDeterministicSignature() {
        long timestamp = 1000000L;
        String nonce = "fixed-nonce";
        String path = "/api/test";

        String sig1 = signer.sign("SELF", null, "1", timestamp, nonce, path);
        String sig2 = signer.sign("SELF", null, "1", timestamp, nonce, path);

        assertThat(sig1).isEqualTo(sig2);
    }

    @Test
    @DisplayName("Should produce different signatures for different inputs")
    void shouldProduceDifferentSignatures() {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        String path = "/api/test";

        String sig1 = signer.sign("SELF", null, "1", timestamp, nonce, path);
        String sig2 = signer.sign("ALL", null, "1", timestamp, nonce, path);

        assertThat(sig1).isNotEqualTo(sig2);
    }
}