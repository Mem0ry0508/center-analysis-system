package org.center.service;

import org.center.model.Alert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 對真實資料庫跑一次警示產生 + MinHeap/MaxHeap 排序，印出結果。預設不執行：
 * {@code mvn test -Ddb.it=true -Dtest=AlertGenerationServiceIT}
 */
@EnabledIfSystemProperty(named = "db.it", matches = "true")
class AlertGenerationServiceIT {

    @Test
    void generateThenOrder() {
        int created = new AlertGenerationService().generateAll();
        System.out.println("本次新增警示 " + created + " 筆");

        AlertService alertService = new AlertService();
        List<Alert> bySeverity = alertService.findOpenAlertsByPriority();
        List<Alert> byDueDate = alertService.findOpenAlertsByDueDate();

        System.out.println("=== 依嚴重度 (MaxHeap) 前 5 ===");
        bySeverity.stream().limit(5).forEach(a ->
                System.out.printf("  sev %d  %s  %s%n", a.getSeverity(), a.getDueDate(), a.getMessage()));
        System.out.println("=== 依到期日 (MinHeap) 前 5 ===");
        byDueDate.stream().limit(5).forEach(a ->
                System.out.printf("  %s  sev %d  %s%n", a.getDueDate(), a.getSeverity(), a.getMessage()));

        for (int i = 1; i < bySeverity.size(); i++) {
            assertTrue(bySeverity.get(i - 1).getSeverity() >= bySeverity.get(i).getSeverity());
        }
    }
}
