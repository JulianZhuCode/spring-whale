package io.github.springwhale.database.datascope;

import jakarta.persistence.Column;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for resolving entity metadata: column names from field annotations
 * ({@code @TenantIdField}, {@code @DeptIdField}, {@code @UserIdField}) and
 * entity class from JPA repository proxies.
 *
 * <p>All methods respect JPA {@code @Column(name = "...")} annotations,
 * falling back to camelCase→snake_case conversion.</p>
 */
@Slf4j
public class DataScopeHelper {

    static String camelToSnake(String name) {
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

    public List<String> resolveDeptIdFields(Class<?> entityClass) {
        List<String> fields = findAllFieldsWithAnnotation(entityClass, DeptIdField.class);
        DeptIdScope scope = entityClass.getAnnotation(DeptIdScope.class);
        if (scope != null) {
            resolveClassLevelFields(entityClass, scope.value(), fields);
        }
        return fields;
    }

    public List<String> resolveUserIdFields(Class<?> entityClass) {
        List<String> fields = findAllFieldsWithAnnotation(entityClass, UserIdField.class);
        UserIdScope scope = entityClass.getAnnotation(UserIdScope.class);
        if (scope != null) {
            resolveClassLevelFields(entityClass, scope.value(), fields);
        }
        return fields;
    }

    public List<String> resolveTenantIdFields(Class<?> entityClass) {
        List<String> fields = findAllFieldsWithAnnotation(entityClass, TenantIdField.class);
        TenantIdScope scope = entityClass.getAnnotation(TenantIdScope.class);
        if (scope != null) {
            resolveClassLevelFields(entityClass, scope.value(), fields);
        }
        return fields;
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

    private void resolveClassLevelFields(Class<?> entityClass, String[] fieldNames, List<String> target) {
        for (String fieldName : fieldNames) {
            Field field = findField(entityClass, fieldName);
            if (field != null) {
                target.add(resolveColumnName(field));
            } else {
                log.warn("Class-level scope annotation on {} references field '{}' which does not exist",
                        entityClass.getSimpleName(), fieldName);
            }
        }
    }

    private Field findField(Class<?> entityClass, String fieldName) {
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    public Class<?> resolveEntityClass(Object target) {
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Type type : clazz.getGenericInterfaces()) {
                Class<?> entityClass = resolveEntityClassFromType(type);
                if (entityClass != null) {
                    return entityClass;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Class<?> resolveEntityClassFromType(Type type) {
        if (type instanceof ParameterizedType pt) {
            Type rawType = pt.getRawType();
            if (rawType instanceof Class<?> rawClass && JpaRepository.class.isAssignableFrom(rawClass)) {
                return (Class<?>) pt.getActualTypeArguments()[0];
            }
        } else if (type instanceof Class<?> clazz) {
            if (JpaRepository.class.isAssignableFrom(clazz)) {
                for (Type genericInterface : clazz.getGenericInterfaces()) {
                    Class<?> entityClass = resolveEntityClassFromType(genericInterface);
                    if (entityClass != null) {
                        return entityClass;
                    }
                }
            }
        }
        return null;
    }
}