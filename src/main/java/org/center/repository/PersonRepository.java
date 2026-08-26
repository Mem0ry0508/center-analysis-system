package org.center.repository;

import org.center.model.Person;
import org.center.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonRepository implements IRepository<Person, Long> {

    @Override
    public Person save(Person entity) {
        String sql = "INSERT INTO people (start_date, contact_source, referrer_name, name, gender, occupation, "
                + "office_phone, home_phone, mobile_phone, email, line_id, birthday, interests, contactable, "
                + "mailable, preferred_channel, stop_contact_reason, note, status, entered_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setPersonId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save Person 失敗", e);
        }
    }

    @Override
    public Optional<Person> findById(Long id) {
        String sql = "SELECT * FROM people WHERE person_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById Person 失敗", e);
        }
    }

    @Override
    public List<Person> findAll() {
        String sql = "SELECT * FROM people";
        List<Person> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll Person 失敗", e);
        }
    }

    @Override
    public boolean update(Person entity) {
        String sql = "UPDATE people SET start_date=?, contact_source=?, referrer_name=?, name=?, gender=?, "
                + "occupation=?, office_phone=?, home_phone=?, mobile_phone=?, email=?, line_id=?, birthday=?, "
                + "interests=?, contactable=?, mailable=?, preferred_channel=?, stop_contact_reason=?, note=?, "
                + "status=?, entered_by=? WHERE person_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, entity);
            ps.setLong(21, entity.getPersonId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update Person 失敗", e);
        }
    }

    /**
     * people 採軟刪除：狀態改為 inactive，不做實體 DELETE（見 db/schema.sql 設計決策 5）。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE people SET status = 'inactive' WHERE person_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById Person 失敗", e);
        }
    }

    private void bind(PreparedStatement ps, Person p) throws SQLException {
        ps.setObject(1, p.getStartDate());
        ps.setString(2, p.getContactSource());
        ps.setString(3, p.getReferrerName());
        ps.setString(4, p.getName());
        ps.setString(5, p.getGender());
        ps.setString(6, p.getOccupation());
        ps.setString(7, p.getOfficePhone());
        ps.setString(8, p.getHomePhone());
        ps.setString(9, p.getMobilePhone());
        ps.setString(10, p.getEmail());
        ps.setString(11, p.getLineId());
        ps.setObject(12, p.getBirthday());
        ps.setString(13, p.getInterests());
        ps.setBoolean(14, p.isContactable());
        ps.setBoolean(15, p.isMailable());
        ps.setString(16, p.getPreferredChannel());
        ps.setString(17, p.getStopContactReason());
        ps.setString(18, p.getNote());
        ps.setString(19, p.getStatus() == null ? "active" : p.getStatus());
        if (p.getEnteredBy() == null) {
            ps.setNull(20, java.sql.Types.BIGINT);
        } else {
            ps.setLong(20, p.getEnteredBy());
        }
    }

    private Person mapRow(ResultSet rs) throws SQLException {
        Person p = new Person();
        p.setPersonId(rs.getLong("person_id"));
        p.setStartDate(toLocalDate(rs.getDate("start_date")));
        p.setContactSource(rs.getString("contact_source"));
        p.setReferrerName(rs.getString("referrer_name"));
        p.setName(rs.getString("name"));
        p.setGender(rs.getString("gender"));
        p.setOccupation(rs.getString("occupation"));
        p.setOfficePhone(rs.getString("office_phone"));
        p.setHomePhone(rs.getString("home_phone"));
        p.setMobilePhone(rs.getString("mobile_phone"));
        p.setEmail(rs.getString("email"));
        p.setLineId(rs.getString("line_id"));
        p.setBirthday(toLocalDate(rs.getDate("birthday")));
        p.setInterests(rs.getString("interests"));
        p.setContactable(rs.getBoolean("contactable"));
        p.setMailable(rs.getBoolean("mailable"));
        p.setPreferredChannel(rs.getString("preferred_channel"));
        p.setStopContactReason(rs.getString("stop_contact_reason"));
        p.setNote(rs.getString("note"));
        p.setStatus(rs.getString("status"));
        long enteredBy = rs.getLong("entered_by");
        p.setEnteredBy(rs.wasNull() ? null : enteredBy);
        p.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        p.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return p;
    }

    private LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
