package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;

public interface OrderBy<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children orderByAsc(SerializableFunction<T, ?> field) {
        getWrapper().addSort(AbstractWrapper.getPropertyName(field), true);
        return getWrapper().self();
    }

    default Children orderByAsc(String field) {
        getWrapper().addSort(field, true);
        return getWrapper().self();
    }

    default Children orderByDesc(SerializableFunction<T, ?> field) {
        getWrapper().addSort(AbstractWrapper.getPropertyName(field), false);
        return getWrapper().self();
    }

    default Children orderByDesc(String field) {
        getWrapper().addSort(field, false);
        return getWrapper().self();
    }
}