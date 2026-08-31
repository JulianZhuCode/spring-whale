package io.github.springwhale.database;

import io.github.springwhale.database.criteria.*;

import java.util.function.Consumer;

/**
 * JPA dynamic query wrapper providing MyBatis-Plus-style chainable conditions
 * on top of JPA {@link jakarta.persistence.criteria.CriteriaBuilder},
 * ultimately producing a Spring Data {@link org.springframework.data.jpa.domain.Specification}.
 *
 * <p>Supports {@code eq}, {@code ne}, {@code eqIgnoreCase}, {@code neIgnoreCase},
 * {@code gt}, {@code ge}, {@code lt}, {@code le},
 * {@code in}, {@code notIn}, {@code between}, {@code notBetween},
 * {@code isNull}, {@code isNotNull},
 * {@code like}, {@code notLike}, {@code likeIgnoreCase}, {@code notLikeIgnoreCase},
 * {@code likeLeft}, {@code likeRight}, {@code notLikeLeft}, {@code notLikeRight},
 * {@code orderByAsc}, {@code orderByDesc}, {@code groupBy}, {@code having},
 * {@code distinct},
 * {@code or()} (top-level), {@code or(Consumer)}, {@code and(Consumer)},
 * and {@code raw} conditions.</p>
 *
 * <p>Usages:</p>
 * <pre>{@code
 * Specification<User> spec = JpaQueryWrapper.of(User.class)
 *     .eq(User::getStatus, 1)
 *     .like(User::getName, "zhang")
 *     .orderByDesc(User::getCreateTime)
 *     .buildSpec();
 * userRepository.findAll(spec, pageable);
 * }</pre>
 */
public class JpaQueryWrapper<T> extends AbstractWrapper<T, JpaQueryWrapper<T>>
        implements Compare<T, JpaQueryWrapper<T>>,
        Like<T, JpaQueryWrapper<T>>,
        Func<T, JpaQueryWrapper<T>>,
        Nested<T, JpaQueryWrapper<T>>,
        OrderBy<T, JpaQueryWrapper<T>>,
        Raw<T, JpaQueryWrapper<T>>,
        GroupBy<T, JpaQueryWrapper<T>>,
        Distinctable<T, JpaQueryWrapper<T>> {

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