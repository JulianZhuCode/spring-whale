package io.github.springwhale.database.criteria;

import io.github.springwhale.database.SerializableFunction;

public interface Like<T, Children extends AbstractWrapper<T, Children>> {

    default Children like(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : "%" + value + "%";
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false));
        }
        return (Children) this;
    }

    default Children like(SerializableFunction<T, ?> field, String value) {
        return like(true, field, value);
    }

    default Children like(boolean condition, String field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : "%" + value + "%";
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false));
        }
        return (Children) this;
    }

    default Children like(String field, String value) {
        return like(true, field, value);
    }

    default Children likeIgnoreCase(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : "%" + value.toLowerCase() + "%";
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, true));
        }
        return (Children) this;
    }

    default Children likeIgnoreCase(SerializableFunction<T, ?> field, String value) {
        return likeIgnoreCase(true, field, value);
    }

    default Children likeIgnoreCase(boolean condition, String field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : "%" + value.toLowerCase() + "%";
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, true));
        }
        return (Children) this;
    }

    default Children likeIgnoreCase(String field, String value) {
        return likeIgnoreCase(true, field, value);
    }

    default Children likeLeft(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : "%" + value;
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false));
        }
        return (Children) this;
    }

    default Children likeLeft(SerializableFunction<T, ?> field, String value) {
        return likeLeft(true, field, value);
    }

    default Children likeLeft(boolean condition, String field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : "%" + value;
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false));
        }
        return (Children) this;
    }

    default Children likeLeft(String field, String value) {
        return likeLeft(true, field, value);
    }

    default Children likeRight(boolean condition, SerializableFunction<T, ?> field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : value + "%";
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(AbstractWrapper.getPropertyName(field), pattern, false));
        }
        return (Children) this;
    }

    default Children likeRight(SerializableFunction<T, ?> field, String value) {
        return likeRight(true, field, value);
    }

    default Children likeRight(boolean condition, String field, String value) {
        if (condition) {
            AbstractWrapper<T, Children> wrapper = (AbstractWrapper<T, Children>) this;
            String pattern = value == null ? null : value + "%";
            wrapper.addCondition(new AbstractWrapper.LikeCondition<>(field, pattern, false));
        }
        return (Children) this;
    }

    default Children likeRight(String field, String value) {
        return likeRight(true, field, value);
    }
}