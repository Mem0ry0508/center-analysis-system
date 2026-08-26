package org.center.repository;

import org.center.model.Account;
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

public class AccountRepository implements IRepository<Account, Long> {

    @Override
    public Account save(Account entity) {
        String sql = "INSERT INTO accounts (username, password_hash, role, person_id, is_active, "
                + "failed_login_count, last_login_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getPasswordHash());
            ps.setString(3, entity.getRole());
            setNullableLong(ps, 4, entity.getPersonId());
            ps.setBoolean(5, entity.isActive());
            ps.setInt(6, entity.getFailedLoginCount());
            ps.setObject(7, entity.getLastLoginAt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setAccountId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save Account 失敗", e);
        }
    }

    @Override
    public Optional<Account> findById(Long id) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById Account 失敗", e);
        }
    }

    public Optional<Account> findByUsername(String username) {
        String sql = "SELECT * FROM accounts WHERE username = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByUsername Account 失敗", e);
        }
    }

    @Override
    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts";
        List<Account> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll Account 失敗", e);
        }
    }

    @Override
    public boolean update(Account entity) {
        String sql = "UPDATE accounts SET username=?, password_hash=?, role=?, person_id=?, is_active=?, "
                + "failed_login_count=?, last_login_at=? WHERE account_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getPasswordHash());
            ps.setString(3, entity.getRole());
            setNullableLong(ps, 4, entity.getPersonId());
            ps.setBoolean(5, entity.isActive());
            ps.setInt(6, entity.getFailedLoginCount());
            ps.setObject(7, entity.getLastLoginAt());
            ps.setLong(8, entity.getAccountId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update Account 失敗", e);
        }
    }

    /**
     * accounts 採軟刪除：is_active 設為 false，避免刪掉帳號後遺失稽核紀錄的操作者關聯。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE accounts SET is_active = FALSE WHERE account_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById Account 失敗", e);
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountId(rs.getLong("account_id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setRole(rs.getString("role"));
        long personId = rs.getLong("person_id");
        a.setPersonId(rs.wasNull() ? null : personId);
        a.setActive(rs.getBoolean("is_active"));
        a.setFailedLoginCount(rs.getInt("failed_login_count"));
        a.setLastLoginAt(toLocalDateTime(rs.getTimestamp("last_login_at")));
        a.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        return a;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
