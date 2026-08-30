package io.github.springwhale.database.datascope;

import jakarta.persistence.Column;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DataScopeHelper {

    public List<String> resolveDeptIdFields(Class<?> entityClass) {
        return findAllFieldsWithAnnotation(entityClass, DeptIdField.class);
    }

    public List<String> resolveUserIdFields(Class<?> entityClass) {
        return findAllFieldsWithAnnotation(entityClass, UserIdField.class);
    }

    private List<String> findAllFieldsWithAnnotation(Class<?> entityClass, Class<? extends java.lang.annotation.Annotation> annotationType) {
        List<String> fields = new ArrayList<>();
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotationType)) {
                    fields.add(resolveColumnName(field));
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private String resolveColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        return camelToSnake(field.getName());
    }

    private String camelToSnake(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0 && !Character.isUpperCase(name.charAt(i - 1))) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}