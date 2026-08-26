package org.center.repository;

import org.center.model.Enrollment;
import org.center.util.ConnectionManager;

import java.math.BigDecimal;
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

public class EnrollmentRepository implements IRepository<Enrollment, Long> {

    @Override
    public Enrollment save(Enrollment entity) {
        String sql = "INSERT INTO enrollments (person_id, course_id, registrar_id, amount, payment_type, "
                + "last_attendance_date, status, cancel_reason) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, entity.getPersonId());
            ps.setLong(2, entity.getCourseId());
            setNullableLong(ps, 3, entity.getRegistrarId());
            ps.setBigDecimal(4, entity.getAmount());
            ps.setString(5, entity.getPaymentType());
            ps.setObject(6, entity.getLastAttendanceDate());
            ps.setString(7, entity.getStatus() == null ? "contacted" : entity.getStatus());
            ps.setString(8, entity.getCancelReason());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setEnrollmentId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save Enrollment 失敗", e);
        }
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById Enrollment 失敗", e);
        }
    }

    public List<Enrollment> findByPersonId(Long personId) {
        String sql = "SELECT * FROM enrollments WHERE person_id = ?";
        List<Enrollment> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findByPersonId Enrollment 失敗", e);
        }
    }

    public List<Enrollment> findByCourseId(Long courseId) {
        String sql = "SELECT * FROM enrollments WHERE course_id = ?";
        List<Enrollment> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findByCourseId Enrollment 失敗", e);
        }
    }

    @Override
    public List<Enrollment> findAll() {
        String sql = "SELECT * FROM enrollments";
        List<Enrollment> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll Enrollment 失敗", e);
        }
    }

    @Override
    public boolean update(Enrollment entity) {
        String sql = "UPDATE enrollments SET person_id=?, course_id=?, registrar_id=?, amount=?, payment_type=?, "
                + "last_attendance_date=?, status=?, cancel_reason=? WHERE enrollment_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, entity.getPersonId());
            ps.setLong(2, entity.getCourseId());
            setNullableLong(ps, 3, entity.getRegistrarId());
            ps.setBigDecimal(4, entity.getAmount());
            ps.setString(5, entity.getPaymentType());
            ps.setObject(6, entity.getLastAttendanceDate());
            ps.setString(7, entity.getStatus());
            ps.setString(8, entity.getCancelReason());
            ps.setLong(9, entity.getEnrollmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update Enrollment 失敗", e);
        }
    }

    /**
     * enrollments 採軟刪除：狀態改為 cancelled（漏斗分析需要保留原始歷程）。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE enrollments SET status = 'cancelled' WHERE enrollment_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById Enrollment 失敗", e);
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getLong("enrollment_id"));
        e.setPersonId(rs.getLong("person_id"));
        e.setCourseId(rs.getLong("course_id"));
        long registrarId = rs.getLong("registrar_id");
        e.setRegistrarId(rs.wasNull() ? null : registrarId);
        BigDecimal amount = rs.getBigDecimal("amount");
        e.setAmount(amount);
        e.setPaymentType(rs.getString("payment_type"));
        e.setEnrolledAt(toLocalDateTime(rs.getTimestamp("enrolled_at")));
        e.setLastAttendanceDate(toLocalDate(rs.getDate("last_attendance_date")));
        e.setStatus(rs.getString("status"));
        e.setCancelReason(rs.getString("cancel_reason"));
        e.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return e;
    }

    private LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
