package org.center.repository;

import org.center.model.CoursePrerequisite;
import org.center.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * course_prerequisites 是複合主鍵的邊表，不套用 IRepository&lt;T, ID&gt;。
 * findAllEdges() 提供給 B 的 CustomGraph 建圖（course_id -> prerequisite_course_id）。
 */
public class CoursePrerequisiteRepository {

    public CoursePrerequisite addPrerequisite(Long courseId, Long prerequisiteCourseId) {
        String sql = "INSERT INTO course_prerequisites (course_id, prerequisite_course_id) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setLong(2, prerequisiteCourseId);
            ps.executeUpdate();
            return new CoursePrerequisite(courseId, prerequisiteCourseId);
        } catch (SQLException e) {
            throw new RuntimeException("addPrerequisite 失敗", e);
        }
    }

    public boolean removePrerequisite(Long courseId, Long prerequisiteCourseId) {
        String sql = "DELETE FROM course_prerequisites WHERE course_id = ? AND prerequisite_course_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            ps.setLong(2, prerequisiteCourseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("removePrerequisite 失敗", e);
        }
    }

    public List<Long> findPrerequisitesOf(Long courseId) {
        String sql = "SELECT prerequisite_course_id FROM course_prerequisites WHERE course_id = ?";
        List<Long> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getLong("prerequisite_course_id"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findPrerequisitesOf 失敗", e);
        }
    }

    /**
     * 供 B 的 CustomGraph 一次讀出所有邊，建構 adjacency list。
     */
    public List<CoursePrerequisite> findAllEdges() {
        String sql = "SELECT course_id, prerequisite_course_id FROM course_prerequisites";
        List<CoursePrerequisite> result = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new CoursePrerequisite(rs.getLong("course_id"), rs.getLong("prerequisite_course_id")));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("findAllEdges 失敗", e);
        }
    }
}
