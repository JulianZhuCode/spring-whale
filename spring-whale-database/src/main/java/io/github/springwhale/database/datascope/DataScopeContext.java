package io.github.springwhale.database.datascope;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * ThreadLocal-based context for data scope and tenant isolation during a single request.
 *
 * <h3>ThreadLocal inventory</h3>
 * <table>
 *   <tr><th>Holder</th><th>Set by</th><th>Cleared by</th><th>Purpose</th></tr>
 *   <tr><td>{@code SCOPE_STACK}</td><td>{@code DataScopeAspect}, {@code DataScopeServerInterceptor}</td><td>{@code popScope()}, {@code clear()}</td><td>Stack of scope definitions pushed by {@code @DataScope} annotations and HTTP headers</td></tr>
 *   <tr><td>{@code ENTITY_CLASS_HOLDER}</td><td>{@code TenantRepositoryAspect}, {@code DataScopeRepositoryAspect}</td><td>{@code clearEntityInfo()}, {@code clear()}</td><td>Current entity class being queried</td></tr>
 *   <tr><td>{@code DEPT_FIELDS_HOLDER}</td><td>{@code DataScopeRepositoryAspect}</td><td>{@code clearEntityInfo()}, {@code clear()}</td><td>Column names annotated with {@code @DeptIdField}</td></tr>
 *   <tr><td>{@code USER_FIELDS_HOLDER}</td><td>{@code DataScopeRepositoryAspect}</td><td>{@code clearEntityInfo()}, {@code clear()}</td><td>Column names annotated with {@code @UserIdField}</td></tr>
 *   <tr><td>{@code TENANT_ID_HOLDER}</td><td>{@code DataScopeServerInterceptor}, {@code TenantRepositoryAspect}</td><td>{@code clear()}</td><td>Current tenant ID (request-scoped, survives across repository calls)</td></tr>
 *   <tr><td>{@code SKIP_TENANT_HOLDER}</td><td>{@code TenantWebMvcInterceptor}</td><td>{@code clear()}</td><td>Flag to skip tenant filtering for {@code @NonTenant} methods</td></tr>
 *   <tr><td>{@code TENANT_FIELDS_HOLDER}</td><td>{@code TenantRepositoryAspect}</td><td>{@code clearEntityInfo()}, {@code clear()}</td><td>Column names annotated with {@code @TenantIdField}</td></tr>
 * </table>
 *
 * <h3>Lifecycle</h3>
 * <pre>
 * Request start  → DataScopeServerInterceptor.preHandle()  → pushScope() / setTenantId()
 * Controller     → @DataScope → DataScopeAspect           → pushScope()
 * Repository     → TenantRepositoryAspect                 → setTenantId() / setTenantFields() / setEntityClass()
 *                → DataScopeRepositoryAspect              → setEntityClass() / setDeptFields() / setUserFields()
 * SQL inspect    → TenantSqlInspector / DataScopeInterceptor → read all holders
 * Repository end → clearEntityInfo()                       → clears entity/fields
 * Controller end → DataScopeAspect.finally                  → popScope()
 * Request end    → DataScopeServerInterceptor.afterCompletion() → clear()
 * </pre>
 */
public class DataScopeContext {

    private static final ThreadLocal<Deque<DataScopeResult>> SCOPE_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private static final ThreadLocal<Class<?>> ENTITY_CLASS_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<List<String>> DEPT_FIELDS_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<List<String>> USER_FIELDS_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<Object> TENANT_ID_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> SKIP_TENANT_HOLDER = ThreadLocal.withInitial(() -> false);

    private static final ThreadLocal<List<String>> TENANT_FIELDS_HOLDER = new ThreadLocal<>();

    public static void pushScope(DataScopeResult result) {
        SCOPE_STACK.get().push(result);
    }

    public static DataScopeResult popScope() {
        return SCOPE_STACK.get().pop();
    }

    public static DataScopeResult getScope() {
        Deque<DataScopeResult> stack = SCOPE_STACK.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    public static boolean hasScope() {
        return !SCOPE_STACK.get().isEmpty();
    }

    public static int getDepth() {
        return SCOPE_STACK.get().size();
    }

    public static Class<?> getEntityClass() {
        return ENTITY_CLASS_HOLDER.get();
    }

    public static void setEntityClass(Class<?> entityClass) {
        ENTITY_CLASS_HOLDER.set(entityClass);
    }

    public static List<String> getDeptFields() {
        return DEPT_FIELDS_HOLDER.get();
    }

    public static void setDeptFields(List<String> fields) {
        DEPT_FIELDS_HOLDER.set(fields);
    }

    public static List<String> getUserFields() {
        return USER_FIELDS_HOLDER.get();
    }

    public static void setUserFields(List<String> fields) {
        USER_FIELDS_HOLDER.set(fields);
    }

    public static void clear() {
        SCOPE_STACK.remove();
        ENTITY_CLASS_HOLDER.remove();
        DEPT_FIELDS_HOLDER.remove();
        USER_FIELDS_HOLDER.remove();
        TENANT_ID_HOLDER.remove();
        SKIP_TENANT_HOLDER.remove();
        TENANT_FIELDS_HOLDER.remove();
    }

    public static void clearEntityInfo() {
        ENTITY_CLASS_HOLDER.remove();
        DEPT_FIELDS_HOLDER.remove();
        USER_FIELDS_HOLDER.remove();
        TENANT_FIELDS_HOLDER.remove();
    }

    public static Object getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    public static void setTenantId(Object tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    public static boolean isSkipTenant() {
        return Boolean.TRUE.equals(SKIP_TENANT_HOLDER.get());
    }

    public static void setSkipTenant(boolean skip) {
        SKIP_TENANT_HOLDER.set(skip);
    }

    public static List<String> getTenantFields() {
        return TENANT_FIELDS_HOLDER.get();
    }

    public static void setTenantFields(List<String> fields) {
        TENANT_FIELDS_HOLDER.set(fields);
    }
}