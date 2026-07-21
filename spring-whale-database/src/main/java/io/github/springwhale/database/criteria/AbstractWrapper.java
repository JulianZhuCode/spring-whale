package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWrapper<T, Children extends AbstractWrapper<T, Children>> {

    protected final List<Condition<T>> conditions = new ArrayList<>();
    protected final List<SortInfo> sorts = new ArrayList<>();
    protected final Class<T> entityClass;

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

    protected static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1))) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    protected abstract Children self();

    protected abstract Children createSubWrapper();

    protected void addCondition(Condition<T> condition) {
        conditions.add(condition);
    }

    protected void addSort(String fieldName, boolean asc) {
        sorts.add(new SortInfo(fieldName, asc));
    }

    protected List<Predicate> buildPredicates(Root<T> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        for (Condition<T> condition : conditions) {
            Predicate predicate = condition.apply(root, cb);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }
        return predicates;
    }

    protected List<Order> buildOrders(Root<T> root, CriteriaBuilder cb) {
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

    protected enum RangeType {
        BETWEEN, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL
    }

    @FunctionalInterface
    protected interface Condition<T> {
        Predicate apply(Root<T> root, CriteriaBuilder cb);
    }

    protected record SortInfo(String fieldName, boolean asc) {
    }

    protected record LikeCondition<T>(String fieldName, String pattern, boolean ignoreCase) implements Condition<T> {

        @Override
        @SuppressWarnings("unchecked")
        public Predicate apply(Root<T> root, CriteriaBuilder cb) {
            if (ignoreCase) {
                return cb.like(cb.lower(root.get(fieldName)), pattern);
            }
            return cb.like(root.get(fieldName), pattern);
        }
    }

    protected record RangeCondition<T>(String fieldName, Object start, Object end,
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

    protected record CompositeCondition<T>(List<Condition<T>> conditions, boolean isOr) implements Condition<T> {

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