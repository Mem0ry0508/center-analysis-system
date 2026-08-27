package org.center.analytics;

import java.util.List;

/**
 * 第二階段：報名漏斗整體結果。
 */
public class FunnelReport {

    private final List<FunnelStage> stages;
    private final int cancelledCount;
    private final double overallConversion;

    public FunnelReport(List<FunnelStage> stages, int cancelledCount, double overallConversion) {
        this.stages = stages;
        this.cancelledCount = cancelledCount;
        this.overallConversion = overallConversion;
    }

    public List<FunnelStage> getStages() {
        return stages;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public double getOverallConversion() {
        return overallConversion;
    }
}
