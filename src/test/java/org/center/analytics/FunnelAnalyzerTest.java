package org.center.analytics;

import org.center.model.Enrollment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunnelAnalyzerTest {

    @Test
    void accumulatesLaterStagesIntoEarlierOnes() {
        List<Enrollment> enrollments = new ArrayList<>();
        add(enrollments, "contacted", 2);
        add(enrollments, "introduced", 3);
        add(enrollments, "registered", 5);
        add(enrollments, "completed", 10);
        add(enrollments, "cancelled", 4);

        FunnelReport report = FunnelAnalyzer.analyze(enrollments);
        List<FunnelStage> stages = report.getStages();

        // contacted: everyone except cancelled = 2+3+5+10 = 20
        assertEquals("contacted", stages.get(0).getStageName());
        assertEquals(20, stages.get(0).getCount());
        // registered or later = 5 + 10 = 15
        assertEquals(15, stages.get(2).getCount());
        // started or later = 10 (only completed)
        assertEquals(10, stages.get(3).getCount());
        assertEquals(10, stages.get(4).getCount());
        assertEquals(4, report.getCancelledCount());
    }

    @Test
    void conversionAndDropOffBetweenStages() {
        List<Enrollment> enrollments = new ArrayList<>();
        add(enrollments, "contacted", 50);
        add(enrollments, "registered", 50);

        FunnelReport report = FunnelAnalyzer.analyze(enrollments);
        FunnelStage introduced = report.getStages().get(1);

        // contacted total = 100, introduced-or-later = 50 => 50%
        assertEquals(50.0, introduced.getConversionFromPrevious(), 0.001);
        assertEquals(50, introduced.getDropOff());
        // no one reached "completed" => overall conversion 0%
        assertEquals(0.0, report.getOverallConversion(), 0.001);
    }

    @Test
    void handlesEmptyInput() {
        FunnelReport report = FunnelAnalyzer.analyze(List.of());
        assertEquals(FunnelAnalyzer.STAGES.length, report.getStages().size());
        assertTrue(report.getStages().stream().allMatch(s -> s.getCount() == 0));
        assertEquals(0.0, report.getOverallConversion(), 0.001);
    }

    private void add(List<Enrollment> list, String status, int times) {
        for (int i = 0; i < times; i++) {
            Enrollment e = new Enrollment();
            e.setStatus(status);
            list.add(e);
        }
    }
}
