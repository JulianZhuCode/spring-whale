package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 所属部门ID
     */
    private Integer groupId;
}
