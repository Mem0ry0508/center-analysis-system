package org.center.analytics;

import java.util.Map;

/**
 * 第一階段：儀表板統計結果。
 * 所有分類計數由 {@link DashboardAnalyzer} 以自訂 {@code CustomHashTable} 單趟彙總後填入。
 */
public class DashboardStats {

    private final int totalPeople;
    private final int activePeople;
    private final int totalCourses;
    private final int ongoingCourses;
    private final int openAlerts;
    private final int lowStockBooks;
    private final int overdueContacts;
    private final Map<String, Integer> peopleByStatus;
    private final Map<String, Integer> coursesByStatus;
    private final Map<String, Integer> enrollmentsByStage;

    public DashboardStats(int totalPeople, int activePeople, int totalCourses, int ongoingCourses,
                          int openAlerts, int lowStockBooks, int overdueContacts,
                          Map<String, Integer> peopleByStatus, Map<String, Integer> coursesByStatus,
                          Map<String, Integer> enrollmentsByStage) {
        this.totalPeople = totalPeople;
        this.activePeople = activePeople;
        this.totalCourses = totalCourses;
        this.ongoingCourses = ongoingCourses;
        this.openAlerts = openAlerts;
        this.lowStockBooks = lowStockBooks;
        this.overdueContacts = overdueContacts;
        this.peopleByStatus = peopleByStatus;
        this.coursesByStatus = coursesByStatus;
        this.enrollmentsByStage = enrollmentsByStage;
    }

    public int getTotalPeople() {
        return totalPeople;
    }

    public int getActivePeople() {
        return activePeople;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public int getOngoingCourses() {
        return ongoingCourses;
    }

    public int getOpenAlerts() {
        return openAlerts;
    }

    public int getLowStockBooks() {
        return lowStockBooks;
    }

    public int getOverdueContacts() {
        return overdueContacts;
    }

    public Map<String, Integer> getPeopleByStatus() {
        return peopleByStatus;
    }

    public Map<String, Integer> getCoursesByStatus() {
        return coursesByStatus;
    }

    public Map<String, Integer> getEnrollmentsByStage() {
        return enrollmentsByStage;
    }
}
