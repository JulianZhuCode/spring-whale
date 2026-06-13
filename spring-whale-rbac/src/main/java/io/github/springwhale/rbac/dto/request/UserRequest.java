package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * User create/update request
 */
@Data
public class UserRequest {

    @NotBlank(message = "Username must not be empty")
    private String username;

    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Integer groupId;
}
