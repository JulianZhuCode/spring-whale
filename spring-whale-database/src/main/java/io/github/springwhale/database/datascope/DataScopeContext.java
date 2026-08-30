package io.github.springwhale.database.datascope;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class DataScopeContext {

    private static final ThreadLocal<Deque<DataScopeResult>> SCOPE_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private static final ThreadLocal<Class<?>> ENTITY_CLASS_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<List<String>> DEPT_FIELDS_HOLDER = new ThreadLocal<>();

    private static final ThreadLocal<List<String>> USER_FIELDS_HOLDER = new ThreadLocal<>();

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
    }

    public static void clearEntityInfo() {
        ENTITY_CLASS_HOLDER.remove();
        DEPT_FIELDS_HOLDER.remove();
        USER_FIELDS_HOLDER.remove();
    }
}