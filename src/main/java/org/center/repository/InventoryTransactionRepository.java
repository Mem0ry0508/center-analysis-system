package org.center.repository;

import org.center.model.InventoryTransaction;
import org.center.util.ConnectionManager;

import java.math.BigDecimal;
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

public class InventoryTransactionRepository implements IRepository<InventoryTransaction, Long> {

    /**
     * 銷售扣庫存 + 建立交易紀錄的 Transaction 案例（分工計畫要求的跨表 commit/rollback 示範）。
     * 庫存不足時整筆 rollback，不建立交易紀錄、也不會扣庫存。
     */
    public InventoryTransaction recordSale(Long bookId, Long personId, int quantity, BigDecimal unitPrice,
                                            BigDecimal discount, String invoiceType, String invoiceNumber) {
        String selectStockSql = "SELECT current_stock FROM books WHERE book_id = ? FOR UPDATE";
        String updateStockSql = "UPDATE books SET current_stock = current_stock - ? WHERE book_id = ?";
        String insertTxSql = "INSERT INTO inventory_transactions (book_id, transaction_type, person_id, "
                + "quantity, unit_price, discount, net_amount, invoice_type, invoice_number, status) "
                + "VALUES (?, 'sale', ?, ?, ?, ?, ?, ?, ?, 'completed')";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            int currentStock;
            try (PreparedStatement ps = conn.prepareStatement(selectStockSql)) {
                ps.setLong(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("找不到 book_id=" + bookId);
                    }
                    currentStock = rs.getInt("current_stock");
                }
            }

            if (currentStock < quantity) {
                conn.rollback();
                throw new IllegalStateException("庫存不足，無法完成銷售：book_id=" + bookId
                        + " 現有=" + currentStock + " 需求=" + quantity);
            }

            try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                ps.setInt(1, quantity);
                ps.setLong(2, bookId);
                ps.executeUpdate();
            }

            BigDecimal netAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
            InventoryTransaction tx = new InventoryTransaction();
            try (PreparedStatement ps = conn.prepareStatement(insertTxSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, bookId);
                if (personId == null) {
                    ps.setNull(2, Types.BIGINT);
                } else {
                    ps.setLong(2, personId);
                }
                ps.setInt(3, quantity);
                ps.setBigDecimal(4, unitPrice);
                ps.setBigDecimal(5, discount);
                ps.setBigDecimal(6, netAmount);
                ps.setString(7, invoiceType);
                ps.setString(8, invoiceNumber);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        tx.setTransactionId(keys.getLong(1));
                    }
                }
            }

            conn.commit();
            tx.setBookId(bookId);
            tx.setPersonId(personId);
            tx.setTransactionType("sale");
            tx.setQuantity(quantity);
            tx.setUnitPrice(unitPrice);
            tx.setDiscount(discount);
            tx.setNetAmount(netAmount);
            tx.setInvoiceType(invoiceType);
            tx.setInvoiceNumber(invoiceNumber);
            tx.setStatus("completed");
            return tx;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("recordSale 失敗，已 rollback", e);
        } catch (IllegalStateException | IllegalArgumentException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * 進貨新增交易紀錄 + 累加庫存的 Transaction 案例，跟 recordSale 一樣單一 connection 內
     * 手動 commit/rollback，避免「交易紀錄建好了但庫存沒真的加上去」的不一致狀態。
     */
    public InventoryTransaction recordPurchase(InventoryTransaction transaction) {
        String insertSql = "INSERT INTO inventory_transactions (book_id, transaction_type, supplier, "
                + "quantity, unit_price, discount, net_amount, document_number, inspection_status, status) "
                + "VALUES (?, 'purchase', ?, ?, ?, ?, ?, ?, ?, 'completed')";
        String updateStockSql = "UPDATE books SET current_stock = current_stock + ? WHERE book_id = ?";

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            BigDecimal discount = transaction.getDiscount() == null ? BigDecimal.ZERO : transaction.getDiscount();
            BigDecimal netAmount = transaction.getNetAmount() != null
                    ? transaction.getNetAmount()
                    : transaction.getUnitPrice().multiply(BigDecimal.valueOf(transaction.getQuantity()))
                            .subtract(discount);
            String inspectionStatus = transaction.getInspectionStatus() == null
                    ? "accepted" : transaction.getInspectionStatus();

            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, transaction.getBookId());
                ps.setString(2, transaction.getSupplier());
                ps.setInt(3, transaction.getQuantity());
                ps.setBigDecimal(4, transaction.getUnitPrice());
                ps.setBigDecimal(5, discount);
                ps.setBigDecimal(6, netAmount);
                ps.setString(7, transaction.getDocumentNumber());
                ps.setString(8, inspectionStatus);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        transaction.setTransactionId(keys.getLong(1));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                ps.setInt(1, transaction.getQuantity());
                ps.setLong(2, transaction.getBookId());
                ps.executeUpdate();
            }

            conn.commit();
            transaction.setTransactionType("purchase");
            transaction.setDiscount(discount);
            transaction.setNetAmount(netAmount);
            transaction.setInspectionStatus(inspectionStatus);
            transaction.setStatus("completed");
            return transaction;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("recordPurchase 失敗，已 rollback", e);
        } finally {
            closeQuietly(conn);
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public InventoryTransaction save(InventoryTransaction entity) {
        String sql = "INSERT INTO inventory_transactions (book_id, transaction_type, person_id, supplier, "
                + "quantity, unit_price, discount, net_amount, invoice_type, invoice_number, document_number, "
                + "inspection_status, status, note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setTransactionId(keys.getLong(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("save InventoryTransaction 失敗", e);
        }
    }

    @Override
    public Optional<InventoryTransaction> findById(Long id) {
        String sql = "SELECT * FROM inventory_transactions WHERE transaction_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById InventoryTransaction 失敗", e);
        }
    }

    public List<InventoryTransaction> findByBookId(Long bookId) {
        String sql = "SELECT * FROM inventory_transactions WHERE book_id = ? ORDER BY transaction_date";
        List<InventoryTransaction> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findByBookId InventoryTransaction 失敗", e);
        }
    }

    @Override
    public List<InventoryTransaction> findAll() {
        String sql = "SELECT * FROM inventory_transactions";
        List<InventoryTransaction> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAll InventoryTransaction 失敗", e);
        }
    }

    @Override
    public boolean update(InventoryTransaction entity) {
        String sql = "UPDATE inventory_transactions SET book_id=?, transaction_type=?, person_id=?, supplier=?, "
                + "quantity=?, unit_price=?, discount=?, net_amount=?, invoice_type=?, invoice_number=?, "
                + "document_number=?, inspection_status=?, status=?, note=? WHERE transaction_id=?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, entity);
            ps.setLong(15, entity.getTransactionId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("update InventoryTransaction 失敗", e);
        }
    }

    /**
     * inventory_transactions 採軟刪除：狀態改為 reversed，保留原始交易歷程（見 FR-042 反向紀錄要求）。
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE inventory_transactions SET status = 'reversed' WHERE transaction_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("deleteById InventoryTransaction 失敗", e);
        }
    }

    private void bind(PreparedStatement ps, InventoryTransaction t) throws SQLException {
        ps.setLong(1, t.getBookId());
        ps.setString(2, t.getTransactionType());
        if (t.getPersonId() == null) {
            ps.setNull(3, Types.BIGINT);
        } else {
            ps.setLong(3, t.getPersonId());
        }
        ps.setString(4, t.getSupplier());
        ps.setInt(5, t.getQuantity());
        ps.setBigDecimal(6, t.getUnitPrice());
        ps.setBigDecimal(7, t.getDiscount());
        ps.setBigDecimal(8, t.getNetAmount());
        ps.setString(9, t.getInvoiceType());
        ps.setString(10, t.getInvoiceNumber());
        ps.setString(11, t.getDocumentNumber());
        ps.setString(12, t.getInspectionStatus() == null ? "pending" : t.getInspectionStatus());
        ps.setString(13, t.getStatus() == null ? "completed" : t.getStatus());
        ps.setString(14, t.getNote());
    }

    private InventoryTransaction mapRow(ResultSet rs) throws SQLException {
        InventoryTransaction t = new InventoryTransaction();
        t.setTransactionId(rs.getLong("transaction_id"));
        t.setBookId(rs.getLong("book_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        long personId = rs.getLong("person_id");
        t.setPersonId(rs.wasNull() ? null : personId);
        t.setSupplier(rs.getString("supplier"));
        t.setQuantity(rs.getInt("quantity"));
        t.setUnitPrice(rs.getBigDecimal("unit_price"));
        t.setDiscount(rs.getBigDecimal("discount"));
        t.setNetAmount(rs.getBigDecimal("net_amount"));
        t.setInvoiceType(rs.getString("invoice_type"));
        t.setInvoiceNumber(rs.getString("invoice_number"));
        t.setDocumentNumber(rs.getString("document_number"));
        t.setInspectionStatus(rs.getString("inspection_status"));
        t.setTransactionDate(toLocalDateTime(rs.getTimestamp("transaction_date")));
        t.setStatus(rs.getString("status"));
        t.setNote(rs.getString("note"));
        return t;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
