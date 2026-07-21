package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;

public interface OrderBy<T, Children extends AbstractWrapper<T, Children>> {

    default Children orderByAsc(SerializableFunction<T, ?> field) {
        AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
        wrapper.addSort(AbstractWrapper.getPropertyName(field), true);
        return (Children) this;
    }

    default Children orderByAsc(String field) {
        AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
        wrapper.addSort(field, true);
        return (Children) this;
    }

    default Children orderByDesc(SerializableFunction<T, ?> field) {
        AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
        wrapper.addSort(AbstractWrapper.getPropertyName(field), false);
        return (Children) this;
    }

    default Children orderByDesc(String field) {
        AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
        wrapper.addSort(field, false);
        return (Children) this;
    }
}