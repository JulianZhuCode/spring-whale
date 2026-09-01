# spring-whale-thymeleaf

Spring Whale フレームワークの **Admin Console UI モジュール**。Spring Boot Auto-Configuration + Thymeleaf + Bootstrap 5 上に構築され、ビジネスモジュール向けのプラグ可能な管理ダッシュボードフレームワークを提供します。

---

## 目次

- [クイックスタート](#クイックスタート)
- [コア機能](#コア機能)
- [アーキテクチャ](#アーキテクチャ)
- [設定](#設定)
- [SPI 拡張ガイド](#spi-拡張ガイド)
  - [メニュー登録（AdminMenuProvider）](#メニュー登録adminmenuprovider)
  - [セキュリティ設定（SecurityConfigProvider）](#セキュリティ設定securityconfigprovider)
- [テンプレート開発ガイド](#テンプレート開発ガイド)
  - [ページレイアウト](#ページレイアウト)
  - [データテーブル](#データテーブル)
  - [CRUD モーダル](#crud-モーダル)
  - [削除ボタン](#削除ボタン)
  - [確認ダイアログ](#確認ダイアログ)
  - [検索バー](#検索バー)
  - [フィールドタイプ宣言](#フィールドタイプ宣言)
  - [タグセレクター](#タグセレクター)
- [フロントエンド JavaScript API](#フロントエンド-javascript-api)
- [依存関係](#依存関係)

---

## クイックスタート

### 1. 依存関係の追加

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-thymeleaf</artifactId>
</dependency>
```

### 2. 設定（オプション）

```yaml
spring:
  whale:
    thymeleaf:
      admin:
        brand-name: マイアプリ
        short-name: MY
        copyright: マイカンパニー
        version: 1.0.0
```

### 3. アプリケーションの起動

`http://localhost:8080/admin/login` にアクセスして管理コンソールを開きます。

---

## コア機能

| 機能 | 説明 |
|------|------|
| ログインページ | JWT Cookie 認証、診断ヒント付き（トークンなし / トークン無効 / 認証必要） |
| ダッシュボード | モジュール統計カード、登録済み全メニューモジュールを自動表示 |
| サイドバーメニュー | 折りたたみ/展開可能、多階層グループ、アイコン、権限フィルタリング、i18n 対応 |
| パンくずリスト | 現在のパスに基づいて自動生成 |
| データテーブル | ページネーション、ソート、検索、Thymeleaf フラグメントで再利用可能 |
| CRUD モーダル | `data-*` 属性による宣言的駆動、JS 不要 |
| グローバル確認ダイアログ | Promise API、`async/await` 対応 |
| 削除ボタン | 宣言的確認 + API 呼び出し + 自動リフレッシュ |
| トースト通知 | 成功/エラーメッセージ |
| ページレベルエラーバナー | API 呼び出し失敗時に自動表示 |
| エラーページ | 403/404/500 の親切な HTML エラーページ |
| i18n 国際化 | 簡体字中国語、英語、日本語に対応 |
| 権限認識 UI | メニューとボタンがユーザー権限に基づいて自動表示/非表示 |

---

## アーキテクチャ

### パッケージ構造

```
thymeleaf/
├── autoconfigure/     Spring Boot 自動設定エントリ + 設定プロパティ (AdminProperties)
├── controller/        コントローラー + @AdminPage マーカーアノテーション
├── menu/              SPI メニューインターフェース + モデル
└── security/          セキュリティ設定 SPI 実装
```

### デザインパターン

| パターン | 説明 |
|------|------|
| **SPI 拡張** | ビジネスモジュールが `AdminMenuProvider` / `SecurityConfigProvider` を実装して自動登録 |
| **カスタムアノテーション + Advice** | `@AdminPage` マーカー + `@ControllerAdvice(annotations = ...)` で正確なグローバル属性注入 |
| **デコレータパターン** | `layout.html` が `layout:decorate` でページレイアウトを統一 |
| **data-\* 属性駆動** | フロントエンド CRUD 動作を HTML 属性で宣言、JS 不要 |

### セキュリティ設定チェーン

`spring-whale-webmvc` モジュールの `SecurityAutoConfiguration` が `SecurityFilterChain` を構築し、各 `SecurityConfigProvider` が SPI 経由で参加します：

```
SecurityAutoConfiguration (spring-whale-webmvc)
  └── securityFilterChain()
      ├── collectPermitAllUrls()       ← 各プロバイダーの許可 URL を収集
      │   └── ThymeleafSecurityConfigProvider.getPermitAllUrls()
      │       └── /admin/login, /admin/css/**, /admin/js/**, ...
      ├── HttpSecurity 基本設定
      └── applyCustomConfigurations()
          └── ThymeleafSecurityConfigProvider.configure()
              └── http.exceptionHandling(entryPoint)  ← AuthenticationEntryPoint を登録
```

---

## 設定

### AdminProperties

| プロパティ | デフォルト | 説明 |
|------|--------|------|
| `spring.whale.thymeleaf.admin.brand-name` | `Spring Whale` | ページタイトルとログインページに表示されるブランド名 |
| `spring.whale.thymeleaf.admin.short-name` | `SW Admin` | サイドバーに表示される短縮名 |
| `spring.whale.thymeleaf.admin.copyright` | `Spring Whale Framework` | フッターに表示される著作権情報 |
| `spring.whale.thymeleaf.admin.version` | `0.0.2` | ダッシュボードに表示されるバージョン文字列 |

---

## SPI 拡張ガイド

### メニュー登録（AdminMenuProvider）

ビジネスモジュールが `AdminMenuProvider` インターフェースを実装し、Spring Bean として登録すると、自動的にサイドバーにメニューが表示されます：

```java
@Component
public class RbacMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
            // グループメニュー（コンテナとして機能、URL なし）
            MenuItem.group("rbac", "RBAC", "menu.rbac", "shield", 10),

            // リーフメニュー（URL あり、parentKey でグループに関連付け）
            MenuItem.leaf("rbac-users", "rbac", "ユーザー管理", "menu.rbac.user_management",
                    "/admin/rbac/users", "people", "rbac:user:read", 1),

            MenuItem.leaf("rbac-roles", "rbac", "ロール管理", "menu.rbac.role_management",
                    "/admin/rbac/roles", "person-badge", "rbac:role:read", 2),

            MenuItem.leaf("rbac-groups", "rbac", "グループ管理", "menu.rbac.group_management",
                    "/admin/rbac/groups", "diagram-3", "rbac:group:read", 3)
        );
    }

    @Override
    public int getOrder() {
        return 10;  // 小さい数字ほど前に表示
    }
}
```

#### MenuItem ファクトリメソッド

| メソッド | 説明 |
|------|------|
| `MenuItem.group(key, label, icon, sort)` | 基本グループ（i18n なし） |
| `MenuItem.group(key, label, labelI18nKey, icon, sort)` | i18n 付きグループ |
| `MenuItem.leaf(key, parentKey, label, url, sort)` | 基本リーフメニュー（アイコン/権限なし） |
| `MenuItem.leaf(key, parentKey, label, url, icon, sort)` | アイコン付きリーフメニュー |
| `MenuItem.leaf(key, parentKey, label, url, icon, permission, sort)` | 権限付きリーフメニュー |
| `MenuItem.leaf(key, parentKey, label, labelI18nKey, url, icon, permission, sort)` | フル機能リーフメニュー（全オプション） |

> **権限に関する注意：** `permission` を設定すると、その権限または `*` ワイルドカード権限を持つユーザーのみにメニュー項目が表示されます。

### セキュリティ設定（SecurityConfigProvider）

デフォルトでは、`/admin/login` と静的リソースパスは既にホワイトリストに登録されています。拡張するには：

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
        return 200;  // RBAC (100) の後に実行
    }
}
```

---

## テンプレート開発ガイド

### ページレイアウト

すべての管理ページは `layout.html` をデコレータテンプレートとして使用します。**ページの Controller には `@AdminPage` アノテーションが必要**です。これにより、メニューツリー、ユーザー権限、現在のパスなどのグローバル変数が自動注入されます：

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
    <!-- ページコンテンツ -->
</main>
</html>
```

### データテーブル

`data-table` Thymeleaf フラグメントを使用し、ヘッダーと行フラグメントでカスタムコンテンツを注入します：

```html
<th:block th:replace="~{admin/fragments/data-table :: table(
    title='ユーザー管理',
    description='システムユーザーの管理',
    createUrl='/admin/rbac/users',
    createPermission='rbac:user:create',
    items=${page.content},
    emptyMessage='ユーザーデータがありません',
    baseUrl='/admin/rbac/users',
    page=${page},
    colspan=6,
    headers=~{:: #user-headers},
    rows=~{:: #user-rows}
)}"></th:block>

<th:block id="user-headers" th:fragment="user-headers">
    <th>ユーザー名</th>
    <th>メール</th>
    <th>グループ</th>
    <th>ステータス</th>
    <th>操作</th>
</th:block>

<th:block id="user-rows" th:fragment="user-rows">
    <td th:text="${item.username}">admin</td>
    <td th:text="${item.email}">admin@example.com</td>
    <td th:text="${item.groupName}">デフォルトグループ</td>
    <td>
        <span class="badge bg-success" th:if="${item.status == 1}">有効</span>
        <span class="badge bg-secondary" th:unless="${item.status == 1}">無効</span>
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

#### テーブルパラメータ

| パラメータ | 型 | デフォルト | 説明 |
|------|------|--------|------|
| `title` | String | - | ページタイトル |
| `description` | String | - | ページ説明 |
| `createUrl` | String | - | 新規作成ボタンの URL、空の場合はボタン非表示 |
| `createPermission` | String | - | 新規作成ボタンに必要な権限、null は制限なし |
| `items` | List | - | データリスト |
| `emptyMessage` | String | - | データがない場合のメッセージ |
| `baseUrl` | String | - | ページネーションとソートに使用するベース URL |
| `page` | Page | - | Spring Data Page オブジェクト |
| `colspan` | int | - | データなし行の列結合数 |
| `headers` | Fragment | - | ヘッダーフラグメント |
| `rows` | Fragment | - | 行フラグメント |
| `filter` | Fragment | - | 検索バーフラグメント（オプション） |
| `sortableId` | String | `'true'` | ID ソート列を表示するかどうか、`'false'` で非表示 |

> **モーダルベースの新規作成：** 新規作成ボタンでページ遷移ではなくモーダルを開くには、Controller の Model に `createModal` 変数をモーダル ID として設定します。データテーブルフラグメントがこの変数を自動検出し、モーダルトリガーボタンに切り替えます。

### CRUD モーダル

`data-*` 属性による宣言的駆動、JS 不要：

```html
<!-- 新規作成/編集ボタン -->
<button class="btn btn-primary" data-modal="userModal">+ 新規</button>
<button class="btn btn-sm btn-outline-primary"
        data-modal="userModal" th:attr="data-edit-id=${item.id}">編集</button>

<!-- モーダル -->
<div class="modal fade" id="userModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">ユーザー</h5>
                <button class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form class="dict-modal-form" data-api-base="/api/rbac/users"
                      data-group-api="/api/rbac/groups?page=0&size=1000">
                    <div class="mb-3">
                        <label class="form-label">ユーザー名</label>
                        <input class="form-control" name="username" type="text" required>
                        <div class="invalid-feedback" data-field="username"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">グループ</label>
                        <select class="form-select" name="groupId" data-int-field>
                            <option value="">選択してください</option>
                        </select>
                        <div class="invalid-feedback" data-field="groupId"></div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">ステータス</label>
                        <select class="form-select" name="status" data-int-field>
                            <option value="1">有効</option>
                            <option value="0">無効</option>
                        </select>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" data-bs-dismiss="modal">キャンセル</button>
                <button class="btn btn-primary modal-submit">保存</button>
            </div>
        </div>
    </div>
</div>
```

#### モーダルフォーム属性

| 属性 | 説明 |
|------|------|
| `data-api-base` | CRUD API ベースパス（例：`/api/rbac/users`） |
| `data-group-api` | グループドロップダウンデータソース（オプション、`<select name="groupId">` 用） |
| `data-edit-id` | 編集モードのレコード ID（編集ボタンが自動設定） |
| `data-int-field` | フィールドを整数型としてマーク、送信時に自動 `parseInt` |
| `data-array-type="string"` | フィールドをカンマ区切り文字列配列としてマーク |
| `data-array-type="int"` | フィールドをカンマ区切り整数配列としてマーク |
| `data-tag-field` | フィールドをタグセレクターコンポーネントに関連付け |
| `data-tag-items-key` | レスポンスデータ内のタグリストのキー |

> **フィールドバリデーション：** `submitDictForm` は API が返す `errors` オブジェクトを解析し、対応する `<div class="invalid-feedback" data-field="xxx">` 要素に自動的にエラーを表示します。

### 削除ボタン

```html
<button class="btn btn-sm btn-outline-danger"
        data-delete-api="/api/rbac/users"
        data-delete-id="1"
        data-delete-name="admin">
    削除
</button>
```

| 属性 | 説明 |
|------|------|
| `data-delete-api` | 削除 API ベースパス（自動的に `/{id}` を追加） |
| `data-delete-id` | レコード ID |
| `data-delete-name` | レコード名（確認プロンプトで使用） |

### 確認ダイアログ

宣言的アプローチとプログラマティックアプローチの両方をサポートします。

**宣言的：**

```html
<button data-confirm="この操作を実行してもよろしいですか？"
        data-confirm-title="操作確認"
        data-confirm-type="danger"
        data-confirm-ok="削除確認">
    危険な操作
</button>
```

**プログラマティック（Promise API）：**

```js
const ok = await showConfirm({
    message: '「admin」を削除してもよろしいですか？この操作は取り消せません。',
    title: '削除確認',
    type: 'danger',
    okText: '削除確認'
});
if (ok) { /* 削除を実行 */ }
```

| パラメータ | 型 | デフォルト | 説明 |
|------|------|--------|------|
| `message` | String | - | 確認メッセージ（必須） |
| `title` | String | `操作確認` | ダイアログタイトル |
| `type` | String | `warning` | `warning` / `danger` / `success` / `info` |
| `okText` | String | `OK` | 確認ボタンのテキスト |
| `cancelText` | String | `キャンセル` | キャンセルボタンのテキスト |

### 検索バー

```html
<div class="row g-2" data-table-search="/admin/rbac/users" data-debounce-ms="800">
    <div class="col-md-5">
        <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input class="form-control" data-search-field="keyword" type="text"
                   placeholder="検索..." th:value="${keyword}">
            <button class="btn btn-outline-secondary" data-search-clear type="button">
                <i class="bi bi-x-lg"></i>
            </button>
            <button class="btn btn-primary" data-search-submit type="button">検索</button>
        </div>
    </div>
    <div class="col-md-3">
        <select class="form-select" data-search-field="status">
            <option value="">すべてのステータス</option>
            <option value="1">有効</option>
            <option value="0">無効</option>
        </select>
    </div>
</div>
```

| 属性 | デフォルト | 説明 |
|------|--------|------|
| `data-table-search` | - | 検索ベース URL |
| `data-debounce-ms` | `800` | テキスト入力のデバウンス遅延（ミリ秒） |
| `data-search-field` | - | クエリパラメータ名（例：`keyword`、`status`） |
| `data-search-clear` | - | クリアボタンをマーク |
| `data-search-submit` | - | 検索ボタンをマーク |

### フィールドタイプ宣言

`dict-modal-form` フォーム内で、以下の属性を使用してフィールドタイプを宣言します：

| 属性 | 効果 |
|------|------|
| `data-int-field` | 送信時に自動 `parseInt(value, 10)` |
| `data-array-type="string"` | カンマ区切り → 文字列配列 `["a", "b"]` |
| `data-array-type="int"` | カンマ区切り → 整数配列 `[1, 2, 3]` |
| （属性なし） | 生の文字列値を保持 |

### タグセレクター

タグセレクターは、データを関連付けるための複数選択タグコンポーネントです（例：例文の関連単語/文法）：

```html
<input name="relatedWords" type="hidden" value=""
       data-tag-field="relatedWords"
       data-tag-items-key="relatedWordItems"
       data-array-type="int">

<div class="tag-selector" data-field="relatedWords" data-api="/api/dict/words"></div>
```

| 属性 | 説明 |
|------|------|
| `data-tag-field` | 隠しフィールド名、`.tag-selector[data-field="..."]` に関連付け |
| `data-tag-items-key` | 編集モード時のレスポンスデータ内のタグリストのキー |

---

## フロントエンド JavaScript API

### グローバル関数

| 関数 | 説明 |
|------|------|
| `apiCall(url, options)` | 統一 API 呼び出し、`ApiResult` レスポンス形式を自動処理 |
| `showConfirm(opts)` | グローバル確認ダイアログ、`Promise<boolean>` を返す |
| `showToast(message, type)` | トースト通知、`type` は `'success'` または `'error'` |
| `showPageError(message, type, duration)` | ページレベルエラーバナー、`type` は `'error'` / `'warning'` |
| `hidePageError()` | エラーバナーを非表示 |

### apiCall 使用例

```js
// ApiResult の data フィールドを自動展開
const users = await apiCall('/api/rbac/users?page=0&size=20');

// POST リクエストを送信
const result = await apiCall('/api/rbac/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'test', password: '123456' })
});
```

> `apiCall` は `{ "code": "200", "data": ... }` 形式を自動処理し、成功時は `data` を返し、失敗時はエラーバナーを表示して例外をスローします。

### テンプレート変数

以下の変数は `AdminControllerAdvice` によって `@AdminPage` アノテーションが付いたすべてのページに自動注入されます：

| 変数 | 型 | 説明 |
|------|------|------|
| `menuGroups` | `List<MenuGroup>` | サイドバーメニューツリー（権限フィルタリング済み） |
| `currentPath` | `String` | 現在のリクエストパス |
| `userAuthorities` | `Set<String>` | 現在のユーザー権限セット |
| `adminProps` | `AdminProperties` | 管理コンソール設定プロパティ |

---

## 依存関係

| 依存関係 | 説明 |
|------|------|
| `spring-whale-webmvc` | セキュリティフレームワーク + JWT 認証 |
| `spring-boot-starter-thymeleaf` | Thymeleaf テンプレートエンジン |
| `thymeleaf-layout-dialect` | レイアウトデコレータ |
| `bootstrap 5.3.3` | UI フレームワーク |
| `bootstrap-icons 1.11.3` | アイコンライブラリ |
| `lombok` | コード簡略化（provided scope） |