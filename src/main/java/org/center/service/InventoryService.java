package org.center.service;

import org.center.model.InventoryTransaction;
import org.center.repository.InventoryTransactionRepository;

import java.math.BigDecimal;
import java.util.List;

public class InventoryService {

    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryService() {
        this(new InventoryTransactionRepository());
    }

    public InventoryService(InventoryTransactionRepository inventoryTransactionRepository) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    public List<InventoryTransaction> findAll() {
        return inventoryTransactionRepository.findAll();
    }

    public List<InventoryTransaction> findByBookId(Long bookId) {
        return inventoryTransactionRepository.findByBookId(bookId);
    }

    /**
     * 銷售扣庫存，底層用 A 的 recordSale（同一個 Transaction 內 commit/rollback）。
     */
    public InventoryTransaction recordSale(Long bookId, Long personId, int quantity, BigDecimal unitPrice,
                                            BigDecimal discount, String invoiceType, String invoiceNumber) {
        return inventoryTransactionRepository.recordSale(bookId, personId, quantity, unitPrice,
                discount, invoiceType, invoiceNumber);
    }

    /**
     * 進貨：新增交易紀錄並累加書籍庫存，底層用 A 的 recordPurchase（同一個 Transaction 內 commit/rollback）。
     */
    public InventoryTransaction recordPurchase(InventoryTransaction transaction) {
        return inventoryTransactionRepository.recordPurchase(transaction);
    }
}
