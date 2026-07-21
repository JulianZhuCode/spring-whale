package io.github.springwhale.database;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class JpaQueryWrapper<T> {

    private final List<Condition<T>> conditions = new ArrayList<>();
    private final List<SortInfo> sorts = new ArrayList<>();
    private final Class<T> entityClass;

    private JpaQueryWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public static <T> JpaQueryWrapper<T> of(Class<T> entityClass) {
        return new JpaQueryWrapper<>(entityClass);
    }

    public static <T> JpaQueryWrapper<T> of(Class<T> entityClass, Consumer<JpaQueryWrapper<T>> consumer) {
        JpaQueryWrapper<T> wrapper = new JpaQueryWrapper<>(entityClass);
        consumer.accept(wrapper);
        return wrapper;
    }

    private static String getPropertyName(SerializableFunction<?, ?> field) {
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

    private static SerializedLambda getSerializedLambda(SerializableFunction<?, ?> field) throws Exception {
        try {
            java.lang.reflect.Method writeReplaceMethod = field.getClass().getDeclaredMethod("writeReplace");
            writeReplaceMethod.setAccessible(true);
            return (SerializedLambda) writeReplaceMethod.invoke(field);
        } catch (NoSuchMethodException e) {
            try {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(field);
                    byte[] bytes = baos.toByteArray();
                    try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes))) {
                        Object obj = ois.readObject();
                        if (obj instanceof SerializedLambda) {
                            return (SerializedLambda) obj;
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to extract SerializedLambda. " +
                        "This may be due to JDK version restrictions.", ex);
            }
        }
        throw new RuntimeException("Failed to extract SerializedLambda");
    }

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1))) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public JpaQueryWrapper<T> eq(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            conditions.add((root, cb) -> cb.equal(root.get(getPropertyName(field)), value));
        }
        return this;
    }

    public JpaQueryWrapper<T> eq(SerializableFunction<T, ?> field, Object value) {
        return eq(true, field, value);
    }

    public JpaQueryWrapper<T> eq(boolean condition, String field, Object value) {
        if (condition) {
            conditions.add((root, cb) -> cb.equal(root.get(field), value));
        }
        return this;
    }

    public JpaQueryWrapper<T> eq(String field, Object value) {
        return eq(true, field, value);
    }

    public JpaQueryWrapper<T> ne(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            conditions.add((root, cb) -> cb.notEqual(root.get(getPropertyName(field)), value));
        }
        return this;
    }

    public JpaQueryWrapper<T> ne(SerializableFunction<T, ?> field, Object value) {
        return ne(true, field, value);
    }

    public JpaQueryWrapper<T> ne(boolean condition, String field, Object value) {
        if (condition) {
            conditions.add((root, cb) -> cb.notEqual(root.get(field), value));
        }
        return this;
    }

    public JpaQueryWrapper<T> ne(String field, Object value) {
        return ne(true, field, value);
    }

    public JpaQueryWrapper<T> like(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            String pattern = value == null ? null : "%" + value + "%";
            conditions.add(new LikeCondition<>(getPropertyName(field), pattern, false));
        }
        return this;
    }

    public JpaQueryWrapper<T> like(SerializableFunction<T, ?> field, String value) {
        return like(true, field, value);
    }

    public JpaQueryWrapper<T> like(boolean condition, String field, String value) {
        if (condition) {
            String pattern = value == null ? null : "%" + value + "%";
            conditions.add(new LikeCondition<>(field, pattern, false));
        }
        return this;
    }

    public JpaQueryWrapper<T> like(String field, String value) {
        return like(true, field, value);
    }

    public JpaQueryWrapper<T> likeIgnoreCase(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            String pattern = value == null ? null : "%" + value.toLowerCase() + "%";
            conditions.add(new LikeCondition<>(getPropertyName(field), pattern, true));
        }
        return this;
    }

    public JpaQueryWrapper<T> likeIgnoreCase(SerializableFunction<T, ?> field, String value) {
        return likeIgnoreCase(true, field, value);
    }

    public JpaQueryWrapper<T> likeIgnoreCase(boolean condition, String field, String value) {
        if (condition) {
            String pattern = value == null ? null : "%" + value.toLowerCase() + "%";
            conditions.add(new LikeCondition<>(field, pattern, true));
        }
        return this;
    }

    public JpaQueryWrapper<T> likeIgnoreCase(String field, String value) {
        return likeIgnoreCase(true, field, value);
    }

    public JpaQueryWrapper<T> likeLeft(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            String pattern = value == null ? null : "%" + value;
            conditions.add(new LikeCondition<>(getPropertyName(field), pattern, false));
        }
        return this;
    }

    public JpaQueryWrapper<T> likeLeft(SerializableFunction<T, ?> field, String value) {
        return likeLeft(true, field, value);
    }

    public JpaQueryWrapper<T> likeLeft(boolean condition, String field, String value) {
        if (condition) {
            String pattern = value == null ? null : "%" + value;
            conditions.add(new LikeCondition<>(field, pattern, false));
        }
        return this;
    }

    public JpaQueryWrapper<T> likeLeft(String field, String value) {
        return likeLeft(true, field, value);
    }

    public JpaQueryWrapper<T> likeRight(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            String pattern = value == null ? null : value + "%";
            conditions.add(new LikeCondition<>(getPropertyName(field), pattern, false));
        }
        return this;
    }

    public JpaQueryWrapper<T> likeRight(SerializableFunction<T, ?> field, String value) {
        return likeRight(true, field, value);
    }

    public JpaQueryWrapper<T> likeRight(boolean condition, String field, String value) {
        if (condition) {
            String pattern = value == null ? null : value + "%";
            conditions.add(new LikeCondition<>(field, pattern, false));
        }
        return this;
    }

    public JpaQueryWrapper<T> likeRight(String field, String value) {
        return likeRight(true, field, value);
    }

    public JpaQueryWrapper<T> isNull(boolean condition, SerializableFunction<T, ?> field) {
        if (condition) {
            conditions.add((root, cb) -> cb.isNull(root.get(getPropertyName(field))));
        }
        return this;
    }

    public JpaQueryWrapper<T> isNull(SerializableFunction<T, ?> field) {
        return isNull(true, field);
    }

    public JpaQueryWrapper<T> isNull(boolean condition, String field) {
        if (condition) {
            conditions.add((root, cb) -> cb.isNull(root.get(field)));
        }
        return this;
    }

    public JpaQueryWrapper<T> isNull(String field) {
        return isNull(true, field);
    }

    public JpaQueryWrapper<T> isNotNull(boolean condition, SerializableFunction<T, ?> field) {
        if (condition) {
            conditions.add((root, cb) -> cb.isNotNull(root.get(getPropertyName(field))));
        }
        return this;
    }

    public JpaQueryWrapper<T> isNotNull(SerializableFunction<T, ?> field) {
        return isNotNull(true, field);
    }

    public JpaQueryWrapper<T> isNotNull(boolean condition, String field) {
        if (condition) {
            conditions.add((root, cb) -> cb.isNotNull(root.get(field)));
        }
        return this;
    }

    public JpaQueryWrapper<T> isNotNull(String field) {
        return isNotNull(true, field);
    }

    public JpaQueryWrapper<T> in(boolean condition, SerializableFunction<T, ?> field, Object... values) {
        if (condition) {
            conditions.add((root, cb) -> root.get(getPropertyName(field)).in(Arrays.asList(values)));
        }
        return this;
    }

    public JpaQueryWrapper<T> in(SerializableFunction<T, ?> field, Object... values) {
        return in(true, field, values);
    }

    public JpaQueryWrapper<T> in(boolean condition, String field, Object... values) {
        if (condition) {
            conditions.add((root, cb) -> root.get(field).in(Arrays.asList(values)));
        }
        return this;
    }

    public JpaQueryWrapper<T> in(String field, Object... values) {
        return in(true, field, values);
    }

    public JpaQueryWrapper<T> in(boolean condition, SerializableFunction<T, ?> field, List<?> values) {
        if (condition) {
            conditions.add((root, cb) -> root.get(getPropertyName(field)).in(values));
        }
        return this;
    }

    public JpaQueryWrapper<T> in(SerializableFunction<T, ?> field, List<?> values) {
        return in(true, field, values);
    }

    public JpaQueryWrapper<T> in(boolean condition, String field, List<?> values) {
        if (condition) {
            conditions.add((root, cb) -> root.get(field).in(values));
        }
        return this;
    }

    public JpaQueryWrapper<T> in(String field, List<?> values) {
        return in(true, field, values);
    }

    public JpaQueryWrapper<T> between(boolean condition, SerializableFunction<T, ?> field, Object start, Object end) {
        if (condition) {
            conditions.add(new RangeCondition<>(getPropertyName(field), start, end, RangeType.BETWEEN));
        }
        return this;
    }

    public JpaQueryWrapper<T> between(SerializableFunction<T, ?> field, Object start, Object end) {
        return between(true, field, start, end);
    }

    public JpaQueryWrapper<T> between(boolean condition, String field, Object start, Object end) {
        if (condition) {
            conditions.add(new RangeCondition<>(field, start, end, RangeType.BETWEEN));
        }
        return this;
    }

    public JpaQueryWrapper<T> between(String field, Object start, Object end) {
        return between(true, field, start, end);
    }

    public JpaQueryWrapper<T> gt(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(getPropertyName(field), value, null, RangeType.GREATER_THAN));
        }
        return this;
    }

    public JpaQueryWrapper<T> gt(SerializableFunction<T, ?> field, Object value) {
        return gt(true, field, value);
    }

    public JpaQueryWrapper<T> gt(boolean condition, String field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(field, value, null, RangeType.GREATER_THAN));
        }
        return this;
    }

    public JpaQueryWrapper<T> gt(String field, Object value) {
        return gt(true, field, value);
    }

    public JpaQueryWrapper<T> ge(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(getPropertyName(field), value, null, RangeType.GREATER_THAN_OR_EQUAL));
        }
        return this;
    }

    public JpaQueryWrapper<T> ge(SerializableFunction<T, ?> field, Object value) {
        return ge(true, field, value);
    }

    public JpaQueryWrapper<T> ge(boolean condition, String field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(field, value, null, RangeType.GREATER_THAN_OR_EQUAL));
        }
        return this;
    }

    public JpaQueryWrapper<T> ge(String field, Object value) {
        return ge(true, field, value);
    }

    public JpaQueryWrapper<T> lt(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(getPropertyName(field), null, value, RangeType.LESS_THAN));
        }
        return this;
    }

    public JpaQueryWrapper<T> lt(SerializableFunction<T, ?> field, Object value) {
        return lt(true, field, value);
    }

    public JpaQueryWrapper<T> lt(boolean condition, String field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(field, null, value, RangeType.LESS_THAN));
        }
        return this;
    }

    public JpaQueryWrapper<T> lt(String field, Object value) {
        return lt(true, field, value);
    }

    public JpaQueryWrapper<T> le(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(getPropertyName(field), null, value, RangeType.LESS_THAN_OR_EQUAL));
        }
        return this;
    }

    public JpaQueryWrapper<T> le(SerializableFunction<T, ?> field, Object value) {
        return le(true, field, value);
    }

    public JpaQueryWrapper<T> le(boolean condition, String field, Object value) {
        if (condition) {
            conditions.add(new RangeCondition<>(field, null, value, RangeType.LESS_THAN_OR_EQUAL));
        }
        return this;
    }

    public JpaQueryWrapper<T> le(String field, Object value) {
        return le(true, field, value);
    }

    public JpaQueryWrapper<T> or(boolean condition, Consumer<JpaQueryWrapper<T>> consumer) {
        if (condition) {
            JpaQueryWrapper<T> subWrapper = new JpaQueryWrapper<>(entityClass);
            consumer.accept(subWrapper);
            if (!subWrapper.conditions.isEmpty()) {
                conditions.add(new CompositeCondition<>(subWrapper.conditions, true));
            }
        }
        return this;
    }

    public JpaQueryWrapper<T> or(Consumer<JpaQueryWrapper<T>> consumer) {
        return or(true, consumer);
    }

    public JpaQueryWrapper<T> and(boolean condition, Consumer<JpaQueryWrapper<T>> consumer) {
        if (condition) {
            JpaQueryWrapper<T> subWrapper = new JpaQueryWrapper<>(entityClass);
            consumer.accept(subWrapper);
            if (!subWrapper.conditions.isEmpty()) {
                conditions.add(new CompositeCondition<>(subWrapper.conditions, false));
            }
        }
        return this;
    }

    public JpaQueryWrapper<T> and(Consumer<JpaQueryWrapper<T>> consumer) {
        return and(true, consumer);
    }

    public JpaQueryWrapper<T> orderByAsc(SerializableFunction<T, ?> field) {
        sorts.add(new SortInfo(getPropertyName(field), true));
        return this;
    }

    public JpaQueryWrapper<T> orderByAsc(String field) {
        sorts.add(new SortInfo(field, true));
        return this;
    }

    public JpaQueryWrapper<T> orderByDesc(SerializableFunction<T, ?> field) {
        sorts.add(new SortInfo(getPropertyName(field), false));
        return this;
    }

    public JpaQueryWrapper<T> orderByDesc(String field) {
        sorts.add(new SortInfo(field, false));
        return this;
    }

    private List<Predicate> buildPredicates(Root<T> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        for (Condition<T> condition : conditions) {
            Predicate predicate = condition.apply(root, cb);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }
        return predicates;
    }

    private List<Order> buildOrders(Root<T> root, CriteriaBuilder cb) {
        List<Order> orders = new ArrayList<>();
        for (SortInfo sort : sorts) {
            Path<?> path = root.get(sort.fieldName);
            if (sort.asc) {
                orders.add(cb.asc(path));
            } else {
                orders.add(cb.desc(path));
            }
        }
        return orders;
    }

    public Specification<T> build() {
        return (root, query, cb) -> {
            List<Predicate> predicates = buildPredicates(root, cb);
            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            List<Order> orders = buildOrders(root, cb);
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }
            return query.getRestriction();
        };
    }

    public Specification<T> buildSpec() {
        return (root, query, cb) -> {
            List<Predicate> predicates = buildPredicates(root, cb);
            if (!predicates.isEmpty()) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
            return cb.conjunction();
        };
    }

    private enum RangeType {
        BETWEEN, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL
    }

    @FunctionalInterface
    private interface Condition<T> {
        Predicate apply(Root<T> root, CriteriaBuilder cb);
    }

    private record SortInfo(String fieldName, boolean asc) {
    }

    private record LikeCondition<T>(String fieldName, String pattern, boolean ignoreCase) implements Condition<T> {

        @Override
            @SuppressWarnings("unchecked")
            public Predicate apply(Root<T> root, CriteriaBuilder cb) {
                if (ignoreCase) {
                    return cb.like(cb.lower(root.get(fieldName)), pattern);
                }
                return cb.like(root.get(fieldName), pattern);
            }
        }

    private record RangeCondition<T>(String fieldName, Object start, Object end,
                                     RangeType type) implements Condition<T> {

        @Override
            @SuppressWarnings("unchecked")
            public Predicate apply(Root<T> root, CriteriaBuilder cb) {
                switch (type) {
                    case BETWEEN:
                        return cb.between(root.get(fieldName), (Comparable) start, (Comparable) end);
                    case GREATER_THAN:
                        return cb.greaterThan(root.get(fieldName), (Comparable) start);
                    case GREATER_THAN_OR_EQUAL:
                        return cb.greaterThanOrEqualTo(root.get(fieldName), (Comparable) start);
                    case LESS_THAN:
                        return cb.lessThan(root.get(fieldName), (Comparable) end);
                    case LESS_THAN_OR_EQUAL:
                        return cb.lessThanOrEqualTo(root.get(fieldName), (Comparable) end);
                    default:
                        return null;
                }
            }
        }

    private record CompositeCondition<T>(List<Condition<T>> conditions, boolean isOr) implements Condition<T> {

        @Override
            public Predicate apply(Root<T> root, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                for (Condition<T> condition : conditions) {
                    Predicate predicate = condition.apply(root, cb);
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