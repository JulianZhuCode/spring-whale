package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.SerializedLambda;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Base class for JPA Criteria API wrappers. Manages the internal condition list,
 * sort list, and provides the {@link #buildSpec()} method that converts
 * accumulated conditions into a Spring Data {@link Specification}.
 *
 * <p>Supports groupBy, having, distinct, top-level OR, and nested conditions.</p>
 *
 * @param <T>        entity type
 * @param <Children> self-type for fluent API chaining
 */
public abstract class AbstractWrapper<T, Children extends AbstractWrapper<T, Children>> implements Wrapper<T, Children> {

    protected final List<Condition<T>> conditions = new ArrayList<>();
    protected final List<SortInfo> sorts = new ArrayList<>();
    protected final List<String> groupByFields = new ArrayList<>();
    protected final List<HavingCondition<T>> havingConditions = new ArrayList<>();
    protected final Class<T> entityClass;
    protected boolean topLevelOr;
    protected boolean isDistinct;

    protected AbstractWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected static String getPropertyName(SerializableFunction<?, ?> field) {
        try {
            SerializedLambda lambda = getSerializedLambda(field);
            String methodName = lambda.getImplMethodName();
            if (methodName.startsWith("get")) {
                return decapitalize(methodName.substring(3));
            } else if (methodName.startsWith("is")) {
                return decapitalize(methodName.substring(2));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract property name from lambda. " +
                    "This may be due to JDK version restrictions. " +
                    "Please use the String field name version instead: eq(\"fieldName\", value)", e);
        }
        throw new IllegalArgumentException("Cannot extract property name from lambda. " +
                "Please use the String field name version instead: eq(\"fieldName\", value)");
    }

    protected static SerializedLambda getSerializedLambda(SerializableFunction<?, ?> field) throws Exception {
        try {
            java.lang.reflect.Method writeReplaceMethod = field.getClass().getDeclaredMethod("writeReplace");
            writeReplaceMethod.setAccessible(true);
            return (SerializedLambda) writeReplaceMethod.invoke(field);
        } catch (NoSuchMethodException e) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(field);
                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(baos.toByteArray()))) {
                    Object obj = ois.readObject();
                    if (obj instanceof SerializedLambda) {
                        return (SerializedLambda) obj;
                    }
                }
            }
            throw new RuntimeException("Failed to extract SerializedLambda. " +
                    "This may be due to JDK version restrictions.");
        }
    }

    protected static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1))) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public abstract Children self();

    protected abstract Children createSubWrapper();

    protected void addCondition(Condition<T> condition) {
        conditions.add(condition);
    }

    protected void addSort(String fieldName, boolean asc) {
        sorts.add(new SortInfo(fieldName, asc));
    }

    protected void addGroupBy(String fieldName) {
        groupByFields.add(fieldName);
    }

    protected void addHaving(HavingCondition<T> condition) {
        havingConditions.add(condition);
    }

    protected void setTopLevelOr(boolean topLevelOr) {
        this.topLevelOr = topLevelOr;
    }

    protected void setDistinct(boolean distinct) {
        this.isDistinct = distinct;
    }

    protected <X> Path<X> resolvePath(Root<T> root, Map<String, Join<?, ?>> joinMap, String fieldName) {
        int dotIndex = fieldName.indexOf('.');
        if (dotIndex > 0) {
            String joinAttr = fieldName.substring(0, dotIndex);
            String nestedField = fieldName.substring(dotIndex + 1);
            Join<?, ?> join = joinMap.get(joinAttr);
            if (join != null) {
                return join.get(nestedField);
            }
            Join<?, ?> newJoin = root.join(joinAttr, JoinType.LEFT);
            joinMap.put(joinAttr, newJoin);
            return newJoin.get(nestedField);
        }
        return root.get(fieldName);
    }

    protected List<Predicate> buildPredicates(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap) {
        List<Predicate> predicates = new ArrayList<>();
        for (Condition<T> condition : conditions) {
            Predicate predicate = condition.apply(root, cb, joinMap);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }
        return predicates;
    }

    protected List<Order> buildOrders(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap) {
        List<Order> orders = new ArrayList<>();
        for (SortInfo sort : sorts) {
            Path<?> path = resolvePath(root, joinMap, sort.fieldName);
            if (sort.asc) {
                orders.add(cb.asc(path));
            } else {
                orders.add(cb.desc(path));
            }
        }
        return orders;
    }

    protected void applyGroupBy(CriteriaQuery<?> query, Root<T> root, Map<String, Join<?, ?>> joinMap) {
        if (!groupByFields.isEmpty()) {
            List<Expression<?>> expressions = new ArrayList<>();
            for (String field : groupByFields) {
                expressions.add(resolvePath(root, joinMap, field));
            }
            query.groupBy(expressions);
        }
    }

    protected void applyHaving(CriteriaQuery<?> query, Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap) {
        if (!havingConditions.isEmpty()) {
            List<Predicate> havingPredicates = new ArrayList<>();
            for (HavingCondition<T> condition : havingConditions) {
                Predicate predicate = condition.apply(root, cb, joinMap);
                if (predicate != null) {
                    havingPredicates.add(predicate);
                }
            }
            if (!havingPredicates.isEmpty()) {
                query.having(cb.and(havingPredicates.toArray(new Predicate[0])));
            }
        }
    }

    public Specification<T> build() {
        return (root, query, cb) -> {
            Map<String, Join<?, ?>> joinMap = new LinkedHashMap<>();
            List<Predicate> predicates = buildPredicates(root, cb, joinMap);
            if (!predicates.isEmpty()) {
                Predicate combined = topLevelOr
                        ? cb.or(predicates.toArray(new Predicate[0]))
                        : cb.and(predicates.toArray(new Predicate[0]));
                query.where(combined);
            }
            List<Order> orders = buildOrders(root, cb, joinMap);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }
            applyGroupBy(query, root, joinMap);
            applyHaving(query, root, cb, joinMap);
            if (isDistinct) {
                query.distinct(true);
            }
            return query.getRestriction();
        };
    }

    public Specification<T> buildSpec() {
        return (root, query, cb) -> {
            Map<String, Join<?, ?>> joinMap = new LinkedHashMap<>();
            List<Predicate> predicates = buildPredicates(root, cb, joinMap);
            List<Order> orders = buildOrders(root, cb, joinMap);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }
            applyGroupBy(query, root, joinMap);
            applyHaving(query, root, cb, joinMap);
            if (isDistinct) {
                query.distinct(true);
            }
            if (!predicates.isEmpty()) {
                return topLevelOr
                        ? cb.or(predicates.toArray(new Predicate[0]))
                        : cb.and(predicates.toArray(new Predicate[0]));
            }
            return cb.conjunction();
        };
    }

    protected enum RangeType {
        BETWEEN, NOT_BETWEEN, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL
    }

    @FunctionalInterface
    protected interface Condition<T> {
        Predicate apply(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap);

        default Predicate apply(Root<T> root, CriteriaBuilder cb) {
            return apply(root, cb, Collections.emptyMap());
        }
    }

    @FunctionalInterface
    protected interface HavingCondition<T> {
        Predicate apply(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap);
    }

    protected record SortInfo(String fieldName, boolean asc) {
    }

    protected record LikeCondition<T>(String fieldName, String pattern, boolean ignoreCase, boolean not) implements Condition<T> {

        @Override
        public Predicate apply(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap) {
            Path<String> path = resolvePathForCondition(root, joinMap, fieldName);
            Expression<String> expr = ignoreCase ? cb.lower(path) : path;
            Predicate like = cb.like(expr, pattern);
            return not ? cb.not(like) : like;
        }

        @SuppressWarnings("unchecked")
        private static <T, X> Path<X> resolvePathForCondition(Root<T> root, Map<String, Join<?, ?>> joinMap, String fieldName) {
            int dotIndex = fieldName.indexOf('.');
            if (dotIndex > 0) {
                String joinAttr = fieldName.substring(0, dotIndex);
                String nestedField = fieldName.substring(dotIndex + 1);
                Join<?, ?> join = joinMap.get(joinAttr);
                if (join != null) {
                    return (Path<X>) join.get(nestedField);
                }
            }
            return root.get(fieldName);
        }
    }

    protected record RangeCondition<T>(String fieldName, Object start, Object end,
                                       RangeType type) implements Condition<T> {

        @Override
        @SuppressWarnings("unchecked")
        public Predicate apply(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap) {
            Path<?> path = resolvePathForCondition(root, joinMap, fieldName);
            Predicate predicate = switch (type) {
                case BETWEEN -> cb.between((Path<Comparable>) path, (Comparable) start, (Comparable) end);
                case NOT_BETWEEN -> cb.not(cb.between((Path<Comparable>) path, (Comparable) start, (Comparable) end));
                case GREATER_THAN -> cb.greaterThan((Path<Comparable>) path, (Comparable) start);
                case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) start);
                case LESS_THAN -> cb.lessThan((Path<Comparable>) path, (Comparable) end);
                case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) end);
            };
            return predicate;
        }

        @SuppressWarnings("unchecked")
        private static <T, X> Path<X> resolvePathForCondition(Root<T> root, Map<String, Join<?, ?>> joinMap, String fieldName) {
            int dotIndex = fieldName.indexOf('.');
            if (dotIndex > 0) {
                String joinAttr = fieldName.substring(0, dotIndex);
                String nestedField = fieldName.substring(dotIndex + 1);
                Join<?, ?> join = joinMap.get(joinAttr);
                if (join != null) {
                    return (Path<X>) join.get(nestedField);
                }
            }
            return root.get(fieldName);
        }
    }

    protected record CompositeCondition<T>(List<Condition<T>> conditions, boolean isOr) implements Condition<T> {

        @Override
        public Predicate apply(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap) {
            List<Predicate> predicates = new ArrayList<>();
            for (Condition<T> condition : conditions) {
                Predicate predicate = condition.apply(root, cb, joinMap);
                if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (predicates.isEmpty()) {
                return null;
            }
            if (isOr) {
                return cb.or(predicates.toArray(new Predicate[0]));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }
    }
}