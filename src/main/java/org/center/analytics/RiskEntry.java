package org.center.analytics;

import java.util.List;

/**
 * 第二／第三階段：單一人員的流失風險評估結果。
 * {@code riskScore} 越高代表越需要優先關懷（供 MaxHeap 取優先名單）。
 */
public class RiskEntry {

    private final long personId;
    private final String personName;
    private final int riskScore;
    private final List<String> reasons;
    private final Integer daysSinceLastContact;
    private final Integer daysSinceLastAttendance;
    private final boolean hasOverdueFollowUp;

    public RiskEntry(long personId, String personName, int riskScore, List<String> reasons,
                     Integer daysSinceLastContact, Integer daysSinceLastAttendance, boolean hasOverdueFollowUp) {
        this.personId = personId;
        this.personName = personName;
        this.riskScore = riskScore;
        this.reasons = reasons;
        this.daysSinceLastContact = daysSinceLastContact;
        this.daysSinceLastAttendance = daysSinceLastAttendance;
        this.hasOverdueFollowUp = hasOverdueFollowUp;
    }

    public long getPersonId() {
        return personId;
    }

    public String getPersonName() {
        return personName;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public Integer getDaysSinceLastContact() {
        return daysSinceLastContact;
    }

    public Integer getDaysSinceLastAttendance() {
        return daysSinceLastAttendance;
    }

    public boolean isHasOverdueFollowUp() {
        return hasOverdueFollowUp;
    }

    /** 供表格顯示的原因摘要。 */
    public String getReasonText() {
        return String.join("；", reasons);
    }
}
