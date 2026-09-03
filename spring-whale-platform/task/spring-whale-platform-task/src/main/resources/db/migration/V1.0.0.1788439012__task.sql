-- ================================================================
-- Initialize Task tables
-- ================================================================

-- 1. Task Batch
CREATE TABLE IF NOT EXISTS task_batch (
    id            SERIAL PRIMARY KEY,
    task_type     VARCHAR(100) NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    total_count   INTEGER      NOT NULL DEFAULT 0,
    success_count INTEGER      NOT NULL DEFAULT 0,
    fail_count    INTEGER      NOT NULL DEFAULT 0,
    skipped_count INTEGER      NOT NULL DEFAULT 0,
    params        TEXT,
    error_message VARCHAR(2000),
    start_time    TIMESTAMP,
    end_time      TIMESTAMP,
    create_time   TIMESTAMP,
    update_time   TIMESTAMP,
    create_by     INTEGER,
    update_by     INTEGER,
    version       INTEGER      NOT NULL DEFAULT 0,
    del_flag      INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_task_type       ON task_batch (task_type);
CREATE INDEX IF NOT EXISTS idx_task_status     ON task_batch (status);
CREATE INDEX IF NOT EXISTS idx_task_create_time ON task_batch (create_time);

-- 2. Task Batch Item
CREATE TABLE IF NOT EXISTS task_batch_item (
    id            SERIAL PRIMARY KEY,
    task_id       INTEGER      NOT NULL,
    item_key      VARCHAR(500) NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    error_message VARCHAR(2000),
    retry_count   INTEGER      NOT NULL DEFAULT 0,
    create_time   TIMESTAMP,
    update_time   TIMESTAMP,
    create_by     INTEGER,
    update_by     INTEGER,
    version       INTEGER      NOT NULL DEFAULT 0,
    del_flag      INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_item_task_id ON task_batch_item (task_id);
CREATE INDEX IF NOT EXISTS idx_item_status  ON task_batch_item (status);
CREATE INDEX IF NOT EXISTS idx_item_key     ON task_batch_item (item_key);