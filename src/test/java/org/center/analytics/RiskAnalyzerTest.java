package org.center.analytics;

import org.center.model.ContactRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskAnalyzerTest {

    private final LocalDate asOf = LocalDate.of(2026, 8, 27);

    @Test
    void noContactHistoryIsHighRisk() {
        RiskEntry entry = RiskAnalyzer.score(1L, "王小明", null, null, false, asOf);
        assertEquals(40, entry.getRiskScore());
        assertTrue(entry.getReasons().contains("無任何聯絡紀錄"));
    }

    @Test
    void recentPositiveContactIsLowRisk() {
        ContactRecord recent = contact(asOf.minusDays(5).atStartOfDay(), 5, null);
        RiskEntry entry = RiskAnalyzer.score(2L, "李小華", List.of(recent), asOf.minusDays(3), false, asOf);
        assertEquals(0, entry.getRiskScore());
        assertEquals(List.of("無明顯風險"), entry.getReasons());
    }

    @Test
    void staleContactLowMoodOverdueAndCancellationStack() {
        ContactRecord stale = contact(asOf.minusDays(100).atStartOfDay(), 1, asOf.minusDays(10));
        RiskEntry entry = RiskAnalyzer.score(3L, "陳大同", List.of(stale), asOf.minusDays(90), true, asOf);
        // 逾90天未聯絡(30) + 情緒偏低(15) + 逾期未回訪(20) + 逾60天未出席(20) + 曾取消(25)
        assertEquals(110, entry.getRiskScore());
        assertTrue(entry.isHasOverdueFollowUp());
    }

    @Test
    void topRiskEntriesReturnsHighestScoresInDescendingOrder() {
        List<RiskEntry> entries = List.of(
                entryWithScore(10), entryWithScore(90), entryWithScore(50), entryWithScore(70));

        List<RiskEntry> top = RiskAnalyzer.topRiskEntries(entries, 3);

        assertEquals(3, top.size());
        assertEquals(90, top.get(0).getRiskScore());
        assertEquals(70, top.get(1).getRiskScore());
        assertEquals(50, top.get(2).getRiskScore());
    }

    @Test
    void topRiskEntriesClampsToListSize() {
        List<RiskEntry> entries = List.of(entryWithScore(5), entryWithScore(8));
        assertEquals(2, RiskAnalyzer.topRiskEntries(entries, 10).size());
    }

    private ContactRecord contact(LocalDateTime when, Integer mood, LocalDate nextContact) {
        ContactRecord c = new ContactRecord();
        c.setPersonId(1L);
        c.setContactDate(when);
        c.setMoodRating(mood);
        c.setNextContactDate(nextContact);
        return c;
    }

    private RiskEntry entryWithScore(int score) {
        return new RiskEntry(score, "P" + score, score, List.of("test"), null, null, false);
    }
}
