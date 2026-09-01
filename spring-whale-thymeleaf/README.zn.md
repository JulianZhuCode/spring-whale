# spring-whale-thymeleaf

Spring Whale 框架的 **Admin Console UI 模块**，基于 Spring Boot Auto-Configuration + Thymeleaf + Bootstrap 5 构建，为业务模块提供可插拔的管理后台框架。

---

## 目录

- [快速开始](#快速开始)
- [核心功能](#核心功能)
- [架构设计](#架构设计)
- [配置说明](#配置说明)
- [SPI 扩展指南](#spi-扩展指南)
  - [注册菜单（AdminMenuProvider）](#注册菜单adminmenuprovider)
  - [安全配置（SecurityConfigProvider）](#安全配置securityconfigprovider)
- [模板开发指南](#模板开发指南)
  - [页面布局](#页面布局)
  - [数据表格](#数据表格)
  - [CRUD 模态框](#crud-模态框)
  - [删除按钮](#删除按钮)
  - [确认对话框](#确认对话框)
  - [搜索栏](#搜索栏)
  - [字段类型声明](#字段类型声明)
  - [Tag Selector](#tag-selector)
- [前端 JavaScript API](#前端-javascript-api)
- [依赖](#依赖)

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-thymeleaf</artifactId>
</dependency>
```

### 2. 配置（可选）

```yaml
spring:
  whale:
    thymeleaf:
      admin:
        brand-name: 我的应用
        short-name: MY
        copyright: 我的公司
        version: 1.0.0
```

### 3. 启动应用

访问 `http://localhost:8080/admin/login` 进入管理后台。

---

## 核心功能

| 功能 | 说明 |
|------|------|
| 登录页面 | JWT Cookie 认证，支持诊断提示（无令牌 / 令牌无效 / 需认证） |
| 仪表盘 | 模块统计卡片，自动展示所有注册的菜单模块 |
| 侧边栏菜单 | 可折叠/展开，支持多级分组、图标、权限过滤、i18n |
| 面包屑导航 | 自动根据当前路径生成 |
| 数据表格 | 分页、排序、搜索，通过 Thymeleaf 片段复用 |
| CRUD 模态框 | 通过 `data-*` 属性声明式驱动，无需编写 JS |
| 全局确认对话框 | Promise API，支持 `async/await` |
| 删除按钮 | 声明式确认 + API 调用 + 自动刷新 |
| Toast 通知 | 成功/错误提示 |
| 页面级错误横幅 | API 调用失败时自动展示 |
| 错误页面 | 403/404/500 友好 HTML 错误页 |
| i18n 国际化 | 支持简体中文、英文、日文 |
| 权限感知 UI | 菜单和按钮根据用户权限自动显示/隐藏 |

---

## 架构设计

### 包结构

```
thymeleaf/
├── autoconfigure/     Spring Boot 自动配置入口 + 配置属性 (AdminProperties)
├── controller/        控制器 + @AdminPage 标记注解
├── menu/              SPI 菜单接口 + 模型
└── security/          安全配置 SPI 实现
```

### 设计模式

| 模式 | 说明 |
|------|------|
| **SPI 扩展** | 业务模块实现 `AdminMenuProvider` / `SecurityConfigProvider` 接口即可自动注册 |
| **自定义注解 + Advice** | `@AdminPage` 标记注解 + `@ControllerAdvice(annotations = ...)` 精确注入全局属性 |
| **装饰器模式** | `layout.html` 通过 `layout:decorate` 统一页面布局 |
| **data-\* 属性驱动** | 前端 CRUD 行为通过 HTML 属性声明，无需编写 JS |

### 安全配置链路

`spring-whale-webmvc` 模块的 `SecurityAutoConfiguration` 负责构建 `SecurityFilterChain`，各 `SecurityConfigProvider` 通过 SPI 参与配置：

```
SecurityAutoConfiguration (spring-whale-webmvc)
  └── securityFilterChain()
      ├── collectPermitAllUrls()       ← 收集各 Provider 白名单
      │   └── ThymeleafSecurityConfigProvider.getPermitAllUrls()
      │       └── /admin/login, /admin/css/**, /admin/js/**, ...
      ├── HttpSecurity 基础配置
      └── applyCustomConfigurations()
          └── ThymeleafSecurityConfigProvider.configure()
              └── http.exceptionHandling(entryPoint)  ← 注册 AuthenticationEntryPoint
```

---

## 配置说明

### AdminProperties

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `spring.whale.thymeleaf.admin.brand-name` | `Spring Whale` | 品牌全名，显示在页面标题和登录页 |
| `spring.whale.thymeleaf.admin.short-name` | `SW Admin` | 短名称，显示在侧边栏 |
| `spring.whale.thymeleaf.admin.copyright` | `Spring Whale Framework` | 版权信息，显示在页脚 |
| `spring.whale.thymeleaf.admin.version` | `0.0.2` | 版本号，显示在仪表盘 |

---

## SPI 扩展指南

### 注册菜单（AdminMenuProvider）

业务模块实现 `AdminMenuProvider` 接口并注册为 Spring Bean，即可自动在侧边栏显示菜单：

```java
@Component
public class RbacMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
            // 分组菜单（作为容器，无 URL）
            MenuItem.group("rbac", "RBAC", "menu.rbac", "shield", 10),

            // 叶子菜单（有 URL，通过 parentKey 关联到分组）
            MenuItem.leaf("rbac-users", "rbac", "用户管理", "menu.rbac.user_management",
                    "/admin/rbac/users", "people", "rbac:user:read", 1),

            MenuItem.leaf("rbac-roles", "rbac", "角色管理", "menu.rbac.role_management",
                    "/admin/rbac/roles", "person-badge", "rbac:role:read", 2),

            MenuItem.leaf("rbac-groups", "rbac", "分组管理", "menu.rbac.group_management",
                    "/admin/rbac/groups", "diagram-3", "rbac:group:read", 3)
        );
    }

    @Override
    public int getOrder() {
        return 10;  // 数值越小越靠前
    }
}
```

#### MenuItem 工厂方法

| 方法 | 说明 |
|------|------|
| `MenuItem.group(key, label, icon, sort)` | 基础分组（无 i18n） |
| `MenuItem.group(key, label, labelI18nKey, icon, sort)` | 带 i18n 分组 |
| `MenuItem.leaf(key, parentKey, label, url, sort)` | 基础叶子菜单（无图标/权限） |
| `MenuItem.leaf(key, parentKey, label, url, icon, sort)` | 带图标叶子菜单 |
| `MenuItem.leaf(key, parentKey, label, url, icon, permission, sort)` | 带权限叶子菜单 |
| `MenuItem.leaf(key, parentKey, label, labelI18nKey, url, icon, permission, sort)` | 完整叶子菜单（全部特性） |

> **权限说明：** 设置 `permission` 后，菜单仅对持有该权限或 `*` 通配符权限的用户可见。

### 安全配置（SecurityConfigProvider）

默认情况下，`/admin/login` 和静态资源路径已加入白名单。如需扩展：

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
        return 200;  // 在 RBAC (100) 之后执行
    }
}
```

---

## 模板开发指南

### 页面布局

所有 Admin 页面使用 `layout.html` 作为装饰器模板。**页面 Controller 必须标注 `@AdminPage` 注解**，才能自动注入菜单树、用户权限和当前路径等全局变量：

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
    <!-- 页面内容 -->
</main>
</html>
```

### 数据表格

使用 `data-table` Thymeleaf 片段，通过表头和行片段注入自定义内容：

```html
<th:block th:replace="~{admin/fragments/data-table :: table(
    title='用户管理',
    description='管理系统用户',
    createUrl='/admin/rbac/users',
    createPermission='rbac:user:create',
    items=${page.content},
    emptyMessage='暂无用户数据',
    baseUrl='/admin/rbac/users',
    page=${page},
    colspan=6,
    headers=~{:: #user-headers},
    rows=~{:: #user-rows}
)}"></th:block>

<th:block id="user-headers" th:fragment="user-headers">
    <th>用户名</th>
    <th>邮箱</th>
    <th>分组</th>
    <th>状态</th>
    <th>操作</th>
</th:block>

<th:block id="user-rows" th:fragment="user-rows">
    <td th:text="${item.username}">admin</td>
    <td th:text="${item.email}">admin@example.com</td>
    <td th:text="${item.groupName}">默认分组</td>
    <td>
        <span class="badge bg-success" th:if="${item.status == 1}">启用</span>
        <span class="badge bg-secondary" th:unless="${item.status == 1}">禁用</span>
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

#### 表格参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | String | - | 页面标题 |
| `description` | String | - | 页面描述 |
| `createUrl` | String | - | 新建按钮的 URL，为空则不显示新建按钮 |
| `createPermission` | String | - | 新建按钮所需权限，null 表示无限制 |
| `items` | List | - | 数据列表 |
| `emptyMessage` | String | - | 无数据时的提示文字 |
| `baseUrl` | String | - | 基础 URL，用于分页和排序 |
| `page` | Page | - | Spring Data Page 对象 |
| `colspan` | int | - | 无数据时单元格合并数 |
| `headers` | Fragment | - | 表头片段 |
| `rows` | Fragment | - | 行片段 |
| `filter` | Fragment | - | 搜索栏片段（可选） |
| `sortableId` | String | `'true'` | 是否显示 ID 排序列，传 `'false'` 隐藏 |

> **模态框新建：** 如需点击新建按钮打开模态框而非跳转页面，在 Controller 的 Model 中设置 `createModal` 变量为模态框 ID。数据表格片段会自动检测该变量，切换为模态框触发按钮。

### CRUD 模态框

通过 `data-*` 属性声明式驱动，无需编写任何 JS：

```html
<!-- 新建/编辑按钮 -->
<button class="btn btn-primary" data-modal="userModal">+ 新建</button>
<button class="btn btn-sm btn-outline-primary"
        data-modal="userModal" th:attr="data-edit-id=${item.id}">编辑</button>

<!-- 模态框 -->
<div class="modal fade" id="userModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">用户</h5>
                <button class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form class="dict-modal-form" data-api-base="/api/rbac/users"
                      data-group-api="/api/rbac/groups?page=0&size=1000">
                    <div class="mb-3">
                        <label class="form-label">用户名</label>
                        <input class="form-control" name="username" type="text" required>
                        <div class="invalid-feedback" data-field="username"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">分组</label>
                        <select class="form-select" name="groupId" data-int-field>
                            <option value="">请选择</option>
                        </select>
                        <div class="invalid-feedback" data-field="groupId"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">状态</label>
                        <select class="form-select" name="status" data-int-field>
                            <option value="1">启用</option>
                            <option value="0">禁用</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                <button class="btn btn-primary modal-submit">保存</button>
            </div>
        </div>
    </div>
</div>
```

#### 模态框表单属性

| 属性 | 说明 |
|------|------|
| `data-api-base` | CRUD API 基础路径（如 `/api/rbac/users`） |
| `data-group-api` | 分组下拉框数据源（可选，用于 `<select name="groupId">`） |
| `data-edit-id` | 编辑模式下的记录 ID（由编辑按钮自动设置） |
| `data-int-field` | 标记字段为整数类型，提交时自动 `parseInt` |
| `data-array-type="string"` | 标记字段为字符串数组，逗号分隔 |
| `data-array-type="int"` | 标记字段为整数数组，逗号分隔 |
| `data-tag-field` | 标记字段关联 Tag Selector 组件 |
| `data-tag-items-key` | Tag Selector 数据在响应中的 key |

> **字段校验：** `submitDictForm` 会解析 API 返回的 `errors` 对象，自动显示到对应字段的 `<div class="invalid-feedback" data-field="xxx">` 中。

### 删除按钮

```html
<button class="btn btn-sm btn-outline-danger"
        data-delete-api="/api/rbac/users"
        data-delete-id="1"
        data-delete-name="admin">
    删除
</button>
```

| 属性 | 说明 |
|------|------|
| `data-delete-api` | 删除 API 基础路径（自动拼接 `/{id}`） |
| `data-delete-id` | 记录 ID |
| `data-delete-name` | 记录名称（用于确认提示） |

### 确认对话框

支持声明式和编程式两种方式。

**声明式：**

```html
<button data-confirm="确定要执行此操作吗？"
        data-confirm-title="操作确认"
        data-confirm-type="danger"
        data-confirm-ok="确认删除">
    危险操作
</button>
```

**编程式（Promise API）：**

```js
const ok = await showConfirm({
    message: '确认删除「admin」？此操作不可撤销。',
    title: '删除确认',
    type: 'danger',
    okText: '确认删除'
});
if (ok) { /* 执行删除 */ }
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `message` | String | - | 确认消息（必填） |
| `title` | String | `确认操作` | 标题 |
| `type` | String | `warning` | `warning` / `danger` / `success` / `info` |
| `okText` | String | `确定` | 确认按钮文字 |
| `cancelText` | String | `取消` | 取消按钮文字 |

### 搜索栏

```html
<div class="row g-2" data-table-search="/admin/rbac/users" data-debounce-ms="800">
    <div class="col-md-5">
        <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input class="form-control" data-search-field="keyword" type="text"
                   placeholder="搜索..." th:value="${keyword}">
            <button class="btn btn-outline-secondary" data-search-clear type="button">
                <i class="bi bi-x-lg"></i>
            </button>
            <button class="btn btn-primary" data-search-submit type="button">搜索</button>
        </div>
    </div>
    <div class="col-md-3">
        <select class="form-select" data-search-field="status">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
        </select>
    </div>
</div>
```

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `data-table-search` | - | 搜索基础 URL |
| `data-debounce-ms` | `800` | 文本输入防抖延迟（毫秒） |
| `data-search-field` | - | 查询参数名（如 `keyword`、`status`） |
| `data-search-clear` | - | 标记清除按钮 |
| `data-search-submit` | - | 标记搜索按钮 |

### 字段类型声明

在 `dict-modal-form` 表单中，通过以下属性声明字段类型：

| 属性 | 效果 |
|------|------|
| `data-int-field` | 提交时自动 `parseInt(value, 10)` |
| `data-array-type="string"` | 逗号分隔 → 字符串数组 `["a", "b"]` |
| `data-array-type="int"` | 逗号分隔 → 整数数组 `[1, 2, 3]` |
| （无属性） | 保持原始字符串值 |

### Tag Selector

Tag Selector 是一个多选标签组件，用于关联数据（如示例的关联单词/语法）：

```html
<input name="relatedWords" type="hidden" value=""
       data-tag-field="relatedWords"
       data-tag-items-key="relatedWordItems"
       data-array-type="int">

<div class="tag-selector" data-field="relatedWords" data-api="/api/dict/words"></div>
```

| 属性 | 说明 |
|------|------|
| `data-tag-field` | 隐藏字段名，关联 `.tag-selector[data-field="..."]` |
| `data-tag-items-key` | 编辑模式下，响应数据中 Tag 列表的 key |

---

## 前端 JavaScript API

### 全局函数

| 函数 | 说明 |
|------|------|
| `apiCall(url, options)` | 统一 API 调用，自动处理 `ApiResult` 响应格式 |
| `showConfirm(opts)` | 全局确认对话框，返回 `Promise<boolean>` |
| `showToast(message, type)` | Toast 通知，`type` 为 `'success'` 或 `'error'` |
| `showPageError(message, type, duration)` | 页面级错误横幅，`type` 为 `'error'` / `'warning'` |
| `hidePageError()` | 隐藏错误横幅 |

### apiCall 使用示例

```js
// 自动解包 ApiResult 的 data 字段
const users = await apiCall('/api/rbac/users?page=0&size=20');

// 发送 POST 请求
const result = await apiCall('/api/rbac/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'test', password: '123456' })
});
```

> `apiCall` 自动处理 `{ "code": "200", "data": ... }` 格式，成功时返回 `data`，失败时显示错误横幅并抛出异常。

### 模板可用变量

以下变量由 `AdminControllerAdvice` 自动注入到所有 `@AdminPage` 标注的页面：

| 变量 | 类型 | 说明 |
|------|------|------|
| `menuGroups` | `List<MenuGroup>` | 侧边栏菜单树（已过滤权限） |
| `currentPath` | `String` | 当前请求路径 |
| `userAuthorities` | `Set<String>` | 当前用户权限集合 |
| `adminProps` | `AdminProperties` | 管理后台配置属性 |

---

## 依赖

| 依赖 | 说明 |
|------|------|
| `spring-whale-webmvc` | 安全框架 + JWT 认证 |
| `spring-boot-starter-thymeleaf` | Thymeleaf 模板引擎 |
| `thymeleaf-layout-dialect` | 布局装饰器 |
| `bootstrap 5.3.3` | UI 框架 |
| `bootstrap-icons 1.11.3` | 图标库 |
| `lombok` | 代码简化（provided scope） |