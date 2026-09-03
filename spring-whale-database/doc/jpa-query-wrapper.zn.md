# JPA 动态查询 & 实体基类

Spring Whale 提供带自动审计的实体基类，以及在 JPA Criteria API 之上的 MyBatis-Plus 风格动态查询包装器。

## 目录

- [实体基类](#实体基类)
- [动态查询（JpaQueryWrapper）](#动态查询jpaquerywrapper)
- [排序工具（SortUtils）](#排序工具sortutils)

## 实体基类

### BaseEntity

完整版：审计 + 乐观锁 + 逻辑删除。

```java
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String email;
}
```

**自动行为：**

| 注解                | 行为                                     |
|---------------------|------------------------------------------|
| `@PrePersist`       | 自动填充 `createTime`、`updateTime`、`createBy`、`updateBy` |
| `@PreUpdate`        | 自动更新 `updateTime`、`updateBy`          |
| `@SQLDelete`        | 将 DELETE 转为 `UPDATE SET del_flag = 1`  |
| `@SQLRestriction`   | 自动过滤 `del_flag = 0` 的记录             |
| `@Version`          | 乐观锁，通过 version 字段控制              |

### SimpleBaseEntity

轻量版：仅 ID + 创建人/时间。

```java
@Entity
@Table(name = "sys_config")
public class SysConfig extends SimpleBaseEntity {
    private String configKey;
    private String configValue;
}
```

### 对比

| 特性                  | BaseEntity | SimpleBaseEntity |
|-----------------------|------------|------------------|
| ID（自动生成）          | ✅         | ✅               |
| createTime / createBy | ✅         | ✅               |
| updateTime / updateBy | ✅         | ❌               |
| delFlag（逻辑删除）     | ✅         | ❌               |
| @Version（乐观锁）     | ✅         | ❌               |

## 动态查询（JpaQueryWrapper）

`JpaQueryWrapper` 提供链式 API 构建 JPA `Specification` 对象，类似 MyBatis-Plus 风格。

### 基础查询

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

### 条件性查询

条件为 `false` 时跳过该条件：

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .eq(name != null, User::getName, name)
        .gt(minAge > 0, User::getAge, minAge)
        .build();
```

### OR 查询

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .or()
        .eq(User::getStatus, 0)
        .eq(User::getStatus, 2)
        .build();
```

### 嵌套条件

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .eq(User::getStatus, 1)
        .and(nested -> nested
                .like(User::getName, "zhang")
                .or()
                .like(User::getEmail, "zhang"))
        .build();
```

### 支持的操作

| 操作            | 方法                            | 说明                |
|----------------|---------------------------------|---------------------|
| eq             | `.eq(field, value)`             | 等于                |
| ne             | `.ne(field, value)`             | 不等于              |
| gt             | `.gt(field, value)`             | 大于                |
| ge             | `.ge(field, value)`             | 大于等于            |
| lt             | `.lt(field, value)`             | 小于                |
| le             | `.le(field, value)`             | 小于等于            |
| like           | `.like(field, value)`           | LIKE '%value%'      |
| notLike        | `.notLike(field, value)`        | NOT LIKE '%value%'  |
| likeLeft       | `.likeLeft(field, value)`       | LIKE '%value'       |
| likeRight      | `.likeRight(field, value)`      | LIKE 'value%'       |
| in             | `.in(field, values)`            | IN (value1, value2, ...) |
| notIn          | `.notIn(field, values)`         | NOT IN (...)        |
| between        | `.between(field, v1, v2)`       | BETWEEN v1 AND v2   |
| isNull         | `.isNull(field)`                | IS NULL             |
| isNotNull      | `.isNotNull(field)`             | IS NOT NULL         |
| orderByAsc     | `.orderByAsc(field)`            | ORDER BY field ASC  |
| orderByDesc    | `.orderByDesc(field)`           | ORDER BY field DESC |
| groupBy        | `.groupBy(field)`               | GROUP BY field      |
| having         | `.having(predicate)`            | HAVING predicate    |
| distinct       | `.distinct()`                   | SELECT DISTINCT     |
| or             | `.or()`                         | 开始 OR 组          |
| and            | `.and(nested -> ...)`           | 开始 AND 嵌套组     |

### 条件性方法

所有比较方法都有带 `boolean condition` 参数的重载版本：

```java
// 仅当 condition 为 true 时生效
.eq(name != null, User::getName, name)
.gt(minAge > 0, User::getAge, minAge)
.like(StringUtils.hasText(keyword), User::getName, keyword)
```

## 排序工具（SortUtils）

`SortUtils` 支持从逗号分隔字符串构建 Spring Data `Sort`，内置字段白名单安全校验。

### 基础用法

```java
// 前端传参格式："field1,asc,field2,desc"
Sort sort = SortUtils.buildSort("createTime,desc,id,asc");
```

### 白名单校验

只允许指定字段排序：

```java
Sort sort = SortUtils.buildSort("createTime,desc", Set.of("id", "createTime", "name"));
```

### 获取排序字段

```java
String field = SortUtils.getSortField(sort);       // "createTime"
String direction = SortUtils.getSortDirection(sort); // "desc"
```

### API 参考

| 方法                      | 说明                          |
|--------------------------|-------------------------------|
| `buildSort(str)`         | 从逗号分隔字符串构建 Sort        |
| `buildSort(str, whitelist)` | 带字段白名单校验构建 Sort     |
| `getSortField(sort)`     | 获取第一个排序字段名            |
| `getSortDirection(sort)` | 获取第一个排序方向（asc/desc）   |