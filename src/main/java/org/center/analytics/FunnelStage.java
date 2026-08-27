package org.center.analytics;

/**
 * 第二階段：報名漏斗的單一階段。
 * {@code count} 為「至少到達此階段」的累積人數；{@code conversionFromPrevious} 為相對前一階段的留存率(%)。
 */
public class FunnelStage {

    private final String stageName;
    private final int count;
    private final double conversionFromPrevious;
    private final int dropOff;

    public FunnelStage(String stageName, int count, double conversionFromPrevious, int dropOff) {
        this.stageName = stageName;
        this.count = count;
        this.conversionFromPrevious = conversionFromPrevious;
        this.dropOff = dropOff;
    }

    public String getStageName() {
        return stageName;
    }

    public int getCount() {
        return count;
    }

    public double getConversionFromPrevious() {
        return conversionFromPrevious;
    }

    public int getDropOff() {
        return dropOff;
    }

    /** 供表格直接顯示的百分比字串。 */
    public String getConversionText() {
        return String.format("%.1f%%", conversionFromPrevious);
    }
}
