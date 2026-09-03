# JPA 動的クエリ & エンティティ基底クラス

Spring Whale は自動監査付きのエンティティ基底クラスと、JPA Criteria API 上の MyBatis-Plus スタイルの動的クエリラッパーを提供します。

## 目次

- [エンティティ基底クラス](#エンティティ基底クラス)
- [動的クエリ（JpaQueryWrapper）](#動的クエリjpaquerywrapper)
- [ソートユーティリティ（SortUtils）](#ソートユーティリティsortutils)

## エンティティ基底クラス

### BaseEntity

フルバージョン：監査 + 楽観ロック + 論理削除。

```java
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String email;
}
```

**自動動作：**

| アノテーション      | 動作                                                          |
|---------------------|---------------------------------------------------------------|
| `@PrePersist`       | `createTime`、`updateTime`、`createBy`、`updateBy` を自動設定   |
| `@PreUpdate`        | `updateTime`、`updateBy` を自動更新                             |
| `@SQLDelete`        | DELETE を `UPDATE SET del_flag = 1` に変換                     |
| `@SQLRestriction`   | `del_flag = 0` のレコードを自動フィルタリング                     |
| `@Version`          | 楽観ロック、バージョンフィールドで制御                              |

### SimpleBaseEntity

軽量版：ID + 作成者/日時のみ。

```java
@Entity
@Table(name = "sys_config")
public class SysConfig extends SimpleBaseEntity {
    private String configKey;
    private String configValue;
}
```

### 比較

| 機能                   | BaseEntity | SimpleBaseEntity |
|------------------------|------------|------------------|
| ID（自動生成）           | ✅         | ✅               |
| createTime / createBy  | ✅         | ✅               |
| updateTime / updateBy  | ✅         | ❌               |
| delFlag（論理削除）      | ✅         | ❌               |
| @Version（楽観ロック）   | ✅         | ❌               |

## 動的クエリ（JpaQueryWrapper）

`JpaQueryWrapper` は JPA `Specification` オブジェクトを構築するためのチェーン可能な API を提供し、MyBatis-Plus に似たスタイルです。

### 基本クエリ

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

### 条件付きクエリ

条件が `false` の場合にスキップされます：

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .eq(name != null, User::getName, name)
        .gt(minAge > 0, User::getAge, minAge)
        .build();
```

### OR クエリ

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .or()
        .eq(User::getStatus, 0)
        .eq(User::getStatus, 2)
        .build();
```

### ネスト条件

```java
Specification<User> spec = JpaQueryWrapper.of(User.class)
        .eq(User::getStatus, 1)
        .and(nested -> nested
                .like(User::getName, "zhang")
                .or()
                .like(User::getEmail, "zhang"))
        .build();
```

### サポートされる操作

| 操作            | メソッド                        | 説明                    |
|----------------|---------------------------------|-------------------------|
| eq             | `.eq(field, value)`             | 等しい                  |
| ne             | `.ne(field, value)`             | 等しくない              |
| gt             | `.gt(field, value)`             | より大きい              |
| ge             | `.ge(field, value)`             | 以上                    |
| lt             | `.lt(field, value)`             | より小さい              |
| le             | `.le(field, value)`             | 以下                    |
| like           | `.like(field, value)`           | LIKE '%value%'          |
| notLike        | `.notLike(field, value)`        | NOT LIKE '%value%'      |
| likeLeft       | `.likeLeft(field, value)`       | LIKE '%value'           |
| likeRight      | `.likeRight(field, value)`      | LIKE 'value%'           |
| in             | `.in(field, values)`            | IN (value1, value2, ...) |
| notIn          | `.notIn(field, values)`         | NOT IN (...)            |
| between        | `.between(field, v1, v2)`       | BETWEEN v1 AND v2       |
| isNull         | `.isNull(field)`                | IS NULL                 |
| isNotNull      | `.isNotNull(field)`             | IS NOT NULL             |
| orderByAsc     | `.orderByAsc(field)`            | ORDER BY field ASC      |
| orderByDesc    | `.orderByDesc(field)`           | ORDER BY field DESC     |
| groupBy        | `.groupBy(field)`               | GROUP BY field          |
| having         | `.having(predicate)`            | HAVING predicate        |
| distinct       | `.distinct()`                   | SELECT DISTINCT         |
| or             | `.or()`                         | OR グループ開始          |
| and            | `.and(nested -> ...)`           | AND ネストグループ開始   |

### 条件付きメソッド

すべての比較メソッドには `boolean condition` パラメータ付きのオーバーロード版があります：

```java
// condition が true の場合のみ適用
.eq(name != null, User::getName, name)
.gt(minAge > 0, User::getAge, minAge)
.like(StringUtils.hasText(keyword), User::getName, keyword)
```

## ソートユーティリティ（SortUtils）

`SortUtils` はカンマ区切り文字列から Spring Data `Sort` を構築し、フィールドホワイトリスト検証を内蔵しています。

### 基本使用法

```java
// フロントエンドパラメータ形式："field1,asc,field2,desc"
Sort sort = SortUtils.buildSort("createTime,desc,id,asc");
```

### ホワイトリスト検証

指定されたフィールドのみソートを許可：

```java
Sort sort = SortUtils.buildSort("createTime,desc", Set.of("id", "createTime", "name"));
```

### ソートフィールドの取得

```java
String field = SortUtils.getSortField(sort);       // "createTime"
String direction = SortUtils.getSortDirection(sort); // "desc"
```

### API リファレンス

| メソッド                    | 説明                                |
|----------------------------|-------------------------------------|
| `buildSort(str)`           | カンマ区切り文字列から Sort を構築    |
| `buildSort(str, whitelist)` | フィールドホワイトリスト検証付きで構築 |
| `getSortField(sort)`       | 最初のソートフィールド名を取得        |
| `getSortDirection(sort)`   | 最初のソート方向を取得（asc/desc）    |