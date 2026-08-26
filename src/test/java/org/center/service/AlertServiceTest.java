package org.center.service;

import org.center.model.Alert;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertServiceTest {

    @Test
    void orderByPriorityPutsHighestSeverityFirst() {
        Alert low = alertWithSeverity(1);
        Alert high = alertWithSeverity(9);
        Alert mid = alertWithSeverity(5);

        List<Alert> ordered = AlertService.orderByPriority(List.of(low, high, mid));

        assertEquals(List.of(high, mid, low), ordered);
    }

    @Test
    void orderByPriorityHandlesEmptyList() {
        assertEquals(List.of(), AlertService.orderByPriority(List.of()));
    }

    @Test
    void orderByPriorityHandlesTiedSeverity() {
        Alert a = alertWithSeverity(3);
        Alert b = alertWithSeverity(3);

        List<Alert> ordered = AlertService.orderByPriority(List.of(a, b));

        assertEquals(2, ordered.size());
        assertEquals(3, ordered.get(0).getSeverity());
        assertEquals(3, ordered.get(1).getSeverity());
    }

    private Alert alertWithSeverity(int severity) {
        Alert alert = new Alert();
        alert.setSeverity(severity);
        return alert;
    }
}
