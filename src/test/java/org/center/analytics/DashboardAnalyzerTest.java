package org.center.analytics;

import org.center.model.Alert;
import org.center.model.Book;
import org.center.model.ContactRecord;
import org.center.model.Course;
import org.center.model.Enrollment;
import org.center.model.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardAnalyzerTest {

    private final LocalDate today = LocalDate.of(2026, 8, 27);

    @Test
    void countsPeopleCoursesAndDerivedTotals() {
        List<Person> people = List.of(person("active"), person("active"), person("inactive"));
        List<Course> courses = List.of(course("ongoing"), course("ended"), course("ongoing"));
        List<Enrollment> enrollments = List.of(enrollment("contacted"), enrollment("completed"));
        List<Alert> alerts = List.of(alert("open"), alert("open"), alert("resolved"));
        List<Book> books = List.of(book(2, 5), book(10, 5));
        List<ContactRecord> contacts = List.of(
                contactWithFollowUp(today.minusDays(1)),
                contactWithFollowUp(today.plusDays(3)));

        DashboardStats stats = DashboardAnalyzer.analyze(people, courses, enrollments, alerts, books, contacts, today);

        assertEquals(3, stats.getTotalPeople());
        assertEquals(2, stats.getActivePeople());
        assertEquals(3, stats.getTotalCourses());
        assertEquals(2, stats.getOngoingCourses());
        assertEquals(2, stats.getOpenAlerts());
        assertEquals(1, stats.getLowStockBooks());
        assertEquals(1, stats.getOverdueContacts());
    }

    @Test
    void tallyCountsByKeyAndTreatsNullAsUnknown() {
        var result = DashboardAnalyzer.tally(java.util.Arrays.asList("a", "a", "b", null));
        assertEquals(2, result.get("a"));
        assertEquals(1, result.get("b"));
        assertEquals(1, result.get("unknown"));
    }

    private Person person(String status) {
        Person p = new Person();
        p.setStatus(status);
        return p;
    }

    private Course course(String status) {
        Course c = new Course();
        c.setStatus(status);
        return c;
    }

    private Enrollment enrollment(String status) {
        Enrollment e = new Enrollment();
        e.setStatus(status);
        return e;
    }

    private Alert alert(String status) {
        Alert a = new Alert();
        a.setStatus(status);
        return a;
    }

    private Book book(int currentStock, int safetyStock) {
        Book b = new Book();
        b.setCurrentStock(currentStock);
        b.setSafetyStock(safetyStock);
        return b;
    }

    private ContactRecord contactWithFollowUp(LocalDate next) {
        ContactRecord c = new ContactRecord();
        c.setNextContactDate(next);
        return c;
    }
}
