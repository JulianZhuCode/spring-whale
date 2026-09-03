# Spring Whale Platform · Task（批量任务）

批量任务模块：面向长时间运行、可断点续跑的批处理场景的通用引擎——
例如"为 1 万个单词重新生成音频"、"全量重建索引"、"大文件导入"。
业务方只需实现单条处理逻辑，进度跟踪、暂停/恢复、失败重试、并发执行、重启恢复全部由框架负责。

本模块是 spring-whale 的**平台参考实现**：面向中小项目提供开箱即用的批量任务基线，
同时演示框架的约定用法（自动装配、`@AdminPage` 后台页面、随 jar 分发的 Flyway 迁移）。
业务系统也可以参照它的结构自建任务基础设施。

## 模块组成

| 模块                              | 说明                                                                            |
|---------------------------------|-------------------------------------------------------------------------------|
| `spring-whale-platform-task`    | 任务核心：实体、`TaskHandler` SPI、执行引擎（虚拟线程）、REST API、后台页面控制器                         |
| `spring-whale-platform-task-ui` | 纯资源 jar（无 Java 代码）：Thymeleaf 模板、三语消息文件（中/英/日）、向 `rbac_menu` 写入后台菜单的 Flyway 脚本 |

task 核心依赖 task-ui，因此**只需引入一个依赖**，后台页面与菜单即开箱即用：

```xml

<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-platform-task</artifactId>
</dependency>
```

通过 Spring Boot 自动装配生效，所有 Bean 均带 `@ConditionalOnMissingBean`，可自由替换；
数据表由 Flyway 自动创建。

## 实现一个任务

业务方为每种任务类型提供一个实现 `TaskHandler` SPI 的 Spring Bean：

```java

@Component
public class WordAudioTaskHandler implements TaskHandler {

    @Override
    public String getTaskType() {
        return "WORD_AUDIO";
    }

    /** 枚举待处理的条目 key，每个 key 唯一标识一条数据 */
    @Override
    public List<String> enumerateItems(Map<String, Object> params) {
        return wordRepository.findAll().stream()
                .map(w -> "word:" + w.getId())
                .toList();
    }

    /** 处理单条；返回 false（或抛异常）标记为失败 */
    @Override
    public boolean processItem(String itemKey, Map<String, Object> params) throws Exception {
        int wordId = Integer.parseInt(itemKey.split(":")[1]);
        wordService.regenerateAudio(wordId);
        return true;
    }
}
```

可选扩展点：

- `processBatch(...)`：需要分块提效时覆写（如自定义线程池）；默认实现逐条调用 `processItem`。
- `beforeStart(params)` / `afterComplete(params)` / `onCancel(params)`：生命周期回调。
- `BatchProgressCallback`：传入 `processBatch` 的回调——上报单条结果（`onItemResult`）、
  协作式取消检查（`isCancelled`）、强制落盘进度（`flush`）。

## 任务生命周期

```
create ──► PENDING ──start──► RUNNING ──┬─ 全部条目完成 ──► COMPLETED
                                         ├─ pause ──► PAUSED ──resume──► RUNNING（断点续跑）
                                         ├─ cancel ─► CANCELLED
                                         └─ 致命异常 ─► FAILED
```

- **创建**（`POST /api/tasks`）：处理器先枚举全部条目 key，批量落库为
  `task_batch_item` 行。同一任务类型**只允许存在一个活动任务**——若已有未终态任务，
  创建请求直接返回现有任务。
- **启动 / 恢复**：执行在**事务提交后**才提交给引擎，保证处理开始前运行状态已持久化。
- **暂停 / 取消**：协作式——在途条目会执行完，但不再领取新条目。引擎每处理 100 条
  检查一次数据库状态，并响应 `isCancelled()`。
- **断点续跑**：引擎按每页 500 条分页加载，且**只加载 PENDING 条目**，成功/失败的
  条目绝不会重复处理。
- **失败重试**（`POST /api/tasks/{id}/retry-failed`）：FAILED 条目重置为 PENDING
  （重试次数 +1）并立即重新执行。
- **重启恢复**：应用启动时（`ApplicationReadyEvent`），因崩溃/停机停留在 RUNNING 的
  任务会被标记为 PAUSED，可手动恢复——已落盘的进度不丢失。

## 执行引擎

模块内部的 `TaskExecutionEngine` 提供：

- **虚拟线程**：每个任务运行在 `newVirtualThreadPerTaskExecutor` 上；`concurrency > 1`
  时，每页 500 条按并发数分块并行处理（默认并发 4，创建任务时可按任务指定）。
- **进度批量落盘**：条目结果在内存累积，按自适应间隔
  （`clamp(总数/50, 20, 100)` 条）或整页完成时批量写入，每次落盘是独立短事务——
  中途崩溃进度不丢，后台页面也可轮询到最新进度。
- **自动清理**：任务完成/取消后自动删除 SUCCESS、SKIPPED 的条目行以控制表体积；
  FAILED 行保留供排查与重试。
- **进度与预计剩余时间**：API 计算 0–100 进度百分比，并根据已用时间和完成比例
  粗略估算剩余时间。

## 管理后台

- 页面：`/admin/task/batch`（`TaskPageController`，`@AdminPage`），支持按任务类型、
  状态筛选，分页与排序。
- 功能：创建任务（类型 + 可选 JSON 参数）、开始/暂停/恢复/取消、进度条与状态徽标、
  详情弹窗（参数、错误信息、失败条目列表及重试次数）、一键"重试失败项"。
- 菜单由 task-ui 的 Flyway 脚本自动写入（系统 → 任务 → 批量任务）；
  页面文案支持中 / 英 / 日三语。

## REST API

基础路径：`/api/tasks`（默认需要登录认证）。

| 方法与路径                                     | 说明                                                   |
|-------------------------------------------|------------------------------------------------------|
| `POST /api/tasks`                         | 创建任务（`taskType`、可选 `params` 参数、可选 `concurrency` 并发数） |
| `GET /api/tasks`                          | 分页任务列表（`page`、`size`、`sort`）                         |
| `GET /api/tasks/by-status?status=RUNNING` | 按状态筛选                                                |
| `GET /api/tasks/by-type?type=WORD_AUDIO`  | 按任务类型筛选                                              |
| `GET /api/tasks/{id}`                     | 任务详情（进度、计数、预计剩余时间）                                   |
| `POST /api/tasks/{id}/start`              | 启动待执行任务                                              |
| `POST /api/tasks/{id}/pause`              | 暂停运行中的任务                                             |
| `POST /api/tasks/{id}/resume`             | 从断点恢复已暂停任务                                           |
| `POST /api/tasks/{id}/cancel`             | 取消任务                                                 |
| `POST /api/tasks/{id}/retry-failed`       | 失败条目重置为待执行并重新运行                                      |
| `GET /api/tasks/{id}/failed-items`        | 失败条目列表（条目 key、错误信息、重试次数）                             |
| `DELETE /api/tasks/{id}`                  | 删除终态任务（已完成 / 已取消 / 失败）                               |

## 数据表

| 表                 | 说明                                                                   |
|-------------------|----------------------------------------------------------------------|
| `task_batch`      | 任务主表：类型、状态、计数（总数/成功/失败/跳过）、JSON 参数、错误信息、并发数、开始/结束时间                  |
| `task_batch_item` | 条目状态：`taskId`、`itemKey`、状态（PENDING/SUCCESS/FAILED/SKIPPED）、错误信息、重试次数 |

两张表均继承框架 `BaseEntity`（审计字段、乐观锁、逻辑删除），由 Flyway 迁移脚本自动创建。
