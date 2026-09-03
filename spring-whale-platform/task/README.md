# Spring Whale Platform · Task

Batch task module: a generic engine for long-running, resumable batch processing —
think "regenerate audio for 10,000 words", "re-index all documents", "import a large file".
You only implement the per-item logic; progress tracking, pause/resume, failure retry,
concurrency, and restart recovery are handled for you.

This module is a **platform reference implementation** of spring-whale. It provides a
ready-to-use batch task baseline for small and medium-sized projects, while demonstrating
the framework's conventions (auto-configuration, `@AdminPage` console pages, Flyway
migrations shipped inside jars). Business systems may also use it as a reference for
building their own task infrastructure.

## Modules

| Module                          | Description                                                                                                                                   |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `spring-whale-platform-task`    | Task core: entities, `TaskHandler` SPI, execution engine (virtual threads), REST API, admin page controller                                   |
| `spring-whale-platform-task-ui` | Resource-only jar (no Java code): Thymeleaf template, i18n messages (zh/en/ja), and the Flyway script that seeds admin menus into `rbac_menu` |

The task core depends on task-ui, so adding **one** dependency is enough —
the console page and its menu are available out of the box:

```xml

<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-task</artifactId>
</dependency>
```

Activated via Spring Boot auto-configuration; all beans are `@ConditionalOnMissingBean`
and can be replaced. Tables are created automatically by Flyway.

## Implementing a Task

Applications provide one Spring bean per task type by implementing the `TaskHandler` SPI:

```java

@Component
public class WordAudioTaskHandler implements TaskHandler {

    @Override
    public String getTaskType() {
        return "WORD_AUDIO";
    }

    /** Enumerate the item keys to process — each key uniquely identifies one item. */
    @Override
    public List<String> enumerateItems(Map<String, Object> params) {
        return wordRepository.findAll().stream()
                .map(w -> "word:" + w.getId())
                .toList();
    }

    /** Process one item; return false (or throw) to mark it FAILED. */
    @Override
    public boolean processItem(String itemKey, Map<String, Object> params) throws Exception {
        int wordId = Integer.parseInt(itemKey.split(":")[1]);
        wordService.regenerateAudio(wordId);
        return true;
    }
}
```

Optional hooks:

- `processBatch(...)` — override for chunk-level efficiency (e.g. your own thread pool);
  the default implementation simply loops over `processItem`.
- `beforeStart(params)` / `afterComplete(params)` / `onCancel(params)` — lifecycle callbacks.
- `BatchProgressCallback` — passed to `processBatch`: report per-item results
  (`onItemResult`), check cooperative cancellation (`isCancelled`), and force-persist
  progress (`flush`).

## Task Lifecycle

```
create ──► PENDING ──start──► RUNNING ──┬─ all items done ──► COMPLETED
                                         ├─ pause ──► PAUSED ──resume──► RUNNING (breakpoint)
                                         ├─ cancel ─► CANCELLED
                                         └─ fatal error ─► FAILED
```

- **Create** (`POST /api/tasks`): the handler enumerates all item keys, which are
  persisted as `task_batch_item` rows up front (batch insert). Only **one active task
  per task type** is allowed — creating while a non-terminal task exists returns the
  existing task.
- **Start / Resume**: execution is submitted to the engine **after transaction commit**,
  so the running state is always durable before processing begins.
- **Pause / Cancel**: cooperative — in-flight items finish, but no new items are picked up.
  The engine checks the database status every 100 items and honors `isCancelled()`.
- **Resume (breakpoint)**: the engine only loads `PENDING` items in pages of 500 —
  succeeded/failed items are never reprocessed.
- **Retry failed** (`POST /api/tasks/{id}/retry-failed`): FAILED items are reset to
  PENDING (retry count incremented) and processing restarts immediately.
- **Restart recovery**: on application startup (`ApplicationReadyEvent`), tasks left in
  RUNNING by a crash/shutdown are marked PAUSED and can be resumed manually —
  progress already persisted is not lost.

## Execution Engine

`TaskExecutionEngine` (internal to the module) provides:

- **Virtual threads**: each task runs on `Executors.newVirtualThreadPerTaskExecutor()`;
  when `concurrency > 1`, each 500-item page is split into chunks processed in parallel
  (default concurrency: 4, configurable per task via `concurrency` in the create request).
- **Batched progress persistence**: item results accumulate in memory and are flushed
  in bulk at an adaptive interval (`clamp(total/50, 20, 100)` items) or when a page
  completes — each flush is a short independent transaction, so progress survives a
  crash mid-run and the admin page can poll it.
- **Housekeeping**: on completion/cancellation, SUCCESS and SKIPPED item rows are deleted
  automatically to keep the table small; FAILED rows are retained for inspection and retry.
- **Progress & ETA**: the API computes a 0–100 progress percentage and a rough
  estimated-remaining-time based on elapsed time and completed ratio.

## Admin Console

- Page: `/admin/task/batch` (`TaskPageController`, `@AdminPage`), with task-type and
  status filters, pagination and sorting.
- Features: create task (type + optional JSON params), start / pause / resume / cancel,
  progress bar with status badge, detail dialog (params, error message, failed item list
  with retry counts), and one-click "Retry Failed".
- Menu is seeded automatically (System → Tasks → Batch Tasks) into `rbac_menu`
  by the task-ui Flyway script; page labels support Chinese / English / Japanese.

## REST API

Base path: `/api/tasks` (authentication required by default).

| Method & Path                             | Description                                                               |
|-------------------------------------------|---------------------------------------------------------------------------|
| `POST /api/tasks`                         | Create a task (`taskType`, optional `params` map, optional `concurrency`) |
| `GET /api/tasks`                          | Paged task list (`page`, `size`, `sort`)                                  |
| `GET /api/tasks/by-status?status=RUNNING` | Filter by status                                                          |
| `GET /api/tasks/by-type?type=WORD_AUDIO`  | Filter by task type                                                       |
| `GET /api/tasks/{id}`                     | Task detail (progress, counts, ETA)                                       |
| `POST /api/tasks/{id}/start`              | Start a pending task                                                      |
| `POST /api/tasks/{id}/pause`              | Pause a running task                                                      |
| `POST /api/tasks/{id}/resume`             | Resume a paused task from the breakpoint                                  |
| `POST /api/tasks/{id}/cancel`             | Cancel a task                                                             |
| `POST /api/tasks/{id}/retry-failed`       | Reset FAILED items to PENDING and restart                                 |
| `GET /api/tasks/{id}/failed-items`        | List failed items (item key, error, retry count)                          |
| `DELETE /api/tasks/{id}`                  | Delete a terminal task (COMPLETED / CANCELLED / FAILED)                   |

## Tables

| Table             | Description                                                                                                               |
|-------------------|---------------------------------------------------------------------------------------------------------------------------|
| `task_batch`      | Task header: type, status, counters (total/success/fail/skipped), JSON params, error message, concurrency, start/end time |
| `task_batch_item` | Per-item state: `taskId`, `itemKey`, status (PENDING/SUCCESS/FAILED/SKIPPED), error message, retry count                  |

Both tables extend the framework's `BaseEntity` (audit fields, optimistic locking,
logical deletion) and are created automatically by Flyway migration scripts.
