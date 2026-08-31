package io.github.springwhale.database.datascope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Value object holding the data scope definition for the current request:
 * scope type, module, department IDs, and user ID.
 *
 * <p>{@link #isEmpty()} returns {@code true} when neither department IDs nor
 * user ID is set, indicating that the scope needs to be resolved by the
 * {@link DataScopeHandler}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeResult {

    private DataScopeType scopeType;

    private String module;

    private List<Object> deptIds = new ArrayList<>();

    private Object userId;

    public boolean hasDeptIds() {
        return deptIds != null && !deptIds.isEmpty();
    }

    public boolean hasUserId() {
        return userId != null;
    }

    public boolean isEmpty() {
        return !hasDeptIds() && !hasUserId();
    }
}