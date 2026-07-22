package io.github.springwhale.database.criteria;

import java.util.function.Consumer;

public interface Nested<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children or(boolean condition, Consumer<Children> consumer) {
        if (condition) {
            Children subWrapper = getWrapper().createSubWrapper();
            consumer.accept(subWrapper);
            if (!subWrapper.getWrapper().conditions.isEmpty()) {
                getWrapper().addCondition(new AbstractWrapper.CompositeCondition<>(subWrapper.getWrapper().conditions, true));
            }
        }
        return getWrapper().self();
    }

    default Children or(Consumer<Children> consumer) {
        return or(true, consumer);
    }

    default Children and(boolean condition, Consumer<Children> consumer) {
        if (condition) {
            Children subWrapper = getWrapper().createSubWrapper();
            consumer.accept(subWrapper);
            if (!subWrapper.getWrapper().conditions.isEmpty()) {
                getWrapper().addCondition(new AbstractWrapper.CompositeCondition<>(subWrapper.getWrapper().conditions, false));
            }
        }
        return getWrapper().self();
    }

    default Children and(Consumer<Children> consumer) {
        return and(true, consumer);
    }
}