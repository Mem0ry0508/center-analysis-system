package org.center.repository;

import org.center.model.Alert;
import org.center.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlertRepository implements IRepository<Alert, Long> {

    @Override
    public Alert save(Alert entity) {
        String sql = "INSERT INTO alerts (alert_type, severity, due_date, priority_tier, source_table, "
                + "source_id, related_person_id, trigger_reason, message, status, assigned_to) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setAlertId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save Alert 失敗", e);
        }
    }

    @Override
    public Optional<Alert> findById(Long id) {
        String sql = "SELECT * FROM alerts WHERE alert_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById Alert 失敗", e);
        }
    }

    /**
     * 供 B 的 MaxHeap 讀入未結案警示，依 severity 建堆輸出優先序。
     */
    public List<Alert> findOpenAlerts() {
        String sql = "SELECT * FROM alerts WHERE status = 'open'";
        List<Alert> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findOpenAlerts 失敗", e);
        }
    }

    @Override
    public List<Alert> findAll() {
        String sql = "SELECT * FROM alerts";
        List<Alert> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll Alert 失敗", e);
        }
    }

    @Override
    public boolean update(Alert entity) {
        String sql = "UPDATE alerts SET alert_type=?, severity=?, due_date=?, priority_tier=?, source_table=?, "
                + "source_id=?, related_person_id=?, trigger_reason=?, message=?, status=?, assigned_to=? "
                + "WHERE alert_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, entity);
            ps.setLong(12, entity.getAlertId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update Alert 失敗", e);
        }
    }

    /**
     * alerts 採軟刪除：狀態改為 resolved 並記錄 resolved_at，而非刪除警示歷程。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE alerts SET status = 'resolved', resolved_at = CURRENT_TIMESTAMP WHERE alert_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById Alert 失敗", e);
        }
    }

    private void bind(PreparedStatement ps, Alert a) throws SQLException {
        ps.setString(1, a.getAlertType());
        ps.setInt(2, a.getSeverity());
        ps.setObject(3, a.getDueDate());
        ps.setString(4, a.getPriorityTier());
        ps.setString(5, a.getSourceTable());
        if (a.getSourceId() == null) {
            ps.setNull(6, Types.BIGINT);
        } else {
            ps.setLong(6, a.getSourceId());
        }
        if (a.getRelatedPersonId() == null) {
            ps.setNull(7, Types.BIGINT);
        } else {
            ps.setLong(7, a.getRelatedPersonId());
        }
        ps.setString(8, a.getTriggerReason());
        ps.setString(9, a.getMessage());
        ps.setString(10, a.getStatus() == null ? "open" : a.getStatus());
        if (a.getAssignedTo() == null) {
            ps.setNull(11, Types.BIGINT);
        } else {
            ps.setLong(11, a.getAssignedTo());
        }
    }

    private Alert mapRow(ResultSet rs) throws SQLException {
        Alert a = new Alert();
        a.setAlertId(rs.getLong("alert_id"));
        a.setAlertType(rs.getString("alert_type"));
        a.setSeverity(rs.getInt("severity"));
        a.setDueDate(toLocalDate(rs.getDate("due_date")));
        a.setPriorityTier(rs.getString("priority_tier"));
        a.setSourceTable(rs.getString("source_table"));
        long sourceId = rs.getLong("source_id");
        a.setSourceId(rs.wasNull() ? null : sourceId);
        long relatedPersonId = rs.getLong("related_person_id");
        a.setRelatedPersonId(rs.wasNull() ? null : relatedPersonId);
        a.setTriggerReason(rs.getString("trigger_reason"));
        a.setMessage(rs.getString("message"));
        a.setStatus(rs.getString("status"));
        long assignedTo = rs.getLong("assigned_to");
        a.setAssignedTo(rs.wasNull() ? null : assignedTo);
        a.setReadAt(toLocalDateTime(rs.getTimestamp("read_at")));
        a.setSnoozedUntil(toLocalDateTime(rs.getTimestamp("snoozed_until")));
        a.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        a.setResolvedAt(toLocalDateTime(rs.getTimestamp("resolved_at")));
        return a;
    }

    private LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
