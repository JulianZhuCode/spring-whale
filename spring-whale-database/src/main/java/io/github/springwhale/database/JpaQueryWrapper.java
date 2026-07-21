package io.github.springwhale.database;

import io.github.springwhale.database.criteria.*;

import java.util.function.Consumer;

public class JpaQueryWrapper<T> extends AbstractWrapper<T, JpaQueryWrapper<T>>
        implements Compare<T, JpaQueryWrapper<T>>,
        Like<T, JpaQueryWrapper<T>>,
        Func<T, JpaQueryWrapper<T>>,
        Nested<T, JpaQueryWrapper<T>>,
        OrderBy<T, JpaQueryWrapper<T>> {

    private JpaQueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    public static <T> JpaQueryWrapper<T> of(Class<T> entityClass) {
        return new JpaQueryWrapper<>(entityClass);
    }

    public static <T> JpaQueryWrapper<T> of(Class<T> entityClass, Consumer<JpaQueryWrapper<T>> consumer) {
        JpaQueryWrapper<T> wrapper = new JpaQueryWrapper<>(entityClass);
        consumer.accept(wrapper);
        return wrapper;
    }

    @Override
    protected JpaQueryWrapper<T> self() {
        return this;
    }

    @Override
    public JpaQueryWrapper<T> createSubWrapper() {
        return new JpaQueryWrapper<>(entityClass);
    }
}