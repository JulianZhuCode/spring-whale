package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户创建/更新请求
 */
@Data
public class UserRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Integer groupId;
}
