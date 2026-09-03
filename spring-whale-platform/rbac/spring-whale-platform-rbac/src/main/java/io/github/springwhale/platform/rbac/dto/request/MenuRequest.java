package io.github.springwhale.platform.rbac.dto.request;

import io.github.springwhale.platform.rbac.enums.MenuType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Menu create/update request
 */
@Data
public class MenuRequest {

    private Long parentId;

    @NotBlank(message = "Menu code must not be empty")
    private String code;

    @NotBlank(message = "Menu name must not be empty")
    private String name;

    private String nameI18nKey;

    @NotNull(message = "Menu type must not be empty")
    private MenuType type;

    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}