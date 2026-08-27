package org.center.analytics;

import org.center.model.Course;

import java.util.List;

/**
 * 第二階段（加分項）：課程先修路徑分析結果。
 * 若 {@code hasCycle} 為真，代表先修關係中存在循環，無法排出修課順序（對應老師文件「發現循環須提示」）。
 */
public class CoursePathReport {

    private final boolean hasCycle;
    private final String cycleHint;
    private final List<Course> recommendedOrder;

    public CoursePathReport(boolean hasCycle, String cycleHint, List<Course> recommendedOrder) {
        this.hasCycle = hasCycle;
        this.cycleHint = cycleHint;
        this.recommendedOrder = recommendedOrder;
    }

    public boolean isHasCycle() {
        return hasCycle;
    }

    public String getCycleHint() {
        return cycleHint;
    }

    public List<Course> getRecommendedOrder() {
        return recommendedOrder;
    }
}
