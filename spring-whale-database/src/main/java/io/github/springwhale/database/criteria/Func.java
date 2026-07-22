package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;

import java.util.Arrays;
import java.util.List;

public interface Func<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children isNull(boolean condition, SerializableFunction<T, ?> field) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.isNull(root.get(AbstractWrapper.getPropertyName(field))));
        }
        return getWrapper().self();
    }

    default Children isNull(SerializableFunction<T, ?> field) {
        return isNull(true, field);
    }

    default Children isNull(boolean condition, String field) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.isNull(root.get(field)));
        }
        return getWrapper().self();
    }

    default Children isNull(String field) {
        return isNull(true, field);
    }

    default Children isNotNull(boolean condition, SerializableFunction<T, ?> field) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.isNotNull(root.get(AbstractWrapper.getPropertyName(field))));
        }
        return getWrapper().self();
    }

    default Children isNotNull(SerializableFunction<T, ?> field) {
        return isNotNull(true, field);
    }

    default Children isNotNull(boolean condition, String field) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.isNotNull(root.get(field)));
        }
        return getWrapper().self();
    }

    default Children isNotNull(String field) {
        return isNotNull(true, field);
    }

    default Children in(boolean condition, SerializableFunction<T, ?> field, Object... values) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> root.get(AbstractWrapper.getPropertyName(field)).in(Arrays.asList(values)));
        }
        return getWrapper().self();
    }

    default Children in(SerializableFunction<T, ?> field, Object... values) {
        return in(true, field, values);
    }

    default Children in(boolean condition, String field, Object... values) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> root.get(field).in(Arrays.asList(values)));
        }
        return getWrapper().self();
    }

    default Children in(String field, Object... values) {
        return in(true, field, values);
    }

    default Children in(boolean condition, SerializableFunction<T, ?> field, List<?> values) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> root.get(AbstractWrapper.getPropertyName(field)).in(values));
        }
        return getWrapper().self();
    }

    default Children in(SerializableFunction<T, ?> field, List<?> values) {
        return in(true, field, values);
    }

    default Children in(boolean condition, String field, List<?> values) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> root.get(field).in(values));
        }
        return getWrapper().self();
    }

    default Children in(String field, List<?> values) {
        return in(true, field, values);
    }

    default Children between(boolean condition, SerializableFunction<T, ?> field, Object start, Object end) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    AbstractWrapper.getPropertyName(field), start, end, AbstractWrapper.RangeType.BETWEEN));
        }
        return getWrapper().self();
    }

    default Children between(SerializableFunction<T, ?> field, Object start, Object end) {
        return between(true, field, start, end);
    }

    default Children between(boolean condition, String field, Object start, Object end) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    field, start, end, AbstractWrapper.RangeType.BETWEEN));
        }
        return getWrapper().self();
    }

    default Children between(String field, Object start, Object end) {
        return between(true, field, start, end);
    }
}