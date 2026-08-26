package org.center.repository;

import org.center.model.Book;
import org.center.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookRepository implements IRepository<Book, Long> {

    @Override
    public Book save(Book entity) {
        String sql = "INSERT INTO books (title, isbn, author, category, supplier, cost, list_price, "
                + "storage_location, safety_stock, current_stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setBookId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save Book 失敗", e);
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById Book 失敗", e);
        }
    }

    public List<Book> findBelowSafetyStock() {
        String sql = "SELECT * FROM books WHERE current_stock < safety_stock";
        List<Book> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findBelowSafetyStock 失敗", e);
        }
    }

    @Override
    public List<Book> findAll() {
        String sql = "SELECT * FROM books";
        List<Book> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll Book 失敗", e);
        }
    }

    @Override
    public boolean update(Book entity) {
        String sql = "UPDATE books SET title=?, isbn=?, author=?, category=?, supplier=?, cost=?, list_price=?, "
                + "storage_location=?, safety_stock=?, current_stock=? WHERE book_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, entity);
            ps.setLong(11, entity.getBookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update Book 失敗", e);
        }
    }

    /**
     * books 沒有狀態欄位；若有 inventory_transactions 參照會因外鍵限制刪除失敗（回傳 false 較安全，
     * 呼叫端應改用調整庫存為 0 或另加 status 欄位，而不是刪除書籍主檔）。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void bind(PreparedStatement ps, Book b) throws SQLException {
        ps.setString(1, b.getTitle());
        ps.setString(2, b.getIsbn());
        ps.setString(3, b.getAuthor());
        ps.setString(4, b.getCategory());
        ps.setString(5, b.getSupplier());
        ps.setBigDecimal(6, b.getCost());
        ps.setBigDecimal(7, b.getListPrice());
        ps.setString(8, b.getStorageLocation());
        ps.setInt(9, b.getSafetyStock());
        ps.setInt(10, b.getCurrentStock());
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setBookId(rs.getLong("book_id"));
        b.setTitle(rs.getString("title"));
        b.setIsbn(rs.getString("isbn"));
        b.setAuthor(rs.getString("author"));
        b.setCategory(rs.getString("category"));
        b.setSupplier(rs.getString("supplier"));
        b.setCost(rs.getBigDecimal("cost"));
        b.setListPrice(rs.getBigDecimal("list_price"));
        b.setStorageLocation(rs.getString("storage_location"));
        b.setSafetyStock(rs.getInt("safety_stock"));
        b.setCurrentStock(rs.getInt("current_stock"));
        b.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        return b;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
