package org.center.analytics;

import org.center.model.Course;
import org.center.model.CoursePrerequisite;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoursePathAnalyzerTest {

    @Test
    void recommendsPrerequisitesBeforeAdvancedCourses() {
        List<Course> courses = List.of(course(1, "基礎"), course(2, "進階"), course(3, "專題"));
        // 1 -> 2 -> 3
        List<CoursePrerequisite> edges = List.of(
                new CoursePrerequisite(2L, 1L),
                new CoursePrerequisite(3L, 2L));

        CoursePathReport report = CoursePathAnalyzer.analyze(edges, courses);

        assertFalse(report.isHasCycle());
        List<Long> order = report.getRecommendedOrder().stream().map(Course::getCourseId).toList();
        assertTrue(order.indexOf(1L) < order.indexOf(2L));
        assertTrue(order.indexOf(2L) < order.indexOf(3L));
        assertEquals(3, report.getRecommendedOrder().size());
    }

    @Test
    void detectsCycleAndSkipsOrdering() {
        List<Course> courses = List.of(course(1, "A"), course(2, "B"));
        // A 需要 B、B 需要 A
        List<CoursePrerequisite> edges = List.of(
                new CoursePrerequisite(1L, 2L),
                new CoursePrerequisite(2L, 1L));

        CoursePathReport report = CoursePathAnalyzer.analyze(edges, courses);

        assertTrue(report.isHasCycle());
        assertTrue(report.getRecommendedOrder().isEmpty());
        assertTrue(report.getCycleHint() != null && !report.getCycleHint().isBlank());
    }

    private Course course(long id, String name) {
        Course c = new Course();
        c.setCourseId(id);
        c.setName(name);
        return c;
    }
}
