package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;

public interface Compare<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children eq(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.equal(root.get(AbstractWrapper.getPropertyName(field)), value));
        }
        return getWrapper().self();
    }

    default Children eq(SerializableFunction<T, ?> field, Object value) {
        return eq(true, field, value);
    }

    default Children eq(boolean condition, String field, Object value) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.equal(root.get(field), value));
        }
        return getWrapper().self();
    }

    default Children eq(String field, Object value) {
        return eq(true, field, value);
    }

    default Children ne(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.notEqual(root.get(AbstractWrapper.getPropertyName(field)), value));
        }
        return getWrapper().self();
    }

    default Children ne(SerializableFunction<T, ?> field, Object value) {
        return ne(true, field, value);
    }

    default Children ne(boolean condition, String field, Object value) {
        if (condition) {
            getWrapper().addCondition((root, cb) -> cb.notEqual(root.get(field), value));
        }
        return getWrapper().self();
    }

    default Children ne(String field, Object value) {
        return ne(true, field, value);
    }

    default Children gt(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    AbstractWrapper.getPropertyName(field), value, null, AbstractWrapper.RangeType.GREATER_THAN));
        }
        return getWrapper().self();
    }

    default Children gt(SerializableFunction<T, ?> field, Object value) {
        return gt(true, field, value);
    }

    default Children gt(boolean condition, String field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    field, value, null, AbstractWrapper.RangeType.GREATER_THAN));
        }
        return getWrapper().self();
    }

    default Children gt(String field, Object value) {
        return gt(true, field, value);
    }

    default Children ge(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    AbstractWrapper.getPropertyName(field), value, null, AbstractWrapper.RangeType.GREATER_THAN_OR_EQUAL));
        }
        return getWrapper().self();
    }

    default Children ge(SerializableFunction<T, ?> field, Object value) {
        return ge(true, field, value);
    }

    default Children ge(boolean condition, String field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    field, value, null, AbstractWrapper.RangeType.GREATER_THAN_OR_EQUAL));
        }
        return getWrapper().self();
    }

    default Children ge(String field, Object value) {
        return ge(true, field, value);
    }

    default Children lt(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    AbstractWrapper.getPropertyName(field), null, value, AbstractWrapper.RangeType.LESS_THAN));
        }
        return getWrapper().self();
    }

    default Children lt(SerializableFunction<T, ?> field, Object value) {
        return lt(true, field, value);
    }

    default Children lt(boolean condition, String field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    field, null, value, AbstractWrapper.RangeType.LESS_THAN));
        }
        return getWrapper().self();
    }

    default Children lt(String field, Object value) {
        return lt(true, field, value);
    }

    default Children le(boolean condition, SerializableFunction<T, ?> field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    AbstractWrapper.getPropertyName(field), null, value, AbstractWrapper.RangeType.LESS_THAN_OR_EQUAL));
        }
        return getWrapper().self();
    }

    default Children le(SerializableFunction<T, ?> field, Object value) {
        return le(true, field, value);
    }

    default Children le(boolean condition, String field, Object value) {
        if (condition) {
            getWrapper().addCondition(new AbstractWrapper.RangeCondition<>(
                    field, null, value, AbstractWrapper.RangeType.LESS_THAN_OR_EQUAL));
        }
        return getWrapper().self();
    }

    default Children le(String field, Object value) {
        return le(true, field, value);
    }
}