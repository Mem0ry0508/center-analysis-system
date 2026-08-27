package org.center.service;

import org.center.model.Enrollment;
import org.center.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrollmentServiceTest {

    private static class FakeEnrollmentRepository extends EnrollmentRepository {
        private final Map<Long, Enrollment> store = new HashMap<>();
        private long nextId = 1;

        @Override
        public Enrollment save(Enrollment entity) {
            entity.setEnrollmentId(nextId++);
            store.put(entity.getEnrollmentId(), entity);
            return entity;
        }

        @Override
        public Optional<Enrollment> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean update(Enrollment entity) {
            if (!store.containsKey(entity.getEnrollmentId())) {
                return false;
            }
            store.put(entity.getEnrollmentId(), entity);
            return true;
        }
    }

    private EnrollmentService service() {
        EnrollmentService service = new EnrollmentService(new FakeEnrollmentRepository());
        service.auditService = AuditService.disabled();
        return service;
    }

    @Test
    void saveDefaultsStatusToContacted() {
        EnrollmentService service = service();
        Enrollment enrollment = new Enrollment();
        enrollment.setPersonId(1L);
        enrollment.setCourseId(2L);

        Enrollment saved = service.save(enrollment);

        assertEquals("contacted", saved.getStatus());
    }

    @Test
    void cancelSetsStatusAndReason() {
        EnrollmentService service = service();
        Enrollment enrollment = new Enrollment();
        enrollment.setPersonId(1L);
        enrollment.setCourseId(2L);
        enrollment.setStatus("started");
        Enrollment saved = service.save(enrollment);

        boolean ok = service.cancel(saved.getEnrollmentId(), "時間衝突");

        assertTrue(ok);
        Enrollment reloaded = service.findById(saved.getEnrollmentId()).orElseThrow();
        assertEquals("cancelled", reloaded.getStatus());
        assertEquals("時間衝突", reloaded.getCancelReason());
    }

    @Test
    void cancelReturnsFalseForUnknownId() {
        assertFalse(service().cancel(999L, "x"));
    }
}
