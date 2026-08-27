package org.center.analytics;

import org.center.datastructure.CustomHashTable;
import org.center.datastructure.IHashTable;
import org.center.model.Alert;
import org.center.model.Book;
import org.center.model.ContactRecord;
import org.center.model.Course;
import org.center.model.Enrollment;
import org.center.model.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 第一階段：儀表板統計。
 *
 * <p>純函式，不碰資料庫；輸入是各 Repository 的 {@code findAll()} 結果，輸出 {@link DashboardStats}。
 * 分類計數不使用 {@code java.util.HashMap} 或 Stream group-by，而是用自訂
 * {@link CustomHashTable} 單趟累加（{@code get} 判斷是否首見、{@code put} 覆寫累加值），
 * 對應報告的「Hash Table O(1) 平均查詢／更新」說明。
 */
public final class DashboardAnalyzer {

    private DashboardAnalyzer() {
    }

    public static DashboardStats analyze(List<Person> people, List<Course> courses,
                                         List<Enrollment> enrollments, List<Alert> alerts,
                                         List<Book> books, List<ContactRecord> contacts, LocalDate asOf) {

        Map<String, Integer> peopleByStatus = tally(people.stream().map(Person::getStatus).toList());
        Map<String, Integer> coursesByStatus = tally(courses.stream().map(Course::getStatus).toList());
        Map<String, Integer> enrollmentsByStage = tally(enrollments.stream().map(Enrollment::getStatus).toList());
        Map<String, Integer> alertsByStatus = tally(alerts.stream().map(Alert::getStatus).toList());

        int activePeople = peopleByStatus.getOrDefault("active", 0);
        int ongoingCourses = coursesByStatus.getOrDefault("ongoing", 0);
        int openAlerts = alertsByStatus.getOrDefault("open", 0);
        int lowStockBooks = countLowStock(books);
        int overdueContacts = countOverdueFollowUps(contacts, asOf);

        return new DashboardStats(people.size(), activePeople, courses.size(), ongoingCourses,
                openAlerts, lowStockBooks, overdueContacts,
                peopleByStatus, coursesByStatus, enrollmentsByStage);
    }

    /**
     * 用自訂 Hash Table 依鍵累加出現次數；另用 List 保留首見順序，讓輸出 Map 穩定可讀。
     */
    static Map<String, Integer> tally(List<String> values) {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        List<String> order = new ArrayList<>();
        for (String raw : values) {
            String key = raw == null ? "unknown" : raw;
            Integer current = table.get(key);
            if (current == null) {
                order.add(key);
                table.put(key, 1);
            } else {
                table.put(key, current + 1);
            }
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String key : order) {
            result.put(key, table.get(key));
        }
        return result;
    }

    private static int countLowStock(List<Book> books) {
        int count = 0;
        for (Book book : books) {
            if (book.getCurrentStock() < book.getSafetyStock()) {
                count++;
            }
        }
        return count;
    }

    private static int countOverdueFollowUps(List<ContactRecord> contacts, LocalDate asOf) {
        int count = 0;
        for (ContactRecord contact : contacts) {
            LocalDate next = contact.getNextContactDate();
            if (next != null && !next.isAfter(asOf)) {
                count++;
            }
        }
        return count;
    }
}
