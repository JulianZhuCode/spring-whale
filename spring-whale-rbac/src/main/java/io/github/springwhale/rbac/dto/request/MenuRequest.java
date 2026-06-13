package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Menu create/update request
 */
@Data
public class MenuRequest {

    private Integer parentId;

    @NotBlank(message = "Menu code must not be empty")
    private String code;

    @NotBlank(message = "Menu name must not be empty")
    private String name;

    @NotNull(message = "Menu type must not be empty")
    private Integer type;

    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
