package io.github.springwhale.database.datascope;

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
                    fields.add(field.getName());
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }
}