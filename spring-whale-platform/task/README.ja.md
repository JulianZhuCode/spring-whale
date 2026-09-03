# Spring Whale Platform · Task（バッチタスク）

バッチタスクモジュール：長時間実行され、中断点から再開可能なバッチ処理のための汎用エンジンです。
例：「1万件の単語の音声を再生成」「ドキュメント全体のインデックス再構築」「大規模ファイルのインポート」。
業務側は1件ずつの処理ロジックを実装するだけで、進捗管理、一時停止/再開、失敗リトライ、並列実行、
再起動後の復旧はフレームワークが処理します。

本モジュールは spring-whale の**プラットフォーム参考実装**です。中規模・小規模プロジェクト向けに
そのまま使えるバッチタスクのベースラインを提供すると同時に、フレームワークの規約
（自動構成、`@AdminPage` 管理画面、jar に同梱される Flyway マイグレーション）の使い方を示すサンプル
としても機能します。業務システムではその構造を参考に独自のタスク基盤を作ることもできます。

## モジュール構成

| モジュール                           | 説明                                                                                               |
|---------------------------------|--------------------------------------------------------------------------------------------------|
| `spring-whale-platform-task`    | タスクコア：エンティティ、`TaskHandler` SPI、実行エンジン（仮想スレッド）、REST API、管理画面コントローラ                                |
| `spring-whale-platform-task-ui` | リソース専用 jar（Java コードなし）：Thymeleaf テンプレート、i18n メッセージ（中/英/日）、管理メニューを `rbac_menu` に投入する Flyway スクリプト |

task コアは task-ui に依存しているため、依存関係は**1つ追加するだけ**で、管理画面とメニューが
オートインで利用できます。

```xml

<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-task</artifactId>
</dependency>
```

Spring Boot の自動構成で有効化され、すべての Bean に `@ConditionalOnMissingBean` が付いているため
自由に差し替え可能です。テーブルは Flyway により自動作成されます。

## タスクの実装

アプリケーションはタスク種別ごとに `TaskHandler` SPI を実装した Spring Bean を1つ提供します。

```java

@Component
public class WordAudioTaskHandler implements TaskHandler {

    @Override
    public String getTaskType() {
        return "WORD_AUDIO";
    }

    /** 処理対象の item key を列挙する。各 key は1件のデータを一意に識別する */
    @Override
    public List<String> enumerateItems(Map<String, Object> params) {
        return wordRepository.findAll().stream()
                .map(w -> "word:" + w.getId())
                .toList();
    }

    /** 1件を処理する。false（または例外）で FAILED としてマークされる */
    @Override
    public boolean processItem(String itemKey, Map<String, Object> params) throws Exception {
        int wordId = Integer.parseInt(itemKey.split(":")[1]);
        wordService.regenerateAudio(wordId);
        return true;
    }
}
```

任意の拡張ポイント：

- `processBatch(...)`：チャンク単位の効率化が必要な場合にオーバーライド（独自スレッドプールなど）。
  デフォルト実装は `processItem` をループするだけです。
- `beforeStart(params)` / `afterComplete(params)` / `onCancel(params)`：ライフサイクルコールバック。
- `BatchProgressCallback`：`processBatch` に渡されるコールバック。1件ずつの結果報告（`onItemResult`）、
  協調的キャンセル確認（`isCancelled`）、進捗の強制保存（`flush`）が行えます。

## タスクのライフサイクル

```
create ──► PENDING ──start──► RUNNING ──┬─ 全件完了 ──► COMPLETED
                                         ├─ pause ──► PAUSED ──resume──► RUNNING（中断点から再開）
                                         ├─ cancel ─► CANCELLED
                                         └─ 致命的エラー ─► FAILED
```

- **作成**（`POST /api/tasks`）：ハンドラが全 item key を列挙し、`task_batch_item` 行として
  一括保存されます。同一タスク種別で**活動中のタスクは1つだけ**許可され、終了していないタスクが
  存在する場合は作成リクエストは既存タスクを返します。
- **開始 / 再開**：実行は**トランザクションのコミット後**にエンジンへ投入されます。これにより
  処理開始前に実行状態が確実に永続化されます。
- **一時停止 / キャンセル**：協調式です。処理中の item は完了しますが、新しい item は取得されません。
  エンジンは100件ごとにデータベースの状態を確認し、`isCancelled()` にも反応します。
- **中断点からの再開**：エンジンは1ページ500件でページング読み込みし、**PENDING の item のみ**
  を読み込みます。成功/失敗済みの item が再処理されることはありません。
- **失敗リトライ**（`POST /api/tasks/{id}/retry-failed`）：FAILED の item を PENDING にリセット
  （リトライ回数を加算）し、直ちに処理を再開します。
- **再起動時の復旧**：アプリ起動時（`ApplicationReadyEvent`）、クラッシュ/停止で RUNNING のまま
  残ったタスクは PAUSED に変更され、手動で再開できます。保存済みの進捗は失われません。

## 実行エンジン

モジュール内部の `TaskExecutionEngine` が提供します。

- **仮想スレッド**：各タスクは `newVirtualThreadPerTaskExecutor` 上で実行されます。
  `concurrency > 1` の場合、500件のページを並列度に応じてチャンク分割し並列処理します
  （デフォルト並列度4。タスク作成時に指定可能）。
- **進捗の一括保存**：item の結果はメモリに蓄積され、適応的な間隔
  （`clamp(総数/50, 20, 100)` 件）またはページ完了時に一括保存されます。各保存は独立した
  短いトランザクションで行われ、実行中のクラッシュでも進捗が失われず、管理画面から最新の進捗を
  ポーリングできます。
- **自動クリーンアップ**：タスク完了/キャンセル時に SUCCESS・SKIPPED の item 行を自動削除し
  テーブルの肥大化を防ぎます。FAILED 行は確認とリトライのため保持されます。
- **進捗と残り時間**：API は0〜100の進捗率と、経過時間と完了率から概算の残り時間を計算します。

## 管理コンソール

- 画面：`/admin/task/batch`（`TaskPageController`、`@AdminPage`）。タスク種別・ステータスでの
  フィルタ、ページネーション、ソートに対応。
- 機能：タスク作成（種別 + 任意の JSON パラメータ）、開始/一時停止/再開/キャンセル、
  プログレスバーとステータスバッジ、詳細ダイアログ（パラメータ、エラーメッセージ、失敗 item 一覧
  とリトライ回数）、ワンクリックの「失敗項目をリトライ」。
- メニューは task-ui の Flyway スクリプトにより自動投入されます（システム → タスク → バッチタスク）。
  画面の文言は中国語 / 英語 / 日本語に対応しています。

## REST API

ベースパス：`/api/tasks`（デフォルトでログイン認証が必要）。

| メソッドとパス                                   | 説明                                               |
|-------------------------------------------|--------------------------------------------------|
| `POST /api/tasks`                         | タスク作成（`taskType`、任意の `params`、任意の `concurrency`） |
| `GET /api/tasks`                          | ページング一覧（`page`、`size`、`sort`）                    |
| `GET /api/tasks/by-status?status=RUNNING` | ステータスで絞り込み                                       |
| `GET /api/tasks/by-type?type=WORD_AUDIO`  | タスク種別で絞り込み                                       |
| `GET /api/tasks/{id}`                     | タスク詳細（進捗、集計、残り時間）                                |
| `POST /api/tasks/{id}/start`              | 待機中タスクを開始                                        |
| `POST /api/tasks/{id}/pause`              | 実行中タスクを一時停止                                      |
| `POST /api/tasks/{id}/resume`             | 一時停止タスクを中断点から再開                                  |
| `POST /api/tasks/{id}/cancel`             | タスクをキャンセル                                        |
| `POST /api/tasks/{id}/retry-failed`       | 失敗 item を PENDING にリセットして再実行                     |
| `GET /api/tasks/{id}/failed-items`        | 失敗 item 一覧（item key、エラー、リトライ回数）                  |
| `DELETE /api/tasks/{id}`                  | 終了状態のタスクを削除（完了 / キャンセル / 失敗）                     |

## テーブル

| テーブル              | 説明                                                                                |
|-------------------|-----------------------------------------------------------------------------------|
| `task_batch`      | タスク本体：種別、ステータス、集計（総数/成功/失敗/スキップ）、JSON パラメータ、エラーメッセージ、並列度、開始/終了時刻                  |
| `task_batch_item` | item の状態：`taskId`、`itemKey`、ステータス（PENDING/SUCCESS/FAILED/SKIPPED）、エラーメッセージ、リトライ回数 |

両テーブルはフレームワークの `BaseEntity`（監査フィールド、楽観ロック、論理削除）を継承し、
Flyway マイグレーションスクリプトにより自動作成されます。
