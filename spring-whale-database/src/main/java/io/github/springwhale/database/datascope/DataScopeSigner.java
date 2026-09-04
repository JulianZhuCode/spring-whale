package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HMAC-SHA256 signer and verifier for cross-service data scope header
 * integrity protection.
 *
 * <h3>Signing payload format</h3>
 * <pre>scopeType:module:tenantId:timestamp:nonce:path</pre>
 *
 * <h3>Security properties</h3>
 * <ul>
 *   <li>HMAC-SHA256 with configurable shared secret</li>
 *   <li>Timestamp validation with configurable clock skew window</li>
 *   <li>Nonce-based replay protection with in-memory deduplication</li>
 *   <li>Constant-time signature comparison via {@link MessageDigest#isEqual}</li>
 * </ul>
 */
@Slf4j
public class DataScopeSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PAYLOAD_DELIMITER = ":";

    private final byte[] secretKey;
    private final long timestampWindowMillis;
    private final boolean enabled;

    private final Map<String, Long> nonceStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor;

    public DataScopeSigner(String hmacSecretKey, long timestampWindowMillis) {
        if (hmacSecretKey != null && !hmacSecretKey.isBlank()) {
            this.secretKey = hmacSecretKey.getBytes(StandardCharsets.UTF_8);
            this.enabled = true;
            log.info("DataScope HMAC signing enabled (timestamp window={}ms)", timestampWindowMillis);
        } else {
            this.secretKey = null;
            this.enabled = false;
            log.warn("DataScope HMAC signing DISABLED — no hmac-secret-key configured. "
                    + "Cross-service data scope headers are unprotected against forgery.");
        }
        this.timestampWindowMillis = timestampWindowMillis;

        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "datascope-nonce-cleanup");
            t.setDaemon(true);
            return t;
        });
        this.cleanupExecutor.scheduleAtFixedRate(
                this::evictExpiredNonces,
                timestampWindowMillis,
                timestampWindowMillis,
                TimeUnit.MILLISECONDS);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String sign(String scopeType, String module, String tenantId,
                       long timestamp, String nonce, String path) {
        if (!enabled) {
            return "";
        }
        String payload = buildPayload(scopeType, module, tenantId, timestamp, nonce, path);
        return computeHmac(payload);
    }

    public boolean verify(String signature, String scopeType, String module,
                          String tenantId, long timestamp, String nonce, String path) {
        if (!enabled) {
            return true;
        }
        if (signature == null || signature.isBlank()) {
            log.warn("DataScope signature verification failed: missing signature header");
            return false;
        }

        if (!isTimestampValid(timestamp)) {
            log.warn("DataScope signature verification failed: timestamp {} outside window ({}ms)",
                    timestamp, timestampWindowMillis);
            return false;
        }

        if (!checkAndStoreNonce(nonce)) {
            log.warn("DataScope signature verification failed: nonce {} already used or expired", nonce);
            return false;
        }

        String payload = buildPayload(scopeType, module, tenantId, timestamp, nonce, path);
        String expected = computeHmac(payload);

        byte[] sigBytes = signature.getBytes(StandardCharsets.UTF_8);
        byte[] expBytes = expected.getBytes(StandardCharsets.UTF_8);

        boolean valid = MessageDigest.isEqual(sigBytes, expBytes);
        if (!valid) {
            log.warn("DataScope signature verification failed: signature mismatch for path={}", path);
        }
        return valid;
    }

    private String buildPayload(String scopeType, String module, String tenantId,
                                long timestamp, String nonce, String path) {
        return String.join(PAYLOAD_DELIMITER,
                nullToEmpty(scopeType),
                nullToEmpty(module),
                nullToEmpty(tenantId),
                String.valueOf(timestamp),
                nullToEmpty(nonce),
                nullToEmpty(path));
    }

    private String computeHmac(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey, HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256", e);
        }
    }

    private boolean isTimestampValid(long timestamp) {
        long now = System.currentTimeMillis();
        return Math.abs(now - timestamp) <= timestampWindowMillis;
    }

    private boolean checkAndStoreNonce(String nonce) {
        if (nonce == null || nonce.isBlank()) {
            return false;
        }
        long now = System.currentTimeMillis();
        long expiresAt = now + timestampWindowMillis;

        Long existing = nonceStore.putIfAbsent(nonce, expiresAt);
        if (existing != null) {
            if (existing > now) {
                return false;
            }
            nonceStore.put(nonce, expiresAt);
        }
        return true;
    }

    private void evictExpiredNonces() {
        long now = System.currentTimeMillis();
        nonceStore.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}