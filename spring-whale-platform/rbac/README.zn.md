# Spring Whale Platform · RBAC

轻量级 RBAC 权限模块：用户、角色、菜单（权限）、部门（用户组）的开箱即用实现。

本模块是 spring-whale 的**平台参考实现**：面向中小项目提供可直接使用的权限基线，
同时演示框架各 SPI（数据权限处理器、安全配置、后台菜单、事件监听）的标准落地写法。
业务系统也可以不引本模块，仅参照其结构实现自己的权限体系。

## 模块组成

| 模块                              | 说明                                                   |
|---------------------------------|------------------------------------------------------|
| `spring-whale-platform-rbac`    | RBAC 核心：实体、REST API、Spring Security 集成、数据权限联动        |
| `spring-whale-platform-rbac-ui` | 管理后台页面：Thymeleaf 模板 + 菜单自动注册（依赖 rbac 与 thymeleaf 模块） |

两个模块均通过 Spring Boot 自动装配生效（`AutoConfiguration.imports`），
所有 Bean 均带 `@ConditionalOnMissingBean`，可自由替换。

## 快速开始

```xml
<!-- RBAC 核心（REST API + 安全集成） -->
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-rbac</artifactId>
</dependency>

        <!-- 可选：管理后台页面（Thymeleaf） -->
<dependency>
<groupId>io.github.julianzhucode</groupId>
<artifactId>spring-whale-platform-rbac-ui</artifactId>
</dependency>
```

引入后 Flyway 自动建表并写入初始数据：

- 根部门 `ROOT`、内置角色 `SUPER_ADMIN`（绕过全部权限校验）
- 内置管理员：**admin / admin**（BCrypt 加密，上线前请修改密码）
- 引入 rbac-ui 后自动写入后台菜单（系统 → RBAC → 用户/角色/菜单/组织管理）

## 权限模型

```
用户 rbac_user ──< rbac_user_role >── 角色 rbac_role ──< rbac_role_menu >── 菜单/权限 rbac_menu
                                          │
                                          ├──< rbac_role_dept >── 部门 rbac_group（自定义数据权限范围）
                                          └── data_scope（数据权限范围：本部门/本部门及下级/自定义…）
部门 rbac_group：树形结构，parent_id + 物化路径 path（如 /1/3/），下级部门查询走 path 前缀匹配
```

- **功能权限**：菜单分三类 `DIRECTORY`（目录）、`MENU`（页面菜单）、`BUTTON`（按钮/操作权限）；
  权限标识即菜单 `code`，如 `rbac:user`、`rbac:user:create`。
- **角色编码**：角色 `code` 可选，存在时授予 Spring Security 的 `ROLE_<code>`；
  `SUPER_ADMIN` 角色额外授予通配权限 `*`，拥有全部菜单与数据范围。
- **登录认证**：`POST /api/rbac/auth/login`，认证通过后签发 JWT（由 webmvc 模块的 JWT 体系承载）。
- **用户详情**：`UserDetailsServiceImpl` 实现 Spring Security `UserDetailsService`，
  批量查询角色与菜单权限，结果按用户名缓存（`userDetails` 缓存）。

## 与数据权限联动

本模块是 database 模块 `DataScopeHandler` SPI 的默认实现（`RBACDataScopeHandler`），
让 `@DataScope` 注解直接读取 RBAC 配置，业务侧无需关心权限数据从哪来：

- **跳过判断**：`SUPER_ADMIN` 用户跳过数据权限过滤；RBAC 自身表不参与多租户隔离。
- **部门范围解析**（按角色配置的 `data_scope`）：
    - `DEPT`：用户所在部门
    - `DEPT_AND_CHILD`：所在部门 + 全部下级部门（物化路径前缀查询）
    - `CUSTOM` / `AUTO`：角色通过 `rbac_role_dept` 显式关联的部门；
      指定 module 时按"角色—菜单"关联进一步匹配
- **性能**：解析结果走 `dataScope` 缓存（TTL 由 database 模块配置），
  权限变更时通过领域事件主动失效：

| 事件                     | 失效范围                          |
|------------------------|-------------------------------|
| `UserRoleChangedEvent` | 该用户                           |
| `RoleChangedEvent`     | 拥有该角色的全部用户                    |
| `GroupChangedEvent`    | 该部门及下级部门的用户 + 自定义范围引用该部门的角色用户 |

事件经 event 模块发布（事务提交后），本地或 MQ 模式下失效逻辑一致。

### 微服务场景：远程数据权限解析

RBAC 作为独立权限服务部署时，下游服务可通过 Feign 远程解析数据权限。
内部接口默认**关闭**，需在权限服务显式开启：

```yaml
spring.whale.database.datascope:
  expose-remote-api: true   # 仅在 RBAC 服务开启，生产环境建议网关层限制为服务间访问
```

开启后暴露 `/api/rbac/datascope/**`（skip / skip-tenant / resolve / cache 失效），
契约由 `DataScopeRemoteApi` 接口保证与下游 Feign 客户端一致。

## 管理后台（rbac-ui）

- `RbacMenuProvider` 实现 thymeleaf 模块的 `AdminMenuProvider` SPI：
  从数据库加载菜单，按当前用户角色过滤（`SUPER_ADMIN` 可见全部），
  仅展示可见且启用的目录/菜单，按钮类型不进侧边栏。
- `RbacPageController` 提供四个后台页面（`/admin/rbac/users|roles|menus|groups`），
  均带 `@PreAuthorize` 权限校验，支持分页、关键字、排序。
- 页面文案支持中 / 英 / 日三语（`messages-rbac*.properties`），菜单名支持 i18n key。

## REST API 一览

| 功能       | 方法与路径                                                                                                    |
|----------|----------------------------------------------------------------------------------------------------------|
| 登录       | `POST /api/rbac/auth/login`                                                                              |
| 用户       | `GET/POST /api/rbac/users`，`GET/PUT/DELETE /api/rbac/users/{id}`                                         |
| 角色       | `GET/POST /api/rbac/roles`，`GET/PUT/DELETE /api/rbac/roles/{id}`                                         |
| 菜单       | `GET/POST /api/rbac/menus`，`GET/PUT/DELETE /api/rbac/menus/{id}`，`GET /api/rbac/menus/tree`（当前用户有权限的菜单树） |
| 部门       | `GET/POST /api/rbac/groups`，`GET/PUT/DELETE /api/rbac/groups/{id}`，`GET /api/rbac/groups/tree`           |
| 角色-菜单    | `GET/POST/DELETE /api/rbac/roles/{roleId}/menus`                                                         |
| 角色-部门    | `GET/POST/DELETE /api/rbac/roles/{roleId}/depts`                                                         |
| 用户-角色    | `GET/POST/DELETE /api/rbac/users/{userId}/roles`                                                         |
| 数据权限（内部） | `GET /api/rbac/datascope/skip/{userId}` 等，需开启 `expose-remote-api`                                        |

鉴权相关 URL（`/api/rbac/auth/**`、`/api/rbac/public/**`）由
`RbacSecurityConfigProvider` 通过 webmvc 模块的 Security SPI 统一放行。

## 数据表

| 表 / 视图                      | 说明                                          |
|-----------------------------|---------------------------------------------|
| `rbac_user`                 | 用户（`group_id` 即部门，标注 `@DeptIdField` 参与数据权限） |
| `rbac_role`                 | 角色（`data_scope` 列存数据权限范围）                   |
| `rbac_menu`                 | 菜单与权限标识（树形 parent_id）                       |
| `rbac_group`                | 部门/用户组（`@DeptIdScope`，物化路径 path）            |
| `rbac_user_role`            | 用户-角色关联                                     |
| `rbac_role_menu`            | 角色-菜单关联                                     |
| `rbac_role_dept`            | 角色-自定义部门关联（CUSTOM 数据权限）                     |
| `rbac_user_role_scope_view` | 只读视图：用户→角色→数据范围→自定义部门，供权限解析批量查询             |

所有表继承框架 `BaseEntity`（审计字段、乐观锁、逻辑删除），由 Flyway 迁移脚本自动创建。
