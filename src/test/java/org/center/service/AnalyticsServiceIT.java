package org.center.service;

import org.center.analytics.CoursePathReport;
import org.center.analytics.FunnelReport;
import org.center.analytics.FunnelStage;
import org.center.analytics.RiskEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 對真實資料庫跑一次分析，印出結果供人工檢視。預設不執行：
 * {@code mvn test -Ddb.it=true -Dtest=AnalyticsServiceIT}
 */
@EnabledIfSystemProperty(named = "db.it", matches = "true")
class AnalyticsServiceIT {

    private final AnalyticsService service = new AnalyticsService();

    @Test
    void funnelIsMonotonicallyDecreasing() {
        FunnelReport report = service.enrollmentFunnel();
        System.out.println("=== 報名漏斗 ===");
        int previous = Integer.MAX_VALUE;
        for (FunnelStage stage : report.getStages()) {
            System.out.printf("%-12s %5d  留存 %.1f%%  流失 %d%n",
                    stage.getStageName(), stage.getCount(), stage.getConversionFromPrevious(), stage.getDropOff());
            assertTrue(stage.getCount() <= previous, "漏斗階段人數應遞減");
            previous = stage.getCount();
        }
        System.out.println("已取消 " + report.getCancelledCount() + "，整體轉換率 " + report.getOverallConversion() + "%");
    }

    @Test
    void riskTableAndTopHeapAgree() {
        List<RiskEntry> table = service.riskTable();
        System.out.println("=== 流失風險（前 10）===");
        List<RiskEntry> top = service.topRisk(table, 10);
        for (RiskEntry entry : top) {
            System.out.printf("#%d %-8s 分數 %d  %s%n",
                    entry.getPersonId(), entry.getPersonName(), entry.getRiskScore(), entry.getReasonText());
        }
        assertTrue(table.size() >= top.size());
        for (int i = 1; i < top.size(); i++) {
            assertTrue(top.get(i - 1).getRiskScore() >= top.get(i).getRiskScore(), "Heap 應依分數遞減");
        }
        if (!table.isEmpty()) {
            assertTrue(table.get(0).getRiskScore() >= table.get(table.size() - 1).getRiskScore(), "MergeSort 應依分數遞減");
        }
    }

    @Test
    void coursePathIsAcyclicForSeedData() {
        CoursePathReport report = service.coursePath();
        System.out.println("=== 課程先修路徑 ===");
        System.out.println("hasCycle=" + report.isHasCycle());
        report.getRecommendedOrder().forEach(c -> System.out.println("  " + c.getCourseId() + " " + c.getName()));
        assertFalse(report.isHasCycle(), "seed 資料是 DAG，不應有循環");
    }

    @Test
    void dashboardStatsPopulated() {
        var stats = service.dashboardStats();
        System.out.printf("人 %d（active %d）｜課 %d（ongoing %d）｜警示 %d｜低庫存 %d｜逾期回訪 %d%n",
                stats.getTotalPeople(), stats.getActivePeople(), stats.getTotalCourses(), stats.getOngoingCourses(),
                stats.getOpenAlerts(), stats.getLowStockBooks(), stats.getOverdueContacts());
        assertTrue(stats.getTotalPeople() > 0);
    }
}
