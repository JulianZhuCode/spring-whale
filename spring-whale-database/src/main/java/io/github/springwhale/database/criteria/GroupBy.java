package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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

    @FunctionalInterface
    interface HavingPredicate<T> {
        Predicate apply(Root<T> root, CriteriaBuilder cb);
    }
}