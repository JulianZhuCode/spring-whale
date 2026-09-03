package io.github.springwhale.platform.rbac.dto.vo;

import lombok.Data;

/**
 * User view object
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Long groupId;
    private String groupName;
}
