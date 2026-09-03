# Spring Whale Platform · RBAC

軽量 RBAC モジュール：ユーザー、ロール、メニュー（権限）、部署（グループ）のオートイン実装を提供します。

本モジュールは spring-whale の**プラットフォーム参考実装**です。中規模・小規模プロジェクト向けにそのまま使える権限のベースラインを提供すると同時に、フレームワークの各 SPI（データスコープハンドラ、セキュリティ設定、管理メニュー、イベントリスナー）の標準的な実装方法を示すサンプルとしても機能します。業務システムでは本モジュールを依存に追加せず、その構造を参考にして独自の権限モデルを実装することもできます。

## モジュール構成

| モジュール | 説明 |
|---|---|
| `spring-whale-platform-rbac` | RBAC コア：エンティティ、REST API、Spring Security 連携、データスコープ連動 |
| `spring-whale-platform-rbac-ui` | 管理コンソール画面：Thymeleaf テンプレート + メニュー自動登録（rbac および thymeleaf モジュールに依存） |

両モジュールとも Spring Boot の自動構成（`AutoConfiguration.imports`）で有効化され、すべての Bean に `@ConditionalOnMissingBean` が付いているため、自由に差し替え可能です。

## クイックスタート

```xml
<!-- RBAC コア（REST API + セキュリティ連携） -->
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-rbac</artifactId>
</dependency>

<!-- 任意：管理コンソール画面（Thymeleaf） -->
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-rbac-ui</artifactId>
</dependency>
```

導入後、Flyway が自動でテーブルを作成し、初期データを投入します。

- ルート部署 `ROOT`、組み込みロール `SUPER_ADMIN`（すべての権限チェックをバイパス）
- 組み込み管理者：**admin / admin**（BCrypt 暗号化。本番公開前にパスワードを変更してください）
- rbac-ui を導入すると管理メニューも自動投入されます（システム → RBAC → ユーザー / ロール / メニュー / 組織管理）

## 権限モデル

```
ユーザー rbac_user ──< rbac_user_role >── ロール rbac_role ──< rbac_role_menu >── メニュー/権限 rbac_menu
                                            │
                                            ├──< rbac_role_dept >── 部署 rbac_group（カスタムデータスコープ）
                                            └── data_scope（データスコープ：自部署 / 自部署および配下 / カスタム…）
部署 rbac_group：parent_id とマテリアライズドパス path（例 /1/3/）によるツリー構造。
                 配下部署の検索は path の前方一致で行います
```

- **機能権限**：メニューは `DIRECTORY`（ディレクトリ）、`MENU`（ページメニュー）、`BUTTON`（ボタン/操作権限）の 3 種類です。権限識別子はメニューの `code`（例：`rbac:user`、`rbac:user:create`）です。
- **ロールコード**：任意項目で、設定すると Spring Security の `ROLE_<code>` が付与されます。`SUPER_ADMIN` ロールにはさらにワイルドカード権限 `*` が付与され、すべてのメニューとデータスコープを持ちます。
- **ログイン認証**：`POST /api/rbac/auth/login`。認証成功後に JWT を発行します（JWT の仕組みは webmvc モジュールが提供）。
- **ユーザー詳細**：`UserDetailsServiceImpl` が Spring Security の `UserDetailsService` を実装し、ロールとメニュー権限を一括クエリで取得します。結果はユーザー名ごとにキャッシュされます（`userDetails` キャッシュ）。

## データスコープ連携

本モジュールは database モジュールの `DataScopeHandler` SPI のデフォルト実装（`RBACDataScopeHandler`）です。これにより `@DataScope` アノテーションが RBAC の設定を直接読み取り、業務側は権限データの取得元を意識する必要がありません。

- **スキップ判定**：`SUPER_ADMIN` ユーザーはデータスコープフィルタをスキップします。また、RBAC 自身のテーブルはマルチテナント分離の対象外です。
- **部署範囲の解決**（ロールに設定された `data_scope` に基づく）：
    - `DEPT`：ユーザー自身の部署
    - `DEPT_AND_CHILD`：自部署 + すべての配下部署（マテリアライズドパスの前方一致検索）
    - `CUSTOM` / `AUTO`：`rbac_role_dept` でロールに明示的に関連付けられた部署。module 指定時は「ロール—メニュー」関連でさらに絞り込み
- **パフォーマンス**：解決結果は `dataScope` キャッシュ（TTL は database モジュールで設定）に格納され、権限変更時はドメインイベントで能動的に無効化されます。

| イベント | 無効化範囲 |
|---|---|
| `UserRoleChangedEvent` | 対象ユーザー |
| `RoleChangedEvent` | 当該ロールを持つすべてのユーザー |
| `GroupChangedEvent` | 当該部署および配下部署のユーザー + カスタムスコープが当該部署を参照しているロールのユーザー |

イベントは event モジュール経由で（トランザクションのコミット後に）発行され、ローカル / MQ どちらのモードでも無効化の動作は同一です。

### マイクロサービス構成：リモートデータスコープ解決

RBAC を独立した権限サービスとして配置する場合、下流サービスは Feign 経由でデータスコープをリモート解決できます。内部 API は**デフォルトで無効**で、権限サービス側で明示的に有効化する必要があります。

```yaml
spring.whale.database.datascope:
  expose-remote-api: true   # RBAC サービスでのみ有効化。本番ではゲートウェイ層でサービス間通信に制限することを推奨
```

有効化すると `/api/rbac/datascope/**`（skip / skip-tenant / resolve / キャッシュ無効化）が公開されます。契約は `DataScopeRemoteApi` インターフェースにより下流の Feign クライアントと一致することが保証されます。

## 管理コンソール（rbac-ui）

- `RbacMenuProvider` は thymeleaf モジュールの `AdminMenuProvider` SPI を実装しています。データベースからメニューを読み込み、現在のユーザーのロールでフィルタリングします（`SUPER_ADMIN` はすべて表示）。サイドバーには表示可能かつ有効なディレクトリ/メニューのみが表示され、BUTTON タイプは含まれません。
- `RbacPageController` は 4 つの管理画面（`/admin/rbac/users|roles|menus|groups`）を提供します。各画面は `@PreAuthorize` で保護され、ページネーション、キーワード検索、ソートに対応しています。
- 画面の文言は中国語 / 英語 / 日本語に対応し（`messages-rbac*.properties`）、メニュー名は i18n キーに対応しています。

## REST API 一覧

| 機能 | メソッドとパス |
|---|---|
| ログイン | `POST /api/rbac/auth/login` |
| ユーザー | `GET/POST /api/rbac/users`、`GET/PUT/DELETE /api/rbac/users/{id}` |
| ロール | `GET/POST /api/rbac/roles`、`GET/PUT/DELETE /api/rbac/roles/{id}` |
| メニュー | `GET/POST /api/rbac/menus`、`GET/PUT/DELETE /api/rbac/menus/{id}`、`GET /api/rbac/menus/tree`（現在のユーザーが権限を持つメニューツリー） |
| 部署 | `GET/POST /api/rbac/groups`、`GET/PUT/DELETE /api/rbac/groups/{id}`、`GET /api/rbac/groups/tree` |
| ロール-メニュー | `GET/POST/DELETE /api/rbac/roles/{roleId}/menus` |
| ロール-部署 | `GET/POST/DELETE /api/rbac/roles/{roleId}/depts` |
| ユーザー-ロール | `GET/POST/DELETE /api/rbac/users/{userId}/roles` |
| データスコープ（内部） | `GET /api/rbac/datascope/skip/{userId}` など。`expose-remote-api` の有効化が必要 |

認証関連 URL（`/api/rbac/auth/**`、`/api/rbac/public/**`）は `RbacSecurityConfigProvider` が webmvc モジュールの Security SPI を通じて一括でアクセス許可します。

## テーブル

| テーブル / ビュー | 説明 |
|---|---|
| `rbac_user` | ユーザー（`group_id` が部署。`@DeptIdField` によりデータスコープ対象） |
| `rbac_role` | ロール（`data_scope` 列にデータスコープ種別を保持） |
| `rbac_menu` | メニューと権限識別子（parent_id によるツリー構造） |
| `rbac_group` | 部署 / グループ（`@DeptIdScope`、マテリアライズドパス path） |
| `rbac_user_role` | ユーザー-ロール関連 |
| `rbac_role_menu` | ロール-メニュー関連 |
| `rbac_role_dept` | ロール-カスタム部署関連（CUSTOM データスコープ） |
| `rbac_user_role_scope_view` | 読み取り専用ビュー：ユーザー → ロール → データスコープ → カスタム部署。権限解決の一括照会用 |

すべてのテーブルはフレームワークの `BaseEntity`（監査フィールド、楽観ロック、論理削除）を継承し、Flyway マイグレーションスクリプトにより自動作成されます。
