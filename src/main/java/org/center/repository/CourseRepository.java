package org.center.repository;

import org.center.model.Course;
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

public class CourseRepository implements IRepository<Course, Long> {

    @Override
    public Course save(Course entity) {
        String sql = "INSERT INTO courses (name, instructor_id, capacity, payment_type, class_time_slot, "
                + "classroom, start_date, completion_date, first_class_date, status, suspend_reason) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setCourseId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save Course 失敗", e);
        }
    }

    @Override
    public Optional<Course> findById(Long id) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById Course 失敗", e);
        }
    }

    @Override
    public List<Course> findAll() {
        String sql = "SELECT * FROM courses";
        List<Course> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll Course 失敗", e);
        }
    }

    @Override
    public boolean update(Course entity) {
        String sql = "UPDATE courses SET name=?, instructor_id=?, capacity=?, payment_type=?, class_time_slot=?, "
                + "classroom=?, start_date=?, completion_date=?, first_class_date=?, status=?, suspend_reason=? "
                + "WHERE course_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, entity);
            ps.setLong(12, entity.getCourseId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update Course 失敗", e);
        }
    }

    /**
     * courses 採軟刪除：狀態改為 cancelled，維持 enrollments 對此課程的歷史關聯。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE courses SET status = 'cancelled' WHERE course_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById Course 失敗", e);
        }
    }

    private void bind(PreparedStatement ps, Course c) throws SQLException {
        ps.setString(1, c.getName());
        if (c.getInstructorId() == null) {
            ps.setNull(2, Types.BIGINT);
        } else {
            ps.setLong(2, c.getInstructorId());
        }
        ps.setInt(3, c.getCapacity());
        ps.setString(4, c.getPaymentType());
        ps.setString(5, c.getClassTimeSlot());
        ps.setString(6, c.getClassroom());
        ps.setObject(7, c.getStartDate());
        ps.setObject(8, c.getCompletionDate());
        ps.setObject(9, c.getFirstClassDate());
        ps.setString(10, c.getStatus() == null ? "planned" : c.getStatus());
        ps.setString(11, c.getSuspendReason());
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getLong("course_id"));
        c.setName(rs.getString("name"));
        long instructorId = rs.getLong("instructor_id");
        c.setInstructorId(rs.wasNull() ? null : instructorId);
        c.setCapacity(rs.getInt("capacity"));
        c.setPaymentType(rs.getString("payment_type"));
        c.setClassTimeSlot(rs.getString("class_time_slot"));
        c.setClassroom(rs.getString("classroom"));
        c.setStartDate(toLocalDate(rs.getDate("start_date")));
        c.setCompletionDate(toLocalDate(rs.getDate("completion_date")));
        c.setFirstClassDate(toLocalDate(rs.getDate("first_class_date")));
        c.setStatus(rs.getString("status"));
        c.setSuspendReason(rs.getString("suspend_reason"));
        c.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        return c;
    }

    private LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
