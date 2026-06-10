package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * Group (department) view object
 */
@Data
public class GroupVO {
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
