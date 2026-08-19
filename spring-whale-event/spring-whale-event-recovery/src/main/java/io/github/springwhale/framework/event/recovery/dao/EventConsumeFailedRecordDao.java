package io.github.springwhale.framework.event.recovery.dao;

import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based DAO for {@code event_consume_failed_record} table.
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

    private final DataSource dataSource;

    public void save(EventConsumeFailedRecord entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, entity.getId());
            ps.setString(2, entity.getMessageId());
            ps.setString(3, entity.getSource());
            ps.setString(4, entity.getBusinessName());
            ps.setString(5, entity.getListenerName());
            ps.setString(6, entity.getAuthenticationContext());
            ps.setString(7, entity.getTopic());
            ps.setString(8, entity.getRawMessage());
            ps.setString(9, entity.getStatus().name());
            ps.setInt(10, entity.getRetryCount());
            ps.setObject(11, entity.getNextRetryTime());
            ps.setString(12, entity.getErrorStack());
            ps.setObject(13, entity.getCreateTime());
            ps.setObject(14, entity.getUpdateTime());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save failed record: " + entity.getId(), e);
        }
    }

    public Optional<EventConsumeFailedRecord> findById(String id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find record by id: " + id, e);
        }
        return Optional.empty();
    }

    public List<EventConsumeFailedRecord> findPendingRetry(EventConsumeStatus status, LocalDateTime before,
                                                           int limit) {
        List<EventConsumeFailedRecord> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PENDING_RETRY)) {
            ps.setString(1, status.name());
            ps.setObject(2, before);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find pending retry records", e);
        }
        return result;
    }

    public int updateRetryResult(String id, EventConsumeStatus status, LocalDateTime nextRetryTime,
                                 String errorStack) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_RETRY_RESULT)) {
            ps.setString(1, status.name());
            ps.setObject(2, nextRetryTime);
            ps.setString(3, errorStack);
            ps.setObject(4, LocalDateTime.now());
            ps.setString(5, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update retry result: " + id, e);
        }
    }

    public int casTransitionStatus(String id, EventConsumeStatus expectedStatus, EventConsumeStatus newStatus) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(CAS_TRANSITION)) {
            ps.setString(1, newStatus.name());
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, id);
            ps.setString(4, expectedStatus.name());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to CAS transition status: " + id, e);
        }
    }

    public List<EventConsumeFailedRecord> findTerminalRecords(List<EventConsumeStatus> statuses,
                                                              LocalDateTime cutoff, int limit) {
        if (statuses.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(statuses.size(), "?"));
        String sql = String.format(SELECT_TERMINAL, placeholders);
        List<EventConsumeFailedRecord> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            for (EventConsumeStatus status : statuses) {
                ps.setString(idx++, status.name());
            }
            ps.setObject(idx++, cutoff);
            ps.setInt(idx, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find terminal records", e);
        }
        return result;
    }

    public void deleteAll(List<EventConsumeFailedRecord> entities) {
        if (entities.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", Collections.nCopies(entities.size(), "?"));
        String sql = String.format(DELETE_BY_IDS, placeholders);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < entities.size(); i++) {
                ps.setString(i + 1, entities.get(i).getId());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete records", e);
        }
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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, EventConsumeStatus.PENDING_RETRY.name());
            ps.setObject(2, LocalDateTime.now());
            ps.setObject(3, LocalDateTime.now());
            int idx = 4;
            for (Object param : params) {
                ps.setObject(idx++, param);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch reset records to pending retry", e);
        }
    }

    private EventConsumeFailedRecord mapRow(ResultSet rs) throws SQLException {
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