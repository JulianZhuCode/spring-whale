package io.github.springwhale.framework.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication context holding the current user's identity information.
 * <p>Plain POJO (not a record) for easier field extension in the future.</p>
 * <p>Jackson serializes/deserializes this natively via getter/setter conventions.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationContext {
    private Long userId;
    private String username;
    private Long tenantId;
}