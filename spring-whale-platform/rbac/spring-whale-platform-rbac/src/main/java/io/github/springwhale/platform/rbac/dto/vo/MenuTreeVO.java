package io.github.springwhale.platform.rbac.dto.vo;

import io.github.springwhale.platform.rbac.enums.MenuType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu tree node view object for role-menu assignment
 */
@Data
public class MenuTreeVO {
    private Integer id;
    private Integer parentId;
    private String code;
    private String name;
    private MenuType type;
    private String permission;
    private List<MenuTreeVO> children = new ArrayList<>();
}