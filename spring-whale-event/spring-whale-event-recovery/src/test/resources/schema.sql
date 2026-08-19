CREATE TABLE IF NOT EXISTS event_consume_failed_record (
    id VARCHAR(255) PRIMARY KEY,
    message_id VARCHAR(255),
    source VARCHAR(255),
    business_name VARCHAR(255),
    listener_name VARCHAR(255),
    authentication_context TEXT,
    topic VARCHAR(255),
    raw_message TEXT,
    status VARCHAR(50),
    retry_count INT DEFAULT 0,
    next_retry_time TIMESTAMP,
    error_stack TEXT,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);