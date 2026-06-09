package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * 用户视图对象
 */
@Data
public class UserVO {
    private Integer id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Integer groupId;
}
