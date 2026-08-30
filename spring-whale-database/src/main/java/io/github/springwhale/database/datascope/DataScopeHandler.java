package io.github.springwhale.database.datascope;

import io.github.springwhale.framework.core.utils.AuthUtil;

import java.util.List;

public interface DataScopeHandler {

    default Object resolveUserId() {
        return AuthUtil.getUserId();
    }

    List<Object> resolveDeptIds(DataScopeType scopeType, String module);
}