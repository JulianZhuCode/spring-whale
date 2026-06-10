package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * Menu view object
 */
@Data
public class MenuVO {
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
