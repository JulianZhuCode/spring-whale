package io.github.springwhale.database.criteria;

public interface Distinctable<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children distinct() {
        getWrapper().setDistinct(true);
        return getWrapper().self();
    }
}