package org.center.service;

import org.center.datastructure.CustomGraph;
import org.center.datastructure.IGraph;
import org.center.model.Course;
import org.center.model.CoursePrerequisite;
import org.center.repository.CoursePrerequisiteRepository;
import org.center.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

public class CourseService {

    private final CourseRepository courseRepository;
    private final CoursePrerequisiteRepository coursePrerequisiteRepository;

    public CourseService() {
        this(new CourseRepository(), new CoursePrerequisiteRepository());
    }

    public CourseService(CourseRepository courseRepository,
                         CoursePrerequisiteRepository coursePrerequisiteRepository) {
        this.courseRepository = courseRepository;
        this.coursePrerequisiteRepository = coursePrerequisiteRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public Course save(Course course) {
        if (course.getCourseId() == null) {
            return courseRepository.save(course);
        }
        courseRepository.update(course);
        return course;
    }

    public boolean deactivate(Long id) {
        return courseRepository.deleteById(id);
    }

    public List<Long> findPrerequisiteIds(Long courseId) {
        return coursePrerequisiteRepository.findPrerequisitesOf(courseId);
    }

    /**
     * 新增一條先修關係。寫入資料庫前，用自訂 {@link CustomGraph} 把現有全部先修邊 + 這條新邊建成有向圖，
     * 呼叫 {@code hasCycle()} 檢查；若會形成循環則拋 {@link PrerequisiteCycleException} 且不寫入。
     */
    public void addPrerequisite(Long courseId, Long prerequisiteCourseId) {
        if (courseId.equals(prerequisiteCourseId)) {
            throw new PrerequisiteCycleException("課程不能將自己設為先修課程");
        }
        IGraph<Long> graph = new CustomGraph<>();
        for (CoursePrerequisite edge : coursePrerequisiteRepository.findAllEdges()) {
            graph.addEdge(edge.getPrerequisiteCourseId(), edge.getCourseId());
        }
        graph.addEdge(prerequisiteCourseId, courseId);
        if (graph.hasCycle()) {
            throw new PrerequisiteCycleException("加入此先修關係會形成循環，已取消，未寫入資料庫");
        }
        coursePrerequisiteRepository.addPrerequisite(courseId, prerequisiteCourseId);
    }

    public boolean removePrerequisite(Long courseId, Long prerequisiteCourseId) {
        return coursePrerequisiteRepository.removePrerequisite(courseId, prerequisiteCourseId);
    }
}
