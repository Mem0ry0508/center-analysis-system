package org.center.service;

import org.center.model.Alert;
import org.center.model.Book;
import org.center.model.ContactRecord;
import org.center.model.Course;
import org.center.model.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertGenerationServiceTest {

    private final LocalDate today = LocalDate.of(2026, 8, 27);

    @Test
    void buildsOneAlertPerTriggerCondition() {
        List<Book> lowStock = List.of(book(1L, "資料結構", 0, 5), book(2L, "演算法", 3, 5));
        List<ContactRecord> overdue = List.of(contact(10L, 100L, today.minusDays(14)));
        List<Course> courses = List.of(ongoingCourse(20L, "Java 專題", today.minusDays(3)));
        List<Person> people = List.of(person(100L, "王小明"));

        List<Alert> alerts = AlertGenerationService.buildAlerts(lowStock, overdue, courses, people, List.of(), today);

        assertEquals(4, alerts.size());
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType().equals("low_stock") && a.getSeverity() >= 8));
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType().equals("overdue_contact")
                && a.getMessage().contains("王小明")));
        assertTrue(alerts.stream().anyMatch(a -> a.getAlertType().equals("incomplete_course")));
        assertTrue(alerts.stream().allMatch(a -> "open".equals(a.getStatus())));
    }

    @Test
    void skipsConditionsThatAlreadyHaveOpenAlert() {
        List<Book> lowStock = List.of(book(1L, "資料結構", 0, 5), book(2L, "演算法", 3, 5));
        Alert existing = new Alert();
        existing.setSourceTable("books");
        existing.setSourceId(1L);
        existing.setStatus("open");

        List<Alert> alerts = AlertGenerationService.buildAlerts(
                lowStock, List.of(), List.of(), List.of(), List.of(existing), today);

        assertEquals(1, alerts.size());
        assertEquals(2L, alerts.get(0).getSourceId());
    }

    @Test
    void ignoresCoursesThatAreNotOverdue() {
        List<Course> courses = List.of(
                ongoingCourse(1L, "未逾期", today.plusDays(30)),
                endedCourse(2L, "已結業"));

        List<Alert> alerts = AlertGenerationService.buildAlerts(
                List.of(), List.of(), courses, List.of(), List.of(), today);

        assertTrue(alerts.isEmpty());
    }

    private Book book(long id, String title, int current, int safety) {
        Book b = new Book();
        b.setBookId(id);
        b.setTitle(title);
        b.setCurrentStock(current);
        b.setSafetyStock(safety);
        return b;
    }

    private ContactRecord contact(long id, long personId, LocalDate nextContact) {
        ContactRecord c = new ContactRecord();
        c.setContactId(id);
        c.setPersonId(personId);
        c.setNextContactDate(nextContact);
        return c;
    }

    private Course ongoingCourse(long id, String name, LocalDate completion) {
        Course c = new Course();
        c.setCourseId(id);
        c.setName(name);
        c.setStatus("ongoing");
        c.setCompletionDate(completion);
        return c;
    }

    private Course endedCourse(long id, String name) {
        Course c = new Course();
        c.setCourseId(id);
        c.setName(name);
        c.setStatus("ended");
        c.setCompletionDate(today.minusDays(10));
        return c;
    }

    private Person person(long id, String name) {
        Person p = new Person();
        p.setPersonId(id);
        p.setName(name);
        return p;
    }
}
