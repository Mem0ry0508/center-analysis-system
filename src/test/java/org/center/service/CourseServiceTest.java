package org.center.service;

import org.center.model.CoursePrerequisite;
import org.center.repository.CoursePrerequisiteRepository;
import org.center.repository.CourseRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseServiceTest {

    /** 假的邊表：findAllEdges 回傳固定資料，addPrerequisite 只記錄有沒有被呼叫。 */
    private static class FakePrerequisiteRepository extends CoursePrerequisiteRepository {
        private final List<CoursePrerequisite> edges;
        boolean written;

        FakePrerequisiteRepository(List<CoursePrerequisite> edges) {
            this.edges = edges;
        }

        @Override
        public List<CoursePrerequisite> findAllEdges() {
            return new ArrayList<>(edges);
        }

        @Override
        public CoursePrerequisite addPrerequisite(Long courseId, Long prerequisiteCourseId) {
            written = true;
            edges.add(new CoursePrerequisite(courseId, prerequisiteCourseId));
            return new CoursePrerequisite(courseId, prerequisiteCourseId);
        }
    }

    @Test
    void addPrerequisiteRejectsSelfReference() {
        FakePrerequisiteRepository repo = new FakePrerequisiteRepository(new ArrayList<>());
        CourseService service = new CourseService(new CourseRepository(), repo);

        assertThrows(PrerequisiteCycleException.class, () -> service.addPrerequisite(1L, 1L));
        assertTrue(!repo.written);
    }

    @Test
    void addPrerequisiteRejectsEdgeThatWouldFormCycle() {
        // 現有 1 -> 2（2 的先修是 1）。再加 2 -> 1 會形成循環。
        List<CoursePrerequisite> edges = new ArrayList<>(List.of(new CoursePrerequisite(2L, 1L)));
        FakePrerequisiteRepository repo = new FakePrerequisiteRepository(edges);
        CourseService service = new CourseService(new CourseRepository(), repo);

        assertThrows(PrerequisiteCycleException.class, () -> service.addPrerequisite(1L, 2L));
        assertTrue(!repo.written);
    }

    @Test
    void addPrerequisiteWritesWhenNoCycle() {
        List<CoursePrerequisite> edges = new ArrayList<>(List.of(new CoursePrerequisite(2L, 1L)));
        FakePrerequisiteRepository repo = new FakePrerequisiteRepository(edges);
        CourseService service = new CourseService(new CourseRepository(), repo);

        service.addPrerequisite(3L, 2L);

        assertTrue(repo.written);
        assertEquals(2, repo.findAllEdges().size());
    }
}
