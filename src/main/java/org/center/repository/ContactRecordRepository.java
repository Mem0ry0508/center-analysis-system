package org.center.repository;

import org.center.model.ContactRecord;
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

public class ContactRecordRepository implements IRepository<ContactRecord, Long> {

    @Override
    public ContactRecord save(ContactRecord entity) {
        String sql = "INSERT INTO contact_records (person_id, contact_date, method, content, mood_rating, "
                + "result, follow_up_action, next_contact_date, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setContactId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save ContactRecord 失敗", e);
        }
    }

    @Override
    public Optional<ContactRecord> findById(Long id) {
        String sql = "SELECT * FROM contact_records WHERE contact_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById ContactRecord 失敗", e);
        }
    }

    public List<ContactRecord> findByPersonId(Long personId) {
        String sql = "SELECT * FROM contact_records WHERE person_id = ? ORDER BY contact_date";
        List<ContactRecord> result = new ArrayList<>();
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
            throw new RuntimeException("findByPersonId ContactRecord 失敗", e);
        }
    }

    public List<ContactRecord> findOverdueFollowUps(LocalDate asOf) {
        String sql = "SELECT * FROM contact_records WHERE next_contact_date IS NOT NULL AND next_contact_date <= ?";
        List<ContactRecord> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, asOf);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findOverdueFollowUps 失敗", e);
        }
    }

    @Override
    public List<ContactRecord> findAll() {
        String sql = "SELECT * FROM contact_records";
        List<ContactRecord> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll ContactRecord 失敗", e);
        }
    }

    @Override
    public boolean update(ContactRecord entity) {
        String sql = "UPDATE contact_records SET person_id=?, contact_date=?, method=?, content=?, mood_rating=?, "
                + "result=?, follow_up_action=?, next_contact_date=?, created_by=? WHERE contact_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, entity);
            ps.setLong(10, entity.getContactId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update ContactRecord 失敗", e);
        }
    }

    /**
     * contact_records 沒有狀態欄位，本身是一筆一筆的歷程紀錄，允許實體刪除（例如誤建的紀錄）。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM contact_records WHERE contact_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById ContactRecord 失敗", e);
        }
    }

    private void bind(PreparedStatement ps, ContactRecord c) throws SQLException {
        ps.setLong(1, c.getPersonId());
        ps.setObject(2, c.getContactDate());
        ps.setString(3, c.getMethod());
        ps.setString(4, c.getContent());
        if (c.getMoodRating() == null) {
            ps.setNull(5, Types.TINYINT);
        } else {
            ps.setInt(5, c.getMoodRating());
        }
        ps.setString(6, c.getResult());
        ps.setString(7, c.getFollowUpAction());
        ps.setObject(8, c.getNextContactDate());
        if (c.getCreatedBy() == null) {
            ps.setNull(9, Types.BIGINT);
        } else {
            ps.setLong(9, c.getCreatedBy());
        }
    }

    private ContactRecord mapRow(ResultSet rs) throws SQLException {
        ContactRecord c = new ContactRecord();
        c.setContactId(rs.getLong("contact_id"));
        c.setPersonId(rs.getLong("person_id"));
        c.setContactDate(toLocalDateTime(rs.getTimestamp("contact_date")));
        c.setMethod(rs.getString("method"));
        c.setContent(rs.getString("content"));
        int moodRating = rs.getInt("mood_rating");
        c.setMoodRating(rs.wasNull() ? null : moodRating);
        c.setResult(rs.getString("result"));
        c.setFollowUpAction(rs.getString("follow_up_action"));
        c.setNextContactDate(toLocalDate(rs.getDate("next_contact_date")));
        long createdBy = rs.getLong("created_by");
        c.setCreatedBy(rs.wasNull() ? null : createdBy);
        return c;
    }

    private LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
