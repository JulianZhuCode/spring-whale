package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Map;

public interface GroupBy<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children groupBy(String... fields) {
        if (fields != null) {
            for (String field : fields) {
                getWrapper().addGroupBy(field);
            }
        }
        return getWrapper().self();
    }

    @SuppressWarnings("unchecked")
    default Children groupBy(SerializableFunction<T, ?>... fields) {
        if (fields != null) {
            for (SerializableFunction<T, ?> field : fields) {
                getWrapper().addGroupBy(AbstractWrapper.getPropertyName(field));
            }
        }
        return getWrapper().self();
    }

    default Children having(boolean condition, HavingPredicate<T> havingFn) {
        if (condition) {
            getWrapper().addHaving((root, cb, joinMap) -> havingFn.apply(root, cb));
        }
        return getWrapper().self();
    }

    default Children having(HavingPredicate<T> havingFn) {
        return having(true, havingFn);
    }

    /**
     * Adds a HAVING condition that can access the join map, allowing conditions
     * on joined (associations) fields, e.g. {@code HAVING COUNT(department.name) > 1}.
     *
     * <p>The {@code joinMap} is shared with the WHERE/GROUP BY resolution: joins created
     * while resolving nested fields (e.g. {@code groupBy("department.name")}) are
     * available here, and joins created inside the predicate are visible to the query.</p>
     *
     * @param condition guard flag; the predicate is only added when {@code true}
     * @param havingFn  the predicate with access to the shared join map
     */
    default Children having(boolean condition, HavingJoinPredicate<T> havingFn) {
        if (condition) {
            getWrapper().addHaving(havingFn::apply);
        }
        return getWrapper().self();
    }

    default Children having(HavingJoinPredicate<T> havingFn) {
        return having(true, havingFn);
    }

    @FunctionalInterface
    interface HavingPredicate<T> {
        Predicate apply(Root<T> root, CriteriaBuilder cb);
    }

    /**
     * HAVING predicate with access to the shared join map, for conditions
     * on joined fields.
     */
    @FunctionalInterface
    interface HavingJoinPredicate<T> {
        Predicate apply(Root<T> root, CriteriaBuilder cb, Map<String, Join<?, ?>> joinMap);
    }
}
