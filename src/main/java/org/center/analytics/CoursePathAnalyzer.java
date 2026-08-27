package org.center.analytics;

import org.center.datastructure.CustomGraph;
import org.center.datastructure.CustomHashTable;
import org.center.datastructure.IGraph;
import org.center.datastructure.IHashTable;
import org.center.model.Course;
import org.center.model.CoursePrerequisite;

import java.util.ArrayList;
import java.util.List;

/**
 * 第二階段（加分項）：課程先修路徑分析。
 *
 * <p>用自訂 {@link CustomGraph}{@code <Long>} 以 adjacency list 建圖，邊的方向是
 * 「先修課 → 進階課」，因此拓撲排序結果就是一條合法的建議修課順序。
 * 先呼叫 {@code hasCycle()} 偵測循環；有循環就不排序並回報，對應老師文件明文要求。
 * 課程 id → Course 物件的對照用自訂 {@link CustomHashTable}。
 */
public final class CoursePathAnalyzer {

    private CoursePathAnalyzer() {
    }

    public static CoursePathReport analyze(List<CoursePrerequisite> edges, List<Course> courses) {
        IGraph<Long> graph = new CustomGraph<>();
        IHashTable<Long, Course> courseById = new CustomHashTable<>();
        for (Course course : courses) {
            if (course.getCourseId() != null) {
                graph.addNode(course.getCourseId());
                courseById.put(course.getCourseId(), course);
            }
        }
        for (CoursePrerequisite edge : edges) {
            graph.addEdge(edge.getPrerequisiteCourseId(), edge.getCourseId());
        }

        if (graph.hasCycle()) {
            return new CoursePathReport(true,
                    "先修關係中存在循環，無法排出修課順序，請檢查是否有課程互相指定為先修。",
                    List.of());
        }

        List<Long> order = graph.topologicalSort();
        List<Course> recommended = new ArrayList<>(order.size());
        for (Long id : order) {
            Course course = courseById.get(id);
            if (course != null) {
                recommended.add(course);
            }
        }
        return new CoursePathReport(false, null, recommended);
    }
}
