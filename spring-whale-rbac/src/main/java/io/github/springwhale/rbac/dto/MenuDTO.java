package io.github.springwhale.rbac.dto;

import lombok.Data;

/**
 * 菜单数据传输对象
 */
@Data
public class MenuDTO {
    private Integer id;
    private Integer parentId;
    private String code;
    private String name;
    private Integer type;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
