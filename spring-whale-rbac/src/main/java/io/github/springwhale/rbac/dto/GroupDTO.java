package io.github.springwhale.rbac.dto;

import lombok.Data;

/**
 * 分组（部门）数据传输对象
 */
@Data
public class GroupDTO {
    private Integer id;
    private Integer parentId;
    private String code;
    private String name;
    private String description;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
}
