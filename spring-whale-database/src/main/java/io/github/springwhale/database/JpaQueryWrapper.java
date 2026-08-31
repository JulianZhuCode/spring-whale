package io.github.springwhale.database;

import io.github.springwhale.database.criteria.*;

import java.util.function.Consumer;

/**
 * JPA dynamic query wrapper providing MyBatis-Plus-style chainable conditions
 * on top of JPA {@link jakarta.persistence.criteria.CriteriaBuilder}.
 *
 * <p>Supports {@code eq}, {@code ne}, {@code gt}, {@code ge}, {@code lt}, {@code le},
 * {@code in}, {@code notIn}, {@code between}, {@code isNull}, {@code isNotNull},
 * {@code like}, {@code notLike}, {@code likeLeft}, {@code likeRight},
 * {@code orderByAsc}, {@code orderByDesc}, {@code groupBy}, {@code nested},
 * {@code raw}, and aggregation functions.</p>
 *
 * <pre>{@code
 * List<User> users = new JpaQueryWrapper<>(entityManager, User.class)
 *     .eq(User::getStatus, 1)
 *     .like(User::getName, "zhang")
 *     .orderByDesc(User::getCreateTime)
 *     .list();
 * }</pre>
 */
public class JpaQueryWrapper<T> extends AbstractWrapper<T, JpaQueryWrapper<T>>
        implements Compare<T, JpaQueryWrapper<T>>,
        Like<T, JpaQueryWrapper<T>>,
        Func<T, JpaQueryWrapper<T>>,
        Nested<T, JpaQueryWrapper<T>>,
        OrderBy<T, JpaQueryWrapper<T>>,
        Raw<T, JpaQueryWrapper<T>> {

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
    public JpaQueryWrapper<T> self() {
        return this;
    }

    @Override
    public JpaQueryWrapper<T> createSubWrapper() {
        return new JpaQueryWrapper<>(entityClass);
    }
}