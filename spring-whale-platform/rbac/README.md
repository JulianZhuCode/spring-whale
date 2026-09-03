# Spring Whale Platform · RBAC

A lightweight RBAC module: an out-of-the-box implementation of users, roles, menus (permissions), and departments (user groups).

This module is a **platform reference implementation** of spring-whale. It provides a ready-to-use permission baseline for small and medium-sized projects, while demonstrating the standard way to wire up the framework's SPIs (data scope handler, security configuration, admin menu, event listener). Business systems may also skip this module entirely and implement their own permission model following its structure.

## Modules

| Module | Description |
|---|---|
| `spring-whale-platform-rbac` | RBAC core: entities, REST API, Spring Security integration, data scope linkage |
| `spring-whale-platform-rbac-ui` | Admin console pages: Thymeleaf templates + automatic menu registration (depends on the rbac and thymeleaf modules) |

Both modules are activated via Spring Boot auto-configuration (`AutoConfiguration.imports`), and every bean is annotated with `@ConditionalOnMissingBean`, so they can be freely replaced.

## Quick Start

```xml
<!-- RBAC core (REST API + security integration) -->
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-rbac</artifactId>
</dependency>

<!-- Optional: admin console pages (Thymeleaf) -->
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-rbac-ui</artifactId>
</dependency>
```

Once added, Flyway creates the tables and seeds the initial data automatically:

- Root department `ROOT` and built-in role `SUPER_ADMIN` (bypasses all permission checks)
- Built-in administrator: **admin / admin** (BCrypt-encoded — change the password before going live)
- With rbac-ui on the classpath, admin menus are seeded as well (System → RBAC → User / Role / Menu / Organization management)

## Permission Model

```
User rbac_user ──< rbac_user_role >── Role rbac_role ──< rbac_role_menu >── Menu/Permission rbac_menu
                                          │
                                          ├──< rbac_role_dept >── Department rbac_group (custom data scope)
                                          └── data_scope (scope type: own dept / dept and children / custom...)
Department rbac_group: tree structure via parent_id + materialized path (e.g. /1/3/);
                       descendant departments are queried with a path prefix match
```

- **Functional permissions**: menus come in three types — `DIRECTORY` (container), `MENU` (page menu), and `BUTTON` (action permission). The permission identifier is the menu `code`, e.g. `rbac:user`, `rbac:user:create`.
- **Role code**: optional; when present it grants Spring Security's `ROLE_<code>`. The `SUPER_ADMIN` role additionally receives the wildcard authority `*`, covering all menus and data scopes.
- **Authentication**: `POST /api/rbac/auth/login`; a JWT is issued after successful authentication (backed by the webmvc module's JWT support).
- **User details**: `UserDetailsServiceImpl` implements Spring Security's `UserDetailsService`, loading roles and menu permissions through batch queries; results are cached per username (`userDetails` cache).

## Data Scope Integration

This module is the default implementation of the database module's `DataScopeHandler` SPI (`RBACDataScopeHandler`), so the `@DataScope` annotation reads RBAC configuration directly — business code never needs to know where permission data comes from:

- **Skip check**: `SUPER_ADMIN` users bypass data scope filtering; RBAC's own tables are excluded from multi-tenant isolation.
- **Department resolution** (based on the role's `data_scope`):
    - `DEPT`: the user's own department
    - `DEPT_AND_CHILD`: own department plus all descendant departments (materialized-path prefix query)
    - `CUSTOM` / `AUTO`: departments explicitly associated with the role via `rbac_role_dept`; when a module is specified, results are further matched through role-menu associations
- **Performance**: resolution results are stored in the `dataScope` cache (TTL configured in the database module) and actively invalidated via domain events when permissions change:

| Event | Invalidation scope |
|---|---|
| `UserRoleChangedEvent` | the affected user |
| `RoleChangedEvent` | all users holding that role |
| `GroupChangedEvent` | users in that department and its descendants, plus users of roles whose custom scope references that department |

Events are published through the event module (after transaction commit); invalidation behaves identically in local and MQ modes.

### Microservices: Remote Data Scope Resolution

When RBAC is deployed as a standalone permission service, downstream services can resolve data scopes remotely via Feign. The internal endpoints are **disabled by default** and must be explicitly enabled on the permission service:

```yaml
spring.whale.database.datascope:
  expose-remote-api: true   # Enable only on the RBAC service; in production, restrict to service-to-service traffic at the gateway
```

Once enabled, `/api/rbac/datascope/**` is exposed (skip / skip-tenant / resolve / cache eviction). The contract is guaranteed by the `DataScopeRemoteApi` interface to stay in sync with the downstream Feign client.

## Admin Console (rbac-ui)

- `RbacMenuProvider` implements the thymeleaf module's `AdminMenuProvider` SPI: it loads menus from the database and filters them by the current user's roles (`SUPER_ADMIN` sees everything). Only visible, enabled directories and menus appear in the sidebar; button-type entries are excluded.
- `RbacPageController` serves four admin pages (`/admin/rbac/users|roles|menus|groups`), each guarded by `@PreAuthorize`, with pagination, keyword search, and sorting support.
- Page labels support Chinese / English / Japanese (`messages-rbac*.properties`), and menu names support i18n keys.

## REST API Overview

| Feature | Method & Path |
|---|---|
| Login | `POST /api/rbac/auth/login` |
| Users | `GET/POST /api/rbac/users`, `GET/PUT/DELETE /api/rbac/users/{id}` |
| Roles | `GET/POST /api/rbac/roles`, `GET/PUT/DELETE /api/rbac/roles/{id}` |
| Menus | `GET/POST /api/rbac/menus`, `GET/PUT/DELETE /api/rbac/menus/{id}`, `GET /api/rbac/menus/tree` (menu tree permitted for the current user) |
| Departments | `GET/POST /api/rbac/groups`, `GET/PUT/DELETE /api/rbac/groups/{id}`, `GET /api/rbac/groups/tree` |
| Role-Menu | `GET/POST/DELETE /api/rbac/roles/{roleId}/menus` |
| Role-Dept | `GET/POST/DELETE /api/rbac/roles/{roleId}/depts` |
| User-Role | `GET/POST/DELETE /api/rbac/users/{userId}/roles` |
| Data scope (internal) | `GET /api/rbac/datascope/skip/{userId}` etc.; requires `expose-remote-api` to be enabled |

Auth-related URLs (`/api/rbac/auth/**`, `/api/rbac/public/**`) are uniformly permitted through the webmvc module's Security SPI by `RbacSecurityConfigProvider`.

## Tables

| Table / View | Description |
|---|---|
| `rbac_user` | Users (`group_id` is the department; annotated with `@DeptIdField` for data scope) |
| `rbac_role` | Roles (the `data_scope` column holds the data scope type) |
| `rbac_menu` | Menus and permission identifiers (tree via parent_id) |
| `rbac_group` | Departments / user groups (`@DeptIdScope`, materialized path) |
| `rbac_user_role` | User-role associations |
| `rbac_role_menu` | Role-menu associations |
| `rbac_role_dept` | Role-custom department associations (CUSTOM data scope) |
| `rbac_user_role_scope_view` | Read-only view: user → role → data scope → custom departments, for batch resolution queries |

All tables extend the framework's `BaseEntity` (audit fields, optimistic locking, logical deletion) and are created automatically by Flyway migration scripts.
