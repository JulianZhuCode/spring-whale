# JPA Query Wrapper & Entity Base Classes

Spring Whale provides entity base classes with automatic auditing and a MyBatis-Plus style dynamic query wrapper on top
of JPA Criteria API.

## Table of Contents

- [Entity Base Classes](#entity-base-classes)
- [Dynamic Queries (JpaQueryWrapper)](#dynamic-queries-jpaquerywrapper)
- [Sort Utility (SortUtils)](#sort-utility-sortutils)

## Entity Base Classes

### BaseEntity

Full version: auditing + optimistic locking + soft delete.

```java
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String email;
}
```

**Automatic behaviors:**

| Annotation          | Behavior                                                                 |
|---------------------|--------------------------------------------------------------------------|
| `@PrePersist`       | Auto-fills `createTime`, `updateTime`, `createBy`, `updateBy`           |
| `@PreUpdate`        | Auto-updates `updateTime`, `updateBy`                                    |
| `@SQLDelete`        | Converts DELETE to `UPDATE SET del_flag = 1`                            |
| `@SQLRestriction`   | Auto-filters `del_flag = 0` records                                     |
| `@Version`          | Optimistic locking via version field                                     |

### SimpleBaseEntity

Lightweight version: only ID + created by/time.

```java
@Entity
@Table(name = "sys_config")
public class SysConfig extends SimpleBaseEntity {
    private String configKey;
    private String configValue;
}
```

### Comparison

| Feature               | BaseEntity | SimpleBaseEntity |
|-----------------------|------------|------------------|
| ID (auto-generated)   | ✅         | ✅               |
| createTime / createBy | ✅         | ✅               |
| updateTime / updateBy | ✅         | ❌               |
| delFlag (soft delete) | ✅         | ❌               |
| @Version (optimistic) | ✅         | ❌               |

## Dynamic Queries (JpaQueryWrapper)

`JpaQueryWrapper` provides a chainable API for building JPA `Specification` objects, similar to MyBatis-Plus.

### Basic Query

```java
@Autowired
private UserRepository userRepository;

Specification<User> spec = JpaQueryWrapper.of(User.class)
        .eq(User::getStatus, 1)
        .like(User::getName, "zhang")
        .orderByDesc(User::getCreateTime)
        .build();
Page<User> page = userRepository.findAll(spec, pageable);
```

### Conditional Query

Conditions are skipped when the condition is `false`:

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .eq(name != null, User::getName, name)
        .gt(minAge > 0, User::getAge, minAge)
        .build();
```

### OR Query

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .or()
        .eq(User::getStatus, 0)
        .eq(User::getStatus, 2)
        .build();
```

### Nested Conditions

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .eq(User::getStatus, 1)
        .and(nested -> nested
                .like(User::getName, "zhang")
                .or()
                .like(User::getEmail, "zhang"))
        .build();
```

### Supported Operations

| Operation      | Method                          | Description                          |
|----------------|---------------------------------|--------------------------------------|
| eq             | `.eq(field, value)`             | Equal to                             |
| ne             | `.ne(field, value)`             | Not equal to                         |
| gt             | `.gt(field, value)`             | Greater than                          |
| ge             | `.ge(field, value)`             | Greater than or equal to             |
| lt             | `.lt(field, value)`             | Less than                             |
| le             | `.le(field, value)`             | Less than or equal to                |
| like           | `.like(field, value)`           | LIKE '%value%'                       |
| notLike        | `.notLike(field, value)`        | NOT LIKE '%value%'                   |
| likeLeft       | `.likeLeft(field, value)`       | LIKE '%value'                        |
| likeRight      | `.likeRight(field, value)`      | LIKE 'value%'                        |
| in             | `.in(field, values)`            | IN (value1, value2, ...)             |
| notIn          | `.notIn(field, values)`         | NOT IN (value1, value2, ...)         |
| between        | `.between(field, v1, v2)`       | BETWEEN v1 AND v2                    |
| isNull         | `.isNull(field)`                | IS NULL                              |
| isNotNull      | `.isNotNull(field)`             | IS NOT NULL                          |
| orderByAsc     | `.orderByAsc(field)`            | ORDER BY field ASC                   |
| orderByDesc    | `.orderByDesc(field)`           | ORDER BY field DESC                  |
| groupBy        | `.groupBy(field)`               | GROUP BY field                       |
| having         | `.having(predicate)`            | HAVING predicate                     |
| distinct       | `.distinct()`                   | SELECT DISTINCT                      |
| or             | `.or()`                         | Start OR group                       |
| and            | `.and(nested -> ...)`           | Start AND nested group               |

### Conditional Methods

All comparison methods have an overloaded version with a `boolean condition` parameter:

```java
// Only applies when condition is true
.eq(name != null, User::getName, name)
.gt(minAge > 0, User::getAge, minAge)
.like(StringUtils.hasText(keyword), User::getName, keyword)
```

## Sort Utility (SortUtils)

`SortUtils` supports building Spring Data `Sort` from comma-separated strings, with built-in field whitelist validation.

### Basic Usage

```java
// Frontend parameter format: "field1,asc,field2,desc"
Sort sort = SortUtils.buildSort("createTime,desc,id,asc");
```

### Whitelist Validation

Only allow specified fields for sorting:

```java
Sort sort = SortUtils.buildSort("createTime,desc", Set.of("id", "createTime", "name"));
```

### Getting Sort Fields

```java
String field = SortUtils.getSortField(sort);       // "createTime"
String direction = SortUtils.getSortDirection(sort); // "desc"
```

### API Reference

| Method               | Description                                                |
|----------------------|------------------------------------------------------------|
| `buildSort(str)`     | Build Sort from comma-separated string                     |
| `buildSort(str, whitelist)` | Build Sort with field whitelist validation         |
| `getSortField(sort)` | Get the first sort field name                              |
| `getSortDirection(sort)` | Get the first sort direction (asc/desc)                |