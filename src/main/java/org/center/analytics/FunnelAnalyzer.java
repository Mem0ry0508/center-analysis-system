package org.center.analytics;

import org.center.datastructure.CustomHashTable;
import org.center.datastructure.IHashTable;
import org.center.model.Enrollment;

import java.util.ArrayList;
import java.util.List;

/**
 * 第二階段（分析項目一）：報名漏斗。
 *
 * <p>漏斗階段是線性推進：contacted → introduced → registered → started → completed。
 * 每筆 enrollment 只帶「目前所在階段」，本分析把它累積回「至少到達過的每個階段」，
 * 因此每個階段人數必然遞減，相鄰兩階段相除即為留存率。
 *
 * <p>階段名稱 → 序位的對照用自訂 {@link CustomHashTable} 建表後 O(1) 查詢，
 * 取代逐一 {@code equals} 比對或 {@code switch}。
 */
public final class FunnelAnalyzer {

    static final String[] STAGES = {"contacted", "introduced", "registered", "started", "completed"};
    private static final String CANCELLED = "cancelled";

    private FunnelAnalyzer() {
    }

    public static FunnelReport analyze(List<Enrollment> enrollments) {
        IHashTable<String, Integer> stageRank = new CustomHashTable<>();
        for (int i = 0; i < STAGES.length; i++) {
            stageRank.put(STAGES[i], i);
        }

        int[] atLeast = new int[STAGES.length];
        int cancelled = 0;
        for (Enrollment enrollment : enrollments) {
            String status = enrollment.getStatus();
            if (CANCELLED.equals(status)) {
                cancelled++;
                continue;
            }
            Integer rank = stageRank.get(status);
            if (rank == null) {
                continue;
            }
            for (int i = 0; i <= rank; i++) {
                atLeast[i]++;
            }
        }

        List<FunnelStage> stages = new ArrayList<>(STAGES.length);
        for (int i = 0; i < STAGES.length; i++) {
            int prev = i == 0 ? atLeast[i] : atLeast[i - 1];
            double conversion = prev == 0 ? 0.0 : (atLeast[i] * 100.0) / prev;
            int dropOff = i == 0 ? 0 : prev - atLeast[i];
            stages.add(new FunnelStage(STAGES[i], atLeast[i], conversion, dropOff));
        }

        double overall = atLeast[0] == 0 ? 0.0 : (atLeast[STAGES.length - 1] * 100.0) / atLeast[0];
        return new FunnelReport(stages, cancelled, overall);
    }
}
