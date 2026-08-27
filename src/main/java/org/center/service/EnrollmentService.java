package org.center.service;

import org.center.model.Enrollment;
import org.center.repository.EnrollmentRepository;

import java.util.List;
import java.util.Optional;

/**
 * 報名／選課業務邏輯。狀態涵蓋漏斗階段：
 * contacted → introduced → registered → started → completed（cancelled 為取消）。
 */
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    AuditService auditService = new AuditService();

    public EnrollmentService() {
        this(new EnrollmentRepository());
    }

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepository.findById(id);
    }

    public List<Enrollment> findByPersonId(Long personId) {
        return enrollmentRepository.findByPersonId(personId);
    }

    public Enrollment save(Enrollment enrollment) {
        if (enrollment.getStatus() == null || enrollment.getStatus().isBlank()) {
            enrollment.setStatus("contacted");
        }
        boolean isNew = enrollment.getEnrollmentId() == null;
        Enrollment result;
        if (isNew) {
            result = enrollmentRepository.save(enrollment);
        } else {
            enrollmentRepository.update(enrollment);
            result = enrollment;
        }
        auditService.record(isNew ? "CREATE" : "UPDATE", "enrollments", result.getEnrollmentId(),
                (isNew ? "新增報名" : "更新報名") + "（學員 " + result.getPersonId()
                        + " / 課程 " + result.getCourseId() + " / 狀態 " + result.getStatus() + "）");
        return result;
    }

    /** 取消報名：軟刪除，狀態改 cancelled 並記錄原因（漏斗分析需要保留歷程）。 */
    public boolean cancel(Long id, String reason) {
        Optional<Enrollment> found = enrollmentRepository.findById(id);
        if (found.isEmpty()) {
            return false;
        }
        Enrollment enrollment = found.get();
        enrollment.setStatus("cancelled");
        enrollment.setCancelReason(reason == null || reason.isBlank() ? "未填原因" : reason);
        boolean ok = enrollmentRepository.update(enrollment);
        if (ok) {
            auditService.record("VOID", "enrollments", id, "取消報名：" + enrollment.getCancelReason());
        }
        return ok;
    }
}
