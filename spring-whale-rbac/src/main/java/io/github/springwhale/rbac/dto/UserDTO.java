package io.github.springwhale.rbac.dto;

import lombok.Data;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Integer groupId;
}
