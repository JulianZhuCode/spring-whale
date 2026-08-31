package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;

public interface Like<T, Children extends AbstractWrapper<T, Children>> extends Wrapper<T, Children> {

    private static void ensureValueNotNull(String methodName, String fieldName, String value) {
        if (value == null) {
            throw new IllegalArgumentException(methodName + " value cannot be null when condition is true. " +
                    "Use condition=false to skip this condition, or provide a valid value. " +
                    "Field: " + fieldName);
        }
    }

    default Children like(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("like()", AbstractWrapper.getPropertyName(field), value);
            String pattern = "%" + value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false, false));
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
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false, false));
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
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, true, false));
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
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, true, false));
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
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false, false));
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
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false, false));
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
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false, false));
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
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false, false));
        }
        return getWrapper().self();
    }

    default Children likeRight(String field, String value) {
        return likeRight(true, field, value);
    }

    default Children notLike(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("notLike()", AbstractWrapper.getPropertyName(field), value);
            String pattern = "%" + value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false, true));
        }
        return getWrapper().self();
    }

    default Children notLike(SerializableFunction<T, ?> field, String value) {
        return notLike(true, field, value);
    }

    default Children notLike(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("notLike()", field, value);
            String pattern = "%" + value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false, true));
        }
        return getWrapper().self();
    }

    default Children notLike(String field, String value) {
        return notLike(true, field, value);
    }

    default Children notLikeIgnoreCase(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("notLikeIgnoreCase()", AbstractWrapper.getPropertyName(field), value);
            String pattern = "%" + value.toLowerCase() + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, true, true));
        }
        return getWrapper().self();
    }

    default Children notLikeIgnoreCase(SerializableFunction<T, ?> field, String value) {
        return notLikeIgnoreCase(true, field, value);
    }

    default Children notLikeIgnoreCase(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("notLikeIgnoreCase()", field, value);
            String pattern = "%" + value.toLowerCase() + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, true, true));
        }
        return getWrapper().self();
    }

    default Children notLikeIgnoreCase(String field, String value) {
        return notLikeIgnoreCase(true, field, value);
    }

    default Children notLikeLeft(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("notLikeLeft()", AbstractWrapper.getPropertyName(field), value);
            String pattern = "%" + value;
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false, true));
        }
        return getWrapper().self();
    }

    default Children notLikeLeft(SerializableFunction<T, ?> field, String value) {
        return notLikeLeft(true, field, value);
    }

    default Children notLikeLeft(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("notLikeLeft()", field, value);
            String pattern = "%" + value;
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false, true));
        }
        return getWrapper().self();
    }

    default Children notLikeLeft(String field, String value) {
        return notLikeLeft(true, field, value);
    }

    default Children notLikeRight(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            ensureValueNotNull("notLikeRight()", AbstractWrapper.getPropertyName(field), value);
            String pattern = value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false, true));
        }
        return getWrapper().self();
    }

    default Children notLikeRight(SerializableFunction<T, ?> field, String value) {
        return notLikeRight(true, field, value);
    }

    default Children notLikeRight(boolean condition, String field, String value) {
        if (condition) {
            ensureValueNotNull("notLikeRight()", field, value);
            String pattern = value + "%";
            getWrapper().addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false, true));
        }
        return getWrapper().self();
    }

    default Children notLikeRight(String field, String value) {
        return notLikeRight(true, field, value);
    }
}