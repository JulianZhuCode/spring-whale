package io.github.springwhale.rbac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String message;
}
