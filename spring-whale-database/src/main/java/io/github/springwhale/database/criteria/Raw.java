package io.github.springwhale.database.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.function.BiFunction;

public interface Raw<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children raw(boolean condition, BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> predicateBuilder.apply(root, cb));
        }
        return getWrapper().self();
    }

    default Children raw(BiFunction<Root<T>, CriteriaBuilder, Predicate> predicateBuilder) {
        return raw(true, predicateBuilder);
    }
}