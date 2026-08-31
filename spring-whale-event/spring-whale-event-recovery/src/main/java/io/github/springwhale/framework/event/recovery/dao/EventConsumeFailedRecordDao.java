package io.github.springwhale.framework.event.recovery.dao;

import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based DAO for {@code event_consume_failed_record} table, backed by Spring's {@link JdbcTemplate}.
 * <p>Uses {@code LIMIT} for row limiting, which is compatible with MySQL, PostgreSQL,
 * MariaDB, and H2. For SQL Server or Oracle, override this DAO with dialect-specific SQL.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class EventConsumeFailedRecordDao {

    private static final String INSERT_SQL = """
            INSERT INTO event_consume_failed_record
            (id, message_id, source, business_name, listener_name, authentication_context,
             topic, raw_message, status, retry_count, next_retry_time, error_stack, create_time, update_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID = """
            SELECT id, message_id, source, business_name, listener_name, authentication_context,
                   topic, raw_message, status, retry_count, next_retry_time, error_stack, create_time, update_time
            FROM event_consume_failed_record WHERE id = ?
            """;

    private static final String SELECT_PENDING_RETRY = """
            SELECT id, message_id, source, business_name, listener_name, authentication_context,
                   topic, raw_message, status, retry_count, next_retry_time, error_stack, create_time, update_time
            FROM event_consume_failed_record
            WHERE status = ? AND next_retry_time < ?
            ORDER BY next_retry_time ASC LIMIT ?
            """;

    private static final String UPDATE_RETRY_RESULT = """
            UPDATE event_consume_failed_record
            SET status = ?, next_retry_time = ?, error_stack = ?, update_time = ?
            WHERE id = ?
            """;

    private static final String CAS_TRANSITION = """
            UPDATE event_consume_failed_record
            SET retry_count = retry_count + 1, status = ?, update_time = ?
            WHERE id = ? AND status = ?
            """;

    private static final String SELECT_TERMINAL = """
            SELECT id, message_id, source, business_name, listener_name, authentication_context,
                   topic, raw_message, status, retry_count, next_retry_time, error_stack, create_time, update_time
            FROM event_consume_failed_record
            WHERE status IN (%s) AND create_time < ? LIMIT ?
            """;

    private static final String DELETE_BY_IDS = "DELETE FROM event_consume_failed_record WHERE id IN (%s)";

    private static final String RESET_TO_PENDING_RETRY = """
            UPDATE event_consume_failed_record
            SET status = ?, next_retry_time = ?, retry_count = 0, update_time = ?
            WHERE %s
            """;

    private static final String RESET_TO_PENDING_RETRY_KEEP_COUNT = """
            UPDATE event_consume_failed_record
            SET status = ?, next_retry_time = ?, update_time = ?
            WHERE %s
            """;

    private static final RowMapper<EventConsumeFailedRecord> ROW_MAPPER = new EventConsumeFailedRecordRowMapper();

    private final JdbcTemplate jdbcTemplate;

    public void save(EventConsumeFailedRecord entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        jdbcTemplate.update(INSERT_SQL,
                entity.getId(), entity.getMessageId(), entity.getSource(),
                entity.getBusinessName(), entity.getListenerName(), entity.getAuthenticationContext(),
                entity.getTopic(), entity.getRawMessage(), entity.getStatus().name(),
                entity.getRetryCount(), entity.getNextRetryTime(), entity.getErrorStack(),
                entity.getCreateTime(), entity.getUpdateTime());
    }

    public Optional<EventConsumeFailedRecord> findById(String id) {
        List<EventConsumeFailedRecord> results = jdbcTemplate.query(SELECT_BY_ID, ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<EventConsumeFailedRecord> findPendingRetry(EventConsumeStatus status, LocalDateTime before,
                                                           int limit) {
        return jdbcTemplate.query(SELECT_PENDING_RETRY, ROW_MAPPER, status.name(), before, limit);
    }

    public int updateRetryResult(String id, EventConsumeStatus status, LocalDateTime nextRetryTime,
                                 String errorStack) {
        return jdbcTemplate.update(UPDATE_RETRY_RESULT,
                status.name(), nextRetryTime, errorStack, LocalDateTime.now(), id);
    }

    public int casTransitionStatus(String id, EventConsumeStatus expectedStatus, EventConsumeStatus newStatus) {
        return jdbcTemplate.update(CAS_TRANSITION,
                newStatus.name(), LocalDateTime.now(), id, expectedStatus.name());
    }

    public List<EventConsumeFailedRecord> findTerminalRecords(List<EventConsumeStatus> statuses,
                                                              LocalDateTime cutoff, int limit) {
        if (statuses.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(statuses.size(), "?"));
        String sql = String.format(SELECT_TERMINAL, placeholders);

        List<Object> params = new ArrayList<>();
        for (EventConsumeStatus status : statuses) {
            params.add(status.name());
        }
        params.add(cutoff);
        params.add(limit);

        return jdbcTemplate.query(sql, ROW_MAPPER, params.toArray());
    }

    public void deleteAll(List<EventConsumeFailedRecord> entities) {
        if (entities.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", Collections.nCopies(entities.size(), "?"));
        String sql = String.format(DELETE_BY_IDS, placeholders);
        Object[] ids = entities.stream().map(EventConsumeFailedRecord::getId).toArray();
        jdbcTemplate.update(sql, ids);
    }

    public int batchResetToPendingRetry(List<String> ids, List<EventConsumeStatus> statuses,
                                        boolean resetRetryCount) {
        if ((ids == null || ids.isEmpty()) && (statuses == null || statuses.isEmpty())) {
            throw new IllegalArgumentException("At least one of ids or statuses must be provided");
        }

        StringBuilder whereClause = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (ids != null && !ids.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
            whereClause.append("id IN (").append(placeholders).append(")");
            params.addAll(ids);
        }

        if (statuses != null && !statuses.isEmpty()) {
            if (whereClause.length() > 0) {
                whereClause.append(" AND ");
            }
            String placeholders = String.join(",", Collections.nCopies(statuses.size(), "?"));
            whereClause.append("status IN (").append(placeholders).append(")");
            for (EventConsumeStatus status : statuses) {
                params.add(status.name());
            }
        }

        String template = resetRetryCount ? RESET_TO_PENDING_RETRY : RESET_TO_PENDING_RETRY_KEEP_COUNT;
        String sql = String.format(template, whereClause.toString());

        List<Object> allParams = new ArrayList<>();
        allParams.add(EventConsumeStatus.PENDING_RETRY.name());
        allParams.add(LocalDateTime.now());
        allParams.add(LocalDateTime.now());
        allParams.addAll(params);

        return jdbcTemplate.update(sql, allParams.toArray());
    }

    private static class EventConsumeFailedRecordRowMapper implements RowMapper<EventConsumeFailedRecord> {
        @Override
        public EventConsumeFailedRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventConsumeFailedRecord entity = new EventConsumeFailedRecord();
            entity.setId(rs.getString("id"));
            entity.setMessageId(rs.getString("message_id"));
            entity.setSource(rs.getString("source"));
            entity.setBusinessName(rs.getString("business_name"));
            entity.setListenerName(rs.getString("listener_name"));
            entity.setAuthenticationContext(rs.getString("authentication_context"));
            entity.setTopic(rs.getString("topic"));
            entity.setRawMessage(rs.getString("raw_message"));
            entity.setStatus(EventConsumeStatus.valueOf(rs.getString("status")));
            entity.setRetryCount(rs.getInt("retry_count"));
            entity.setNextRetryTime(rs.getObject("next_retry_time", LocalDateTime.class));
            entity.setErrorStack(rs.getString("error_stack"));
            entity.setCreateTime(rs.getObject("create_time", LocalDateTime.class));
            entity.setUpdateTime(rs.getObject("update_time", LocalDateTime.class));
            return entity;
        }
    }
}