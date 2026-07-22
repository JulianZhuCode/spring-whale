package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;

public interface Like<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    default Children like(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("like()", AbstractWrapper.getPropertyName(field), value);
            String pattern = "%" + value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false));
        }
        return getWrapper().self();
    }

    default Children like(SerializableFunction<T, ?> field, String value) {
        return like(true, field, value);
    }

    default Children like(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("like()", field, value);
            String pattern = "%" + value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false));
        }
        return getWrapper().self();
    }

    default Children like(String field, String value) {
        return like(true, field, value);
    }

    default Children likeIgnoreCase(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("likeIgnoreCase()", AbstractWrapper.getPropertyName(field), value);
            String pattern = "%" + value.toLowerCase() + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, true));
        }
        return getWrapper().self();
    }

    default Children likeIgnoreCase(SerializableFunction<T, ?> field, String value) {
        return likeIgnoreCase(true, field, value);
    }

    default Children likeIgnoreCase(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("likeIgnoreCase()", field, value);
            String pattern = "%" + value.toLowerCase() + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, true));
        }
        return getWrapper().self();
    }

    default Children likeIgnoreCase(String field, String value) {
        return likeIgnoreCase(true, field, value);
    }

    default Children likeLeft(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("likeLeft()", AbstractWrapper.getPropertyName(field), value);
            String pattern = "%" + value;
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false));
        }
        return getWrapper().self();
    }

    default Children likeLeft(SerializableFunction<T, ?> field, String value) {
        return likeLeft(true, field, value);
    }

    default Children likeLeft(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("likeLeft()", field, value);
            String pattern = "%" + value;
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false));
        }
        return getWrapper().self();
    }

    default Children likeLeft(String field, String value) {
        return likeLeft(true, field, value);
    }

    default Children likeRight(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("likeRight()", AbstractWrapper.getPropertyName(field), value);
            String pattern = value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false));
        }
        return getWrapper().self();
    }

    default Children likeRight(SerializableFunction<T, ?> field, String value) {
        return likeRight(true, field, value);
    }

    default Children likeRight(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("likeRight()", field, value);
            String pattern = value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false));
        }
        return getWrapper().self();
    }

    default Children likeRight(String field, String value) {
        return likeRight(true, field, value);
    }

    private static void ensureValueNotNull(String methodName, String fieldName, String value) {
        if (value == null) {
            throw new IllegalArgumentException(methodName + " value cannot be null when condition is true. " +
                    "Use condition=false to skip this condition, or provide a valid value. " +
                    "Field: " + fieldName);
        }
    }
}