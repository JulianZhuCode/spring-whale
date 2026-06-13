package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Batch assign roles request
 */
@Data
public class AssignRolesRequest {

    /**
     * User ID
     */
    @NotNull(message = "User ID must not be empty")
    private Integer userId;

    /**
     * Role ID list
     */
    @NotEmpty(message = "Role ID list must not be empty")
    private List<Integer> roleIds;
}
