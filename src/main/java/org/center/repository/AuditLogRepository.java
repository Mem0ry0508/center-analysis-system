package org.center.repository;

import org.center.model.AuditLog;
import org.center.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * audit_logs 是稽核紀錄，需求文件明定「一般使用者不得修改稽核紀錄」，
 * 因此僅開放新增與查詢，update / deleteById 一律拋例外（append-only）。
 */
public class AuditLogRepository implements IRepository<AuditLog, Long> {

    @Override
    public AuditLog save(AuditLog entity) {
        String sql = "INSERT INTO audit_logs (actor_id, action, table_name, record_id, field_name, old_value, "
                + "new_value, reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entity.getActorId() == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, entity.getActorId());
            }
            ps.setString(2, entity.getAction());
            ps.setString(3, entity.getTableName());
            if (entity.getRecordId() == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, entity.getRecordId());
            }
            ps.setString(5, entity.getFieldName());
            ps.setString(6, entity.getOldValue());
            ps.setString(7, entity.getNewValue());
            ps.setString(8, entity.getReason());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setLogId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save AuditLog 失敗", e);
        }
    }

    @Override
    public Optional<AuditLog> findById(Long id) {
        String sql = "SELECT * FROM audit_logs WHERE log_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById AuditLog 失敗", e);
        }
    }

    public List<AuditLog> findByTableAndRecord(String tableName, Long recordId) {
        String sql = "SELECT * FROM audit_logs WHERE table_name = ? AND record_id = ? ORDER BY created_at";
        List<AuditLog> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setLong(2, recordId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findByTableAndRecord 失敗", e);
        }
    }

    @Override
    public List<AuditLog> findAll() {
        String sql = "SELECT * FROM audit_logs";
        List<AuditLog> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll AuditLog 失敗", e);
        }
    }

    @Override
    public boolean update(AuditLog entity) {
        throw new UnsupportedOperationException("audit_logs 為 append-only，不允許修改既有紀錄");
    }

    @Override
    public boolean deleteById(Long id) {
        throw new UnsupportedOperationException("audit_logs 為 append-only，不允許刪除既有紀錄");
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogId(rs.getLong("log_id"));
        long actorId = rs.getLong("actor_id");
        log.setActorId(rs.wasNull() ? null : actorId);
        log.setAction(rs.getString("action"));
        log.setTableName(rs.getString("table_name"));
        long recordId = rs.getLong("record_id");
        log.setRecordId(rs.wasNull() ? null : recordId);
        log.setFieldName(rs.getString("field_name"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setReason(rs.getString("reason"));
        log.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        return log;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
