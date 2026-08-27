package org.center.service;

import org.center.datastructure.CustomHashTable;
import org.center.datastructure.IHashTable;
import org.center.model.Alert;
import org.center.model.Book;
import org.center.model.ContactRecord;
import org.center.model.Course;
import org.center.model.Person;
import org.center.repository.AlertRepository;
import org.center.repository.BookRepository;
import org.center.repository.ContactRecordRepository;
import org.center.repository.CourseRepository;
import org.center.repository.PersonRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 通知中心：掃描「低於安全庫存的書」「逾期未回訪的聯絡」「超過結業日仍進行中的課程」，
 * 產生對應的 alerts 資料列。已存在同來源的未結案警示則略過（用 {@link CustomHashTable} 做 O(1) 去重）。
 * 產生後的優先排序由 {@link AlertService} 的 MaxHeap / MinHeap 負責。
 */
public class AlertGenerationService {

    private final BookRepository bookRepository;
    private final ContactRecordRepository contactRecordRepository;
    private final CourseRepository courseRepository;
    private final PersonRepository personRepository;
    private final AlertRepository alertRepository;
    AuditService auditService = new AuditService();

    public AlertGenerationService() {
        this(new BookRepository(), new ContactRecordRepository(), new CourseRepository(),
                new PersonRepository(), new AlertRepository());
    }

    public AlertGenerationService(BookRepository bookRepository,
                                  ContactRecordRepository contactRecordRepository,
                                  CourseRepository courseRepository, PersonRepository personRepository,
                                  AlertRepository alertRepository) {
        this.bookRepository = bookRepository;
        this.contactRecordRepository = contactRecordRepository;
        this.courseRepository = courseRepository;
        this.personRepository = personRepository;
        this.alertRepository = alertRepository;
    }

    /** 掃描並寫入新的未結案警示，回傳本次新增筆數。 */
    public int generateAll() {
        LocalDate today = LocalDate.now();
        List<Alert> toCreate = buildAlerts(
                bookRepository.findBelowSafetyStock(),
                contactRecordRepository.findOverdueFollowUps(today),
                courseRepository.findAll(),
                personRepository.findAll(),
                alertRepository.findOpenAlerts(),
                today);
        for (Alert alert : toCreate) {
            alertRepository.save(alert);
        }
        if (!toCreate.isEmpty()) {
            auditService.record("CREATE", "alerts", null, "自動產生 " + toCreate.size() + " 筆警示");
        }
        return toCreate.size();
    }

    /**
     * 純函式：由觸發條件建出待新增的 Alert 清單，跳過已有同來源的未結案警示。
     */
    static List<Alert> buildAlerts(List<Book> lowStockBooks, List<ContactRecord> overdueContacts,
                                   List<Course> courses, List<Person> people, List<Alert> existingOpen,
                                   LocalDate asOf) {
        IHashTable<String, Boolean> existingKeys = new CustomHashTable<>();
        for (Alert alert : existingOpen) {
            existingKeys.put(sourceKey(alert.getSourceTable(), alert.getSourceId()), Boolean.TRUE);
        }
        IHashTable<Long, String> personName = new CustomHashTable<>();
        for (Person person : people) {
            personName.put(person.getPersonId(), person.getName());
        }

        List<Alert> result = new ArrayList<>();

        for (Book book : lowStockBooks) {
            if (isNew(existingKeys, "books", book.getBookId())) {
                int gap = book.getSafetyStock() - book.getCurrentStock();
                int severity = 5 + (book.getCurrentStock() == 0 ? 4 : Math.min(3, Math.max(1, gap)));
                result.add(alert("low_stock", severity, asOf, "books", book.getBookId(), null,
                        "書籍〈" + book.getTitle() + "〉庫存 " + book.getCurrentStock()
                                + " 低於安全庫存 " + book.getSafetyStock()));
            }
        }

        for (ContactRecord contact : overdueContacts) {
            if (isNew(existingKeys, "contact_records", contact.getContactId())) {
                long daysOverdue = contact.getNextContactDate() == null
                        ? 0
                        : ChronoUnit.DAYS.between(contact.getNextContactDate(), asOf);
                int severity = 4 + (int) Math.min(4, Math.max(0, daysOverdue / 7));
                String who = personName.get(contact.getPersonId());
                Alert alert = alert("overdue_contact", severity, contact.getNextContactDate(),
                        "contact_records", contact.getContactId(), contact.getPersonId(),
                        "學員〈" + (who == null ? "#" + contact.getPersonId() : who) + "〉的回訪已逾期 "
                                + daysOverdue + " 天");
                result.add(alert);
            }
        }

        for (Course course : courses) {
            boolean overdue = "ongoing".equals(course.getStatus())
                    && course.getCompletionDate() != null
                    && course.getCompletionDate().isBefore(asOf);
            if (overdue && isNew(existingKeys, "courses", course.getCourseId())) {
                result.add(alert("incomplete_course", 6, course.getCompletionDate(), "courses",
                        course.getCourseId(), null,
                        "課程〈" + course.getName() + "〉已超過結業日 " + course.getCompletionDate() + " 仍為進行中"));
            }
        }
        return result;
    }

    private static boolean isNew(IHashTable<String, Boolean> existingKeys, String table, Long id) {
        return !Boolean.TRUE.equals(existingKeys.get(sourceKey(table, id)));
    }

    private static String sourceKey(String table, Long id) {
        return table + "#" + id;
    }

    private static Alert alert(String type, int severity, LocalDate dueDate, String sourceTable,
                               Long sourceId, Long relatedPersonId, String message) {
        Alert alert = new Alert();
        alert.setAlertType(type);
        alert.setSeverity(severity);
        alert.setDueDate(dueDate);
        alert.setPriorityTier(severity >= 8 ? "優先處理" : severity >= 5 ? "注意" : "正常");
        alert.setSourceTable(sourceTable);
        alert.setSourceId(sourceId);
        alert.setRelatedPersonId(relatedPersonId);
        alert.setTriggerReason(type);
        alert.setMessage(message);
        alert.setStatus("open");
        return alert;
    }
}
