package org.center.service;

import org.center.model.AuditLog;
import org.center.repository.AuditLogRepository;
import org.center.util.AuditContext;

import java.util.List;

/**
 * 稽核紀錄服務：對主資料的新增／修改／作廢寫一列 audit_logs（append-only，見 {@link AuditLogRepository}）。
 * 稽核寫入失敗不應中斷主流程，因此 {@link #record} 內部吞例外只記到 stderr。
 */
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final boolean enabled;

    public AuditService() {
        this(new AuditLogRepository(), true);
    }

    public AuditService(AuditLogRepository auditLogRepository) {
        this(auditLogRepository, true);
    }

    private AuditService(AuditLogRepository auditLogRepository, boolean enabled) {
        this.auditLogRepository = auditLogRepository;
        this.enabled = enabled;
    }

    /** 測試用：不寫任何東西的稽核服務。 */
    public static AuditService disabled() {
        return new AuditService(null, false);
    }

    public void record(String action, String tableName, Long recordId, String reason) {
        record(action, tableName, recordId, null, null, null, reason);
    }

    public void record(String action, String tableName, Long recordId, String fieldName,
                       String oldValue, String newValue, String reason) {
        if (!enabled) {
            return;
        }
        try {
            AuditLog log = new AuditLog();
            log.setActorId(AuditContext.currentActorId());
            log.setAction(action);
            log.setTableName(tableName);
            log.setRecordId(recordId);
            log.setFieldName(fieldName);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setReason(reason);
            auditLogRepository.save(log);
        } catch (RuntimeException e) {
            System.err.println("稽核紀錄寫入失敗（主流程不受影響）：" + e.getMessage());
        }
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}
