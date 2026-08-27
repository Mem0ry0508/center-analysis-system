package org.center.service;

import org.center.model.Alert;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void orderByDueDatePutsEarliestFirstAndNullsLast() {
        Alert soon = alertWithDueDate(LocalDate.of(2026, 1, 5));
        Alert later = alertWithDueDate(LocalDate.of(2026, 3, 1));
        Alert noDate = alertWithDueDate(null);

        List<Alert> ordered = AlertService.orderByDueDate(List.of(later, noDate, soon));

        assertEquals(LocalDate.of(2026, 1, 5), ordered.get(0).getDueDate());
        assertEquals(LocalDate.of(2026, 3, 1), ordered.get(1).getDueDate());
        assertNull(ordered.get(2).getDueDate());
    }

    private Alert alertWithSeverity(int severity) {
        Alert alert = new Alert();
        alert.setSeverity(severity);
        return alert;
    }

    private Alert alertWithDueDate(LocalDate dueDate) {
        Alert alert = new Alert();
        alert.setDueDate(dueDate);
        return alert;
    }
}
