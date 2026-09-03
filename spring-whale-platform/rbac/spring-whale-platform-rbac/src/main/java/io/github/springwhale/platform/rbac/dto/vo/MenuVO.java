package io.github.springwhale.platform.rbac.dto.vo;

import io.github.springwhale.platform.rbac.enums.MenuType;
import lombok.Data;

/**
 * Menu view object
 */
@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String code;
    private String name;
    private String nameI18nKey;
    private MenuType type;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}