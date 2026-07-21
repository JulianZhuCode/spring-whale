package io.github.springwhale.database.criteria;

import java.util.function.Consumer;

public interface Nested<T, Children extends AbstractWrapper<T, Children>> {

    default Children or(boolean condition, Consumer<Children> consumer) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            Children subWrapper = wrapper.createSubWrapper();
            consumer.accept(subWrapper);
            if (!subWrapper.conditions.isEmpty()) {
                wrapper.addCondition(new AbstractWrapper.CompositeCondition<>(subWrapper.conditions, true));
            }
        }
        return (Children) this;
    }

    default Children or(Consumer<Children> consumer) {
        return or(true, consumer);
    }

    default Children and(boolean condition, Consumer<Children> consumer) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            Children subWrapper = wrapper.createSubWrapper();
            consumer.accept(subWrapper);
            if (!subWrapper.conditions.isEmpty()) {
                wrapper.addCondition(new AbstractWrapper.CompositeCondition<>(subWrapper.conditions, false));
            }
        }
        return (Children) this;
    }

    default Children and(Consumer<Children> consumer) {
        return and(true, consumer);
    }


}