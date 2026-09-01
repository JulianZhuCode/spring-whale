# spring-whale-thymeleaf

The **Admin Console UI module** of the Spring Whale framework, built on Spring Boot Auto-Configuration + Thymeleaf + Bootstrap 5, providing a pluggable admin dashboard framework for business modules.

---

## Table of Contents

- [Quick Start](#quick-start)
- [Core Features](#core-features)
- [Architecture](#architecture)
- [Configuration](#configuration)
- [SPI Extension Guide](#spi-extension-guide)
  - [Registering Menus (AdminMenuProvider)](#registering-menus-adminmenuprovider)
  - [Security Configuration (SecurityConfigProvider)](#security-configuration-securityconfigprovider)
- [Template Development Guide](#template-development-guide)
  - [Page Layout](#page-layout)
  - [Data Table](#data-table)
  - [CRUD Modal](#crud-modal)
  - [Delete Button](#delete-button)
  - [Confirm Dialog](#confirm-dialog)
  - [Search Bar](#search-bar)
  - [Field Type Declaration](#field-type-declaration)
  - [Tag Selector](#tag-selector)
- [Frontend JavaScript API](#frontend-javascript-api)
- [Dependencies](#dependencies)

---

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-thymeleaf</artifactId>
</dependency>
```

### 2. Configuration (Optional)

```yaml
spring:
  whale:
    thymeleaf:
      admin:
        brand-name: My App
        short-name: MA
        copyright: My Company
        version: 1.0.0
```

### 3. Start the Application

Visit `http://localhost:8080/admin/login` to access the admin console.

---

## Core Features

| Feature | Description |
|------|------|
| Login Page | JWT Cookie authentication with diagnostic hints (no token / invalid token / auth required) |
| Dashboard | Module statistics cards, auto-displaying all registered menu modules |
| Sidebar Menu | Collapsible/expandable, supports multi-level groups, icons, permission filtering, i18n |
| Breadcrumb | Auto-generated based on the current path |
| Data Table | Pagination, sorting, search, reusable via Thymeleaf fragments |
| CRUD Modal | Declarative `data-*` attribute-driven, no JS required |
| Global Confirm Dialog | Promise API with `async/await` support |
| Delete Button | Declarative confirmation + API call + auto-refresh |
| Toast Notification | Success/error messages |
| Page-level Error Banner | Auto-displayed on API call failure |
| Error Pages | Friendly 403/404/500 HTML error pages |
| i18n Internationalization | Supports Simplified Chinese, English, and Japanese |
| Permission-aware UI | Menus and buttons auto-show/hide based on user permissions |

---

## Architecture

### Package Structure

```
thymeleaf/
├── autoconfigure/     Spring Boot auto-configuration entry + config properties (AdminProperties)
├── controller/        Controllers + @AdminPage marker annotation
├── menu/              SPI menu interface + model
└── security/          Security configuration SPI implementation
```

### Design Patterns

| Pattern | Description |
|------|------|
| **SPI Extension** | Business modules implement `AdminMenuProvider` / `SecurityConfigProvider` to auto-register |
| **Custom Annotation + Advice** | `@AdminPage` marker + `@ControllerAdvice(annotations = ...)` for precise global attribute injection |
| **Decorator Pattern** | `layout.html` uses `layout:decorate` to unify page layout |
| **data-\* Attribute-Driven** | Frontend CRUD behavior declared via HTML attributes, no JS required |

### Security Configuration Chain

The `SecurityAutoConfiguration` in the `spring-whale-webmvc` module builds the `SecurityFilterChain`, with each `SecurityConfigProvider` participating via SPI:

```
SecurityAutoConfiguration (spring-whale-webmvc)
  └── securityFilterChain()
      ├── collectPermitAllUrls()       ← Collects permit-all URLs from each provider
      │   └── ThymeleafSecurityConfigProvider.getPermitAllUrls()
      │       └── /admin/login, /admin/css/**, /admin/js/**, ...
      ├── HttpSecurity base configuration
      └── applyCustomConfigurations()
          └── ThymeleafSecurityConfigProvider.configure()
              └── http.exceptionHandling(entryPoint)  ← Registers AuthenticationEntryPoint
```

---

## Configuration

### AdminProperties

| Property | Default | Description |
|------|--------|------|
| `spring.whale.thymeleaf.admin.brand-name` | `Spring Whale` | Full brand name displayed in page titles and login page |
| `spring.whale.thymeleaf.admin.short-name` | `SW Admin` | Short name displayed in the sidebar |
| `spring.whale.thymeleaf.admin.copyright` | `Spring Whale Framework` | Copyright text displayed in the footer |
| `spring.whale.thymeleaf.admin.version` | `0.0.2` | Version string displayed on the dashboard |

---

## SPI Extension Guide

### Registering Menus (AdminMenuProvider)

Business modules implement the `AdminMenuProvider` interface and register as a Spring Bean to automatically display menus in the sidebar:

```java
@Component
public class RbacMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
            // Group menu (acts as a container, no URL)
            MenuItem.group("rbac", "RBAC", "menu.rbac", "shield", 10),

            // Leaf menu (has URL, linked to group via parentKey)
            MenuItem.leaf("rbac-users", "rbac", "User Management", "menu.rbac.user_management",
                    "/admin/rbac/users", "people", "rbac:user:read", 1),

            MenuItem.leaf("rbac-roles", "rbac", "Role Management", "menu.rbac.role_management",
                    "/admin/rbac/roles", "person-badge", "rbac:role:read", 2),

            MenuItem.leaf("rbac-groups", "rbac", "Group Management", "menu.rbac.group_management",
                    "/admin/rbac/groups", "diagram-3", "rbac:group:read", 3)
        );
    }

    @Override
    public int getOrder() {
        return 10;  // Lower numbers appear first
    }
}
```

#### MenuItem Factory Methods

| Method | Description |
|------|------|
| `MenuItem.group(key, label, icon, sort)` | Basic group (no i18n) |
| `MenuItem.group(key, label, labelI18nKey, icon, sort)` | Group with i18n |
| `MenuItem.leaf(key, parentKey, label, url, sort)` | Basic leaf menu (no icon/permission) |
| `MenuItem.leaf(key, parentKey, label, url, icon, sort)` | Leaf menu with icon |
| `MenuItem.leaf(key, parentKey, label, url, icon, permission, sort)` | Leaf menu with permission |
| `MenuItem.leaf(key, parentKey, label, labelI18nKey, url, icon, permission, sort)` | Full-featured leaf menu (all options) |

> **Permission Note:** When `permission` is set, the menu item is only visible to users who hold that authority or the `*` wildcard authority.

### Security Configuration (SecurityConfigProvider)

By default, `/admin/login` and static resource paths are already whitelisted. To extend:

```java
@Component
public class MySecurityConfig implements SecurityConfigProvider {

    @Override
    public List<String> getPermitAllUrls() {
        return List.of("/api/public/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
    }

    @Override
    public int getOrder() {
        return 200;  // Executed after RBAC (100)
    }
}
```

---

## Template Development Guide

### Page Layout

All admin pages use `layout.html` as the decorator template. **The page Controller must be annotated with `@AdminPage`** to automatically inject global variables such as the menu tree, user permissions, and current path:

```java
@AdminPage
@Controller
@RequestMapping("/admin/my-module")
public class MyController {
    @GetMapping
    public String index() {
        return "admin/my-module/index";
    }
}
```

```html
<!DOCTYPE html>
<html layout:decorate="~{admin/layout}"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      xmlns:th="http://www.thymeleaf.org">

<main layout:fragment="content">
    <!-- Page content -->
</main>
</html>
```

### Data Table

Use the `data-table` Thymeleaf fragment to inject custom content via header and row fragments:

```html
<th:block th:replace="~{admin/fragments/data-table :: table(
    title='User Management',
    description='Manage system users',
    createUrl='/admin/rbac/users',
    createPermission='rbac:user:create',
    items=${page.content},
    emptyMessage='No user data',
    baseUrl='/admin/rbac/users',
    page=${page},
    colspan=6,
    headers=~{:: #user-headers},
    rows=~{:: #user-rows}
)}"></th:block>

<th:block id="user-headers" th:fragment="user-headers">
    <th>Username</th>
    <th>Email</th>
    <th>Group</th>
    <th>Status</th>
    <th>Actions</th>
</th:block>

<th:block id="user-rows" th:fragment="user-rows">
    <td th:text="${item.username}">admin</td>
    <td th:text="${item.email}">admin@example.com</td>
    <td th:text="${item.groupName}">Default Group</td>
    <td>
        <span class="badge bg-success" th:if="${item.status == 1}">Active</span>
        <span class="badge bg-secondary" th:unless="${item.status == 1}">Disabled</span>
    </td>
    <td>
        <button class="btn btn-sm btn-outline-primary"
                data-modal="userModal"
                th:attr="data-edit-id=${item.id}">
            <i class="bi bi-pencil"></i>
        </button>
        <button class="btn btn-sm btn-outline-danger"
                data-delete-api="/api/rbac/users"
                th:attr="data-delete-id=${item.id},data-delete-name=${item.username}">
            <i class="bi bi-trash"></i>
        </button>
    </td>
</th:block>
```

#### Table Parameters

| Parameter | Type | Default | Description |
|------|------|--------|------|
| `title` | String | - | Page title |
| `description` | String | - | Page description |
| `createUrl` | String | - | URL for the create button; button hidden if empty |
| `createPermission` | String | - | Permission required for the create button; null means no restriction |
| `items` | List | - | Data list |
| `emptyMessage` | String | - | Message displayed when no data is available |
| `baseUrl` | String | - | Base URL used for pagination and sorting |
| `page` | Page | - | Spring Data Page object |
| `colspan` | int | - | Column span for the empty data row |
| `headers` | Fragment | - | Header fragment |
| `rows` | Fragment | - | Row fragment |
| `filter` | Fragment | - | Search bar fragment (optional) |
| `sortableId` | String | `'true'` | Whether to show the ID sort column; pass `'false'` to hide |

> **Modal-based Creation:** To open a modal instead of navigating to a new page when clicking the create button, set the `createModal` variable in the Controller's Model to the modal ID. The data table fragment auto-detects this variable and switches to a modal trigger button.

### CRUD Modal

Declarative `data-*` attribute-driven, no JS required:

```html
<!-- Create/Edit buttons -->
<button class="btn btn-primary" data-modal="userModal">+ New</button>
<button class="btn btn-sm btn-outline-primary"
        data-modal="userModal" th:attr="data-edit-id=${item.id}">Edit</button>

<!-- Modal -->
<div class="modal fade" id="userModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">User</h5>
                <button class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form class="dict-modal-form" data-api-base="/api/rbac/users"
                      data-group-api="/api/rbac/groups?page=0&size=1000">
                    <div class="mb-3">
                        <label class="form-label">Username</label>
                        <input class="form-control" name="username" type="text" required>
                        <div class="invalid-feedback" data-field="username"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Group</label>
                        <select class="form-select" name="groupId" data-int-field>
                            <option value="">Please select</option>
                        </select>
                        <div class="invalid-feedback" data-field="groupId"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Status</label>
                        <select class="form-select" name="status" data-int-field>
                            <option value="1">Active</option>
                            <option value="0">Disabled</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button class="btn btn-primary modal-submit">Save</button>
            </div>
        </div>
    </div>
</div>
```

#### Modal Form Attributes

| Attribute | Description |
|------|------|
| `data-api-base` | CRUD API base path (e.g. `/api/rbac/users`) |
| `data-group-api` | Group dropdown data source (optional, for `<select name="groupId">`) |
| `data-edit-id` | Record ID in edit mode (auto-set by the edit button) |
| `data-int-field` | Marks the field as integer type; auto `parseInt` on submit |
| `data-array-type="string"` | Marks the field as a comma-separated string array |
| `data-array-type="int"` | Marks the field as a comma-separated integer array |
| `data-tag-field` | Marks the field as linked to a Tag Selector component |
| `data-tag-items-key` | The key of the Tag list in the response data |

> **Field Validation:** `submitDictForm` parses the `errors` object returned by the API and automatically displays errors in the corresponding `<div class="invalid-feedback" data-field="xxx">` elements.

### Delete Button

```html
<button class="btn btn-sm btn-outline-danger"
        data-delete-api="/api/rbac/users"
        data-delete-id="1"
        data-delete-name="admin">
    Delete
</button>
```

| Attribute | Description |
|------|------|
| `data-delete-api` | Delete API base path (auto-appends `/{id}`) |
| `data-delete-id` | Record ID |
| `data-delete-name` | Record name (used in the confirmation prompt) |

### Confirm Dialog

Supports both declarative and programmatic approaches.

**Declarative:**

```html
<button data-confirm="Are you sure you want to perform this action?"
        data-confirm-title="Confirm Action"
        data-confirm-type="danger"
        data-confirm-ok="Confirm Delete">
    Dangerous Action
</button>
```

**Programmatic (Promise API):**

```js
const ok = await showConfirm({
    message: 'Are you sure you want to delete "admin"? This action cannot be undone.',
    title: 'Confirm Delete',
    type: 'danger',
    okText: 'Confirm Delete'
});
if (ok) { /* perform delete */ }
```

| Parameter | Type | Default | Description |
|------|------|--------|------|
| `message` | String | - | Confirmation message (required) |
| `title` | String | `Confirm Action` | Dialog title |
| `type` | String | `warning` | `warning` / `danger` / `success` / `info` |
| `okText` | String | `OK` | Confirm button text |
| `cancelText` | String | `Cancel` | Cancel button text |

### Search Bar

```html
<div class="row g-2" data-table-search="/admin/rbac/users" data-debounce-ms="800">
    <div class="col-md-5">
        <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input class="form-control" data-search-field="keyword" type="text"
                   placeholder="Search..." th:value="${keyword}">
            <button class="btn btn-outline-secondary" data-search-clear type="button">
                <i class="bi bi-x-lg"></i>
            </button>
            <button class="btn btn-primary" data-search-submit type="button">Search</button>
        </div>
    </div>
    <div class="col-md-3">
        <select class="form-select" data-search-field="status">
            <option value="">All Statuses</option>
            <option value="1">Active</option>
            <option value="0">Disabled</option>
        </select>
    </div>
</div>
```

| Attribute | Default | Description |
|------|--------|------|
| `data-table-search` | - | Search base URL |
| `data-debounce-ms` | `800` | Text input debounce delay in milliseconds |
| `data-search-field` | - | Query parameter name (e.g. `keyword`, `status`) |
| `data-search-clear` | - | Marks the clear button |
| `data-search-submit` | - | Marks the search button |

### Field Type Declaration

In `dict-modal-form` forms, declare field types via the following attributes:

| Attribute | Effect |
|------|------|
| `data-int-field` | Auto `parseInt(value, 10)` on submit |
| `data-array-type="string"` | Comma-separated → string array `["a", "b"]` |
| `data-array-type="int"` | Comma-separated → integer array `[1, 2, 3]` |
| (no attribute) | Keeps the raw string value |

### Tag Selector

Tag Selector is a multi-select tag component for associating data (e.g. related words/grammar in examples):

```html
<input name="relatedWords" type="hidden" value=""
       data-tag-field="relatedWords"
       data-tag-items-key="relatedWordItems"
       data-array-type="int">

<div class="tag-selector" data-field="relatedWords" data-api="/api/dict/words"></div>
```

| Attribute | Description |
|------|------|
| `data-tag-field` | Hidden field name, linked to `.tag-selector[data-field="..."]` |
| `data-tag-items-key` | The key of the Tag list in the response data in edit mode |

---

## Frontend JavaScript API

### Global Functions

| Function | Description |
|------|------|
| `apiCall(url, options)` | Unified API call that automatically handles the `ApiResult` response format |
| `showConfirm(opts)` | Global confirm dialog, returns `Promise<boolean>` |
| `showToast(message, type)` | Toast notification; `type` is `'success'` or `'error'` |
| `showPageError(message, type, duration)` | Page-level error banner; `type` is `'error'` / `'warning'` |
| `hidePageError()` | Hides the error banner |

### apiCall Usage Example

```js
// Auto-unwraps the data field from ApiResult
const users = await apiCall('/api/rbac/users?page=0&size=20');

// Sends a POST request
const result = await apiCall('/api/rbac/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'test', password: '123456' })
});
```

> `apiCall` automatically handles the `{ "code": "200", "data": ... }` format: returns `data` on success, shows an error banner and throws on failure.

### Template Variables

The following variables are automatically injected by `AdminControllerAdvice` into all pages annotated with `@AdminPage`:

| Variable | Type | Description |
|------|------|------|
| `menuGroups` | `List<MenuGroup>` | Sidebar menu tree (permission-filtered) |
| `currentPath` | `String` | Current request path |
| `userAuthorities` | `Set<String>` | Current user's authority set |
| `adminProps` | `AdminProperties` | Admin console configuration properties |

---

## Dependencies

| Dependency | Description |
|------|------|
| `spring-whale-webmvc` | Security framework + JWT authentication |
| `spring-boot-starter-thymeleaf` | Thymeleaf template engine |
| `thymeleaf-layout-dialect` | Layout decorator |
| `bootstrap 5.3.3` | UI framework |
| `bootstrap-icons 1.11.3` | Icon library |
| `lombok` | Code simplification (provided scope) |