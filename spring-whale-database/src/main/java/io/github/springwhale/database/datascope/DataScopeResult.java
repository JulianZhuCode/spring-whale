package io.github.springwhale.database.datascope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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