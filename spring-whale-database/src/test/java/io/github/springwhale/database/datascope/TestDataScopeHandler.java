package io.github.springwhale.database.datascope;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.core.context.AuthenticationContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestDataScopeHandler implements DataScopeHandler {

    private static volatile boolean skipDataScope = false;
    private static volatile boolean skipTenantScope = false;
    private static volatile boolean returnNullDeptIds = false;

    public static void setSkipDataScope(boolean skip) {
        skipDataScope = skip;
    }

    public static void setSkipTenantScope(boolean skip) {
        skipTenantScope = skip;
    }

    public static void setReturnNullDeptIds(boolean returnNull) {
        returnNullDeptIds = returnNull;
    }

    public static void reset() {
        skipDataScope = false;
        skipTenantScope = false;
        returnNullDeptIds = false;
    }

    @Override
    public boolean skipDataScope() {
        return skipDataScope;
    }

    @Override
    public boolean skipTenantScope() {
        return skipTenantScope;
    }

    @Override
    public Long resolveUserId() {
        AuthenticationContext ctx = AuthenticationContextHolder.getContext();
        return ctx != null ? ctx.getUserId() : null;
    }

    @Override
    public List<Object> resolveDeptIds(DataScopeType scopeType, String module) {
        if (returnNullDeptIds) {
            return null;
        }

        AuthenticationContext ctx = AuthenticationContextHolder.getContext();
        if (ctx == null || ctx.getUserId() == null) {
            return Collections.emptyList();
        }

        Long currentUserId = Long.valueOf(ctx.getUserId());
        List<Object> deptIds = new ArrayList<>();

        switch (scopeType) {
            case SELF:
                return Collections.emptyList();
            case DEPT:
                deptIds.add(departmentOf(currentUserId));
                break;
            case DEPT_AND_CHILD:
                deptIds.add(departmentOf(currentUserId));
                deptIds.addAll(childDepartmentsOf(currentUserId));
                break;
            case CUSTOM:
                deptIds.add(departmentOf(currentUserId));
                break;
            case AUTO:
                deptIds.add(departmentOf(currentUserId));
                break;
            case CALLER:
                deptIds.add(departmentOf(currentUserId));
                break;
        }
        return deptIds;
    }

    private Long departmentOf(Long userId) {
        if (userId == 1 || userId == 2) {
            return 1L;
        }
        if (userId == 3) {
            return 2L;
        }
        return 99L;
    }

    private List<Long> childDepartmentsOf(Long userId) {
        if (userId == 1 || userId == 2) {
            return Collections.singletonList(2L);
        }
        return Collections.emptyList();
    }
}