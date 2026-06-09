package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {
    
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
