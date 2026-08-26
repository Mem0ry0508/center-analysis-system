package org.center.service;

import org.center.model.Course;
import org.center.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService() {
        this(new CourseRepository());
    }

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
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
}
