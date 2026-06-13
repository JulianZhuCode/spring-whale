package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Remove menu request
 */
@Data
public class RemoveMenuRequest {

    /**
     * Role ID
     */
    @NotNull(message = "Role ID must not be empty")
    private Integer roleId;

    /**
     * Menu ID
     */
    @NotNull(message = "Menu ID must not be empty")
    private Integer menuId;
}
