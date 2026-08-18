CREATE TABLE IF NOT EXISTS event_consume_failed_record
(
    message_id             VARCHAR(64) NOT NULL,
    source                 VARCHAR(128),
    business_name          VARCHAR(128),
    listener_name          VARCHAR(128),
    authentication_context TEXT,
    topic                  VARCHAR(256),
    raw_message            TEXT,
    status                 VARCHAR(32) NOT NULL,
    retry_count            INTEGER DEFAULT 0,
    next_retry_time        TIMESTAMP,
    error_stack            TEXT,
    create_time            TIMESTAMP,
    update_time            TIMESTAMP,
    PRIMARY KEY (message_id)
);

CREATE INDEX IF NOT EXISTS idx_event_consume_failed_record_next_retry_time
    ON event_consume_failed_record (next_retry_time);