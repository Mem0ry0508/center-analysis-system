package org.center.analytics;

import org.center.datastructure.CustomHashTable;
import org.center.datastructure.IHashTable;
import org.center.datastructure.IHeap;
import org.center.datastructure.MaxHeap;
import org.center.model.ContactRecord;
import org.center.model.Enrollment;
import org.center.model.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 第二階段（分析項目二）：學員流失風險名單。
 * 第三階段：{@link #topRiskEntries(List, int)} 用自訂 {@link MaxHeap} 取風險最高的前 N 位。
 *
 * <p>資料結構用法：
 * <ul>
 *   <li>用 {@link CustomHashTable}{@code <Long, List<ContactRecord>>} 單趟把聯絡紀錄依 personId 分組，
 *       之後每位學員 O(1) 取回自己的紀錄，取代「每人再掃一次全表」。</li>
 *   <li>用 {@link CustomHashTable}{@code <Long, LocalDate>} 記錄每位學員最後出席日。</li>
 *   <li>用 {@link PersonDirectory#nameOf(long)}（內部 BinarySearch）把 personId 換成姓名。</li>
 *   <li>用 {@link MaxHeap} 依 riskScore 取優先名單。</li>
 * </ul>
 */
public final class RiskAnalyzer {

    private RiskAnalyzer() {
    }

    public static List<RiskEntry> analyze(List<Person> people, List<ContactRecord> contacts,
                                          List<Enrollment> enrollments, PersonDirectory directory,
                                          LocalDate asOf) {

        IHashTable<Long, List<ContactRecord>> contactsByPerson = groupContactsByPerson(contacts);
        IHashTable<Long, LocalDate> lastAttendanceByPerson = latestAttendanceByPerson(enrollments);
        IHashTable<Long, Boolean> cancelledByPerson = cancelledByPerson(enrollments);

        List<RiskEntry> entries = new ArrayList<>();
        for (Person person : people) {
            if (!"active".equals(person.getStatus()) || person.getPersonId() == null) {
                continue;
            }
            long personId = person.getPersonId();
            List<ContactRecord> personContacts = contactsByPerson.get(personId);
            LocalDate lastAttendance = lastAttendanceByPerson.get(personId);
            boolean cancelled = Boolean.TRUE.equals(cancelledByPerson.get(personId));

            entries.add(score(personId, directory.nameOf(personId), personContacts, lastAttendance, cancelled, asOf));
        }
        return entries;
    }

    /** 第三階段：自訂 MaxHeap 依 riskScore 取前 N 名優先關懷名單。 */
    public static List<RiskEntry> topRiskEntries(List<RiskEntry> entries, int n) {
        IHeap<RiskEntry> heap = new MaxHeap<>(Comparator.comparingInt(RiskEntry::getRiskScore));
        for (RiskEntry entry : entries) {
            heap.insert(entry);
        }
        int limit = Math.min(n, entries.size());
        List<RiskEntry> top = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            top.add(heap.remove());
        }
        return top;
    }

    static RiskEntry score(long personId, String name, List<ContactRecord> personContacts,
                           LocalDate lastAttendance, boolean cancelled, LocalDate asOf) {
        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        Integer daysSinceContact = daysSinceLastContact(personContacts, asOf);
        if (daysSinceContact == null) {
            riskScore += 40;
            reasons.add("無任何聯絡紀錄");
        } else if (daysSinceContact > 90) {
            riskScore += 30;
            reasons.add("逾 90 天未聯絡");
        } else if (daysSinceContact > 60) {
            riskScore += 20;
            reasons.add("逾 60 天未聯絡");
        } else if (daysSinceContact > 30) {
            riskScore += 10;
            reasons.add("逾 30 天未聯絡");
        }

        Integer latestMood = latestMoodRating(personContacts);
        if (latestMood != null && latestMood <= 2) {
            riskScore += 15;
            reasons.add("最近情緒評分偏低");
        }

        boolean overdueFollowUp = hasOverdueFollowUp(personContacts, asOf);
        if (overdueFollowUp) {
            riskScore += 20;
            reasons.add("有逾期未回訪");
        }

        Integer daysSinceAttendance = lastAttendance == null
                ? null
                : (int) ChronoUnit.DAYS.between(lastAttendance, asOf);
        if (daysSinceAttendance != null && daysSinceAttendance > 60) {
            riskScore += 20;
            reasons.add("逾 60 天未出席");
        } else if (daysSinceAttendance != null && daysSinceAttendance > 30) {
            riskScore += 10;
            reasons.add("逾 30 天未出席");
        }

        if (cancelled) {
            riskScore += 25;
            reasons.add("曾取消報名");
        }

        if (reasons.isEmpty()) {
            reasons.add("無明顯風險");
        }
        return new RiskEntry(personId, name, riskScore, reasons, daysSinceContact, daysSinceAttendance, overdueFollowUp);
    }

    private static IHashTable<Long, List<ContactRecord>> groupContactsByPerson(List<ContactRecord> contacts) {
        IHashTable<Long, List<ContactRecord>> table = new CustomHashTable<>();
        for (ContactRecord contact : contacts) {
            Long personId = contact.getPersonId();
            if (personId == null) {
                continue;
            }
            List<ContactRecord> bucket = table.get(personId);
            if (bucket == null) {
                bucket = new ArrayList<>();
                table.put(personId, bucket);
            }
            bucket.add(contact);
        }
        return table;
    }

    private static IHashTable<Long, LocalDate> latestAttendanceByPerson(List<Enrollment> enrollments) {
        IHashTable<Long, LocalDate> table = new CustomHashTable<>();
        for (Enrollment enrollment : enrollments) {
            LocalDate attendance = enrollment.getLastAttendanceDate();
            Long personId = enrollment.getPersonId();
            if (attendance == null || personId == null) {
                continue;
            }
            LocalDate current = table.get(personId);
            if (current == null || attendance.isAfter(current)) {
                table.put(personId, attendance);
            }
        }
        return table;
    }

    private static IHashTable<Long, Boolean> cancelledByPerson(List<Enrollment> enrollments) {
        IHashTable<Long, Boolean> table = new CustomHashTable<>();
        for (Enrollment enrollment : enrollments) {
            if ("cancelled".equals(enrollment.getStatus()) && enrollment.getPersonId() != null) {
                table.put(enrollment.getPersonId(), Boolean.TRUE);
            }
        }
        return table;
    }

    private static Integer daysSinceLastContact(List<ContactRecord> contacts, LocalDate asOf) {
        LocalDateTime latest = latestContactDate(contacts);
        return latest == null ? null : (int) ChronoUnit.DAYS.between(latest.toLocalDate(), asOf);
    }

    private static LocalDateTime latestContactDate(List<ContactRecord> contacts) {
        if (contacts == null) {
            return null;
        }
        LocalDateTime latest = null;
        for (ContactRecord contact : contacts) {
            LocalDateTime date = contact.getContactDate();
            if (date != null && (latest == null || date.isAfter(latest))) {
                latest = date;
            }
        }
        return latest;
    }

    private static Integer latestMoodRating(List<ContactRecord> contacts) {
        if (contacts == null) {
            return null;
        }
        LocalDateTime latest = null;
        Integer rating = null;
        for (ContactRecord contact : contacts) {
            LocalDateTime date = contact.getContactDate();
            if (date != null && (latest == null || date.isAfter(latest))) {
                latest = date;
                rating = contact.getMoodRating();
            }
        }
        return rating;
    }

    private static boolean hasOverdueFollowUp(List<ContactRecord> contacts, LocalDate asOf) {
        if (contacts == null) {
            return false;
        }
        for (ContactRecord contact : contacts) {
            LocalDate next = contact.getNextContactDate();
            if (next != null && !next.isAfter(asOf)) {
                return true;
            }
        }
        return false;
    }
}
