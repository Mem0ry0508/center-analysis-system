package org.center.model;

/**
 * course_prerequisites 是複合主鍵的關聯表（course_id, prerequisite_course_id），
 * 不套用 IRepository&lt;T, ID&gt; 單一 ID 的 CRUD 模式，改由 CoursePrerequisiteRepository
 * 提供 addPrerequisite / removePrerequisite / findPrerequisitesOf 等針對邊的操作，
 * 供 B 的 CustomGraph 讀取建圖。
 */
public class CoursePrerequisite {
    private Long courseId;
    private Long prerequisiteCourseId;

    public CoursePrerequisite() {
    }

    public CoursePrerequisite(Long courseId, Long prerequisiteCourseId) {
        this.courseId = courseId;
        this.prerequisiteCourseId = prerequisiteCourseId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getPrerequisiteCourseId() {
        return prerequisiteCourseId;
    }

    public void setPrerequisiteCourseId(Long prerequisiteCourseId) {
        this.prerequisiteCourseId = prerequisiteCourseId;
    }
}
