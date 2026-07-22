package io.github.springwhale.database.criteria;

public interface Wrapper<T, Children extends AbstractWrapper<T, Children>> {

    default AbstractWrapper<T, Children> getWrapper() {
        return (AbstractWrapper<T, Children>) this;
    }
}