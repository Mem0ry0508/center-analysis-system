package org.center.service;

import org.center.algorithm.MergeSort;
import org.center.analytics.CoursePathAnalyzer;
import org.center.analytics.CoursePathReport;
import org.center.analytics.DashboardAnalyzer;
import org.center.analytics.DashboardStats;
import org.center.analytics.FunnelAnalyzer;
import org.center.analytics.FunnelReport;
import org.center.analytics.PersonDirectory;
import org.center.analytics.RiskAnalyzer;
import org.center.analytics.RiskEntry;
import org.center.model.Person;
import org.center.repository.AlertRepository;
import org.center.repository.BookRepository;
import org.center.repository.ContactRecordRepository;
import org.center.repository.CoursePrerequisiteRepository;
import org.center.repository.CourseRepository;
import org.center.repository.EnrollmentRepository;
import org.center.repository.PersonRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 分析模組的對外門面：從各 Repository 拉資料，交給 {@code analytics} 套件的分析器，回傳給 UI。
 * UI 不直接碰 Repository 或資料結構內部。
 */
public class AnalyticsService {

    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ContactRecordRepository contactRecordRepository;
    private final BookRepository bookRepository;
    private final AlertRepository alertRepository;
    private final CoursePrerequisiteRepository coursePrerequisiteRepository;

    public AnalyticsService() {
        this(new PersonRepository(), new CourseRepository(), new EnrollmentRepository(),
                new ContactRecordRepository(), new BookRepository(), new AlertRepository(),
                new CoursePrerequisiteRepository());
    }

    public AnalyticsService(PersonRepository personRepository, CourseRepository courseRepository,
                            EnrollmentRepository enrollmentRepository,
                            ContactRecordRepository contactRecordRepository, BookRepository bookRepository,
                            AlertRepository alertRepository,
                            CoursePrerequisiteRepository coursePrerequisiteRepository) {
        this.personRepository = personRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.contactRecordRepository = contactRecordRepository;
        this.bookRepository = bookRepository;
        this.alertRepository = alertRepository;
        this.coursePrerequisiteRepository = coursePrerequisiteRepository;
    }

    /** 第一階段：儀表板統計。 */
    public DashboardStats dashboardStats() {
        return DashboardAnalyzer.analyze(personRepository.findAll(), courseRepository.findAll(),
                enrollmentRepository.findAll(), alertRepository.findAll(), bookRepository.findAll(),
                contactRecordRepository.findAll(), LocalDate.now());
    }

    /** 第二階段：報名漏斗。 */
    public FunnelReport enrollmentFunnel() {
        return FunnelAnalyzer.analyze(enrollmentRepository.findAll());
    }

    /** 第二階段：流失風險名單，用自訂 MergeSort 依風險分數遞減穩定排序。 */
    public List<RiskEntry> riskTable() {
        List<Person> people = personRepository.findAll();
        PersonDirectory directory = new PersonDirectory(people);
        List<RiskEntry> entries = RiskAnalyzer.analyze(people, contactRecordRepository.findAll(),
                enrollmentRepository.findAll(), directory, LocalDate.now());
        MergeSort.sort(entries, Comparator.comparingInt(RiskEntry::getRiskScore).reversed());
        return entries;
    }

    /** 第三階段：用自訂 MaxHeap 取風險最高的前 N 位（重新查詢後計算）。 */
    public List<RiskEntry> topRisk(int n) {
        return RiskAnalyzer.topRiskEntries(riskTable(), n);
    }

    /** 第三階段：對已算好的風險名單，用自訂 MaxHeap 取前 N 位。 */
    public List<RiskEntry> topRisk(List<RiskEntry> entries, int n) {
        return RiskAnalyzer.topRiskEntries(entries, n);
    }

    /** 第二階段（加分）：課程先修路徑與循環偵測。 */
    public CoursePathReport coursePath() {
        return CoursePathAnalyzer.analyze(coursePrerequisiteRepository.findAllEdges(), courseRepository.findAll());
    }

    /** 依編號精確查找人員（內部走 PersonDirectory 的 BinarySearch）。 */
    public Optional<Person> jumpToPerson(long personId) {
        return new PersonDirectory(personRepository.findAll()).findById(personId);
    }

    /** 供搜尋效能對照畫面使用。 */
    public PersonDirectory personDirectory() {
        return new PersonDirectory(personRepository.findAll());
    }
}
