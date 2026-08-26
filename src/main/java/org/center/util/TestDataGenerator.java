package org.center.util;

import org.center.model.Account;
import org.center.model.Book;
import org.center.model.ContactRecord;
import org.center.model.Course;
import org.center.model.Enrollment;
import org.center.model.Person;
import org.center.repository.AccountRepository;
import org.center.repository.BookRepository;
import org.center.repository.ContactRecordRepository;
import org.center.repository.CoursePrerequisiteRepository;
import org.center.repository.CourseRepository;
import org.center.repository.EnrollmentRepository;
import org.center.repository.InventoryTransactionRepository;
import org.center.repository.PersonRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灌測試資料：200 人、20 課程、50 書、1000 筆聯絡/註冊/交易紀錄（分工計畫要求的最低量）。
 * 用固定亂數種子，讓每次跑出來的資料量與分佈一致，方便重跑與效能測試比較。
 * 執行方式：mvn compile exec:java -Dexec.mainClass=org.center.util.TestDataGenerator
 */
public final class TestDataGenerator {

    private static final Random RANDOM = new Random(42);
    private static final String TEST_ACCOUNT_PASSWORD = "password123";

    private static final int PEOPLE_COUNT = 200;
    private static final int COURSE_COUNT = 20;
    private static final int BOOK_COUNT = 50;
    private static final int ENROLLMENT_COUNT = 400;
    private static final int CONTACT_RECORD_COUNT = 400;
    private static final int INVENTORY_TRANSACTION_COUNT = 200;

    private static final String[] SURNAMES = {
            "陳", "林", "黃", "張", "李", "王", "吳", "劉", "蔡", "楊",
            "許", "鄭", "謝", "洪", "郭", "邱", "曾", "廖", "賴", "徐"
    };
    private static final String[] GIVEN_CHARS = {
            "家", "怡", "淑", "建", "雅", "志", "佳", "承", "詩", "俊",
            "美", "慧", "婷", "豪", "宏", "芬", "偉", "穎", "涵", "傑",
            "安", "樂", "欣", "妍", "皓", "睿", "語", "恩", "平", "琪"
    };
    private static final String[] CONTACT_SOURCES = {"walk_in", "referral", "online_ad", "event", "phone_inquiry"};
    private static final String[] OCCUPATIONS = {"學生", "工程師", "教師", "家管", "自由業", "退休", "服務業", "公職"};
    private static final String[] PREFERRED_CHANNELS = {"phone", "email", "line", "in_person"};
    private static final String[] CONTACT_METHODS = {"phone", "email", "line", "letter", "in_person"};
    private static final String[] CONTACT_RESULTS = {"connected", "no_answer", "left_message", "declined", "rescheduled"};
    private static final String[] ENROLLMENT_STATUSES =
            {"contacted", "introduced", "registered", "started", "completed", "cancelled"};
    private static final String[] COURSE_NAMES = {
            "英文會話", "書法研習", "電腦基礎", "瑜珈", "烘焙", "攝影", "日文入門", "繪畫", "太極拳", "陶藝",
            "合唱團", "投資理財", "手作皮件", "園藝", "戲劇表演", "肌力訓練", "兒童繪本", "詩詞賞析", "咖啡沖煮", "桌遊策略"
    };
    private static final String[] COURSE_TIME_SLOTS = {"週一上午", "週二下午", "週三晚間", "週四上午", "週五下午", "週六上午"};
    private static final String[] PAYMENT_TYPES = {"cash", "credit_card", "transfer", "installment"};
    private static final String[] BOOK_CATEGORIES = {"文學", "商業", "心理", "歷史", "科普", "藝術", "語言", "教育", "生活", "健康"};
    private static final String[] BOOK_SUPPLIERS = {"三民書局", "天下文化", "誠品經銷", "聯經出版", "遠流經銷"};

    private final AccountRepository accountRepository = new AccountRepository();
    private final PersonRepository personRepository = new PersonRepository();
    private final CourseRepository courseRepository = new CourseRepository();
    private final CoursePrerequisiteRepository coursePrerequisiteRepository = new CoursePrerequisiteRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
    private final ContactRecordRepository contactRecordRepository = new ContactRecordRepository();
    private final InventoryTransactionRepository inventoryTransactionRepository = new InventoryTransactionRepository();

    public static void main(String[] args) {
        TestDataGenerator generator = new TestDataGenerator();
        long start = System.currentTimeMillis();

        List<Long> accountIds = generator.generateAccounts(5);
        System.out.println("accounts: " + accountIds.size() + "（帳號 staff1~staff" + accountIds.size()
                + "，測試密碼統一為 \"" + TEST_ACCOUNT_PASSWORD + "\"）");

        List<Long> personIds = generator.generatePeople(PEOPLE_COUNT, accountIds);
        System.out.println("people: " + personIds.size());

        List<Long> courseIds = generator.generateCourses(COURSE_COUNT, personIds);
        System.out.println("courses: " + courseIds.size());

        int prereqCount = generator.generateCoursePrerequisites(courseIds);
        System.out.println("course_prerequisites: " + prereqCount);

        List<Long> bookIds = generator.generateBooks(BOOK_COUNT);
        System.out.println("books: " + bookIds.size());

        int enrollmentCount = generator.generateEnrollments(ENROLLMENT_COUNT, personIds, courseIds, accountIds);
        System.out.println("enrollments: " + enrollmentCount);

        int contactCount = generator.generateContactRecords(CONTACT_RECORD_COUNT, personIds, accountIds);
        System.out.println("contact_records: " + contactCount);

        int transactionCount = generator.generateInventoryTransactions(INVENTORY_TRANSACTION_COUNT, bookIds, personIds);
        System.out.println("inventory_transactions: " + transactionCount);

        long elapsedMs = System.currentTimeMillis() - start;
        System.out.println("完成，耗時 " + elapsedMs + " ms");
    }

    List<Long> generateAccounts(int count) {
        List<Long> ids = new ArrayList<>();
        String[] roles = {"admin", "registrar", "finance", "central_file", "classroom", "bookstore"};
        for (int i = 1; i <= count; i++) {
            Account account = new Account();
            account.setUsername("staff" + i);
            account.setPasswordHash(PasswordUtil.hash(TEST_ACCOUNT_PASSWORD));
            account.setRole(roles[(i - 1) % roles.length]);
            account.setActive(true);
            account.setFailedLoginCount(0);
            ids.add(accountRepository.save(account).getAccountId());
        }
        return ids;
    }

    List<Long> generatePeople(int count, List<Long> accountIds) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Person p = new Person();
            String name = randomChineseName();
            p.setName(name);
            p.setStartDate(randomDateBetween(LocalDate.of(2023, 1, 1), LocalDate.of(2026, 8, 1)));
            p.setContactSource(randomFrom(CONTACT_SOURCES));
            p.setGender(RANDOM.nextBoolean() ? "M" : "F");
            p.setOccupation(randomFrom(OCCUPATIONS));
            p.setMobilePhone(randomMobilePhone());
            p.setEmail("person" + i + "@example.com");
            p.setBirthday(randomDateBetween(LocalDate.of(1950, 1, 1), LocalDate.of(2008, 12, 31)));
            p.setContactable(RANDOM.nextInt(10) > 0);
            p.setMailable(RANDOM.nextInt(10) > 1);
            p.setPreferredChannel(randomFrom(PREFERRED_CHANNELS));
            p.setStatus("active");
            p.setEnteredBy(randomFrom(accountIds));
            ids.add(personRepository.save(p).getPersonId());
        }
        return ids;
    }

    List<Long> generateCourses(int count, List<Long> personIds) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Course c = new Course();
            c.setName(COURSE_NAMES[i % COURSE_NAMES.length] + (i >= COURSE_NAMES.length ? "-進階" : ""));
            c.setInstructorId(randomFrom(personIds));
            c.setCapacity(10 + RANDOM.nextInt(20));
            c.setPaymentType(randomFrom(PAYMENT_TYPES));
            c.setClassTimeSlot(randomFrom(COURSE_TIME_SLOTS));
            c.setClassroom("教室" + (1 + RANDOM.nextInt(6)));
            LocalDate start = randomDateBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 6, 1));
            c.setStartDate(start);
            c.setCompletionDate(start.plusMonths(2 + RANDOM.nextInt(4)));
            c.setFirstClassDate(start.plusDays(RANDOM.nextInt(7)));
            c.setStatus(randomFrom(new String[]{"planned", "ongoing", "ended"}));
            ids.add(courseRepository.save(c).getCourseId());
        }
        return ids;
    }

    /**
     * 依索引順序建立先修關係（course[i] 的先修課只會是 course[j], j<i），保證是 DAG 不會有循環，
     * 供 B 的 CustomGraph 拓撲排序測試使用。
     */
    int generateCoursePrerequisites(List<Long> courseIds) {
        int count = 0;
        for (int i = 1; i < courseIds.size(); i++) {
            if (RANDOM.nextInt(3) == 0) {
                int prereqIndex = RANDOM.nextInt(i);
                coursePrerequisiteRepository.addPrerequisite(courseIds.get(i), courseIds.get(prereqIndex));
                count++;
            }
        }
        return count;
    }

    List<Long> generateBooks(int count) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Book b = new Book();
            String category = randomFrom(BOOK_CATEGORIES);
            b.setTitle(category + "選集 第" + (i + 1) + "冊");
            b.setIsbn("978" + String.format("%010d", RANDOM.nextInt(1_000_000_000)));
            b.setAuthor(randomChineseName());
            b.setCategory(category);
            b.setSupplier(randomFrom(BOOK_SUPPLIERS));
            BigDecimal cost = BigDecimal.valueOf(100 + RANDOM.nextInt(300));
            b.setCost(cost);
            b.setListPrice(cost.multiply(BigDecimal.valueOf(1.5)));
            b.setStorageLocation("架位" + (char) ('A' + RANDOM.nextInt(6)) + (1 + RANDOM.nextInt(20)));
            b.setSafetyStock(5 + RANDOM.nextInt(10));
            b.setCurrentStock(20 + RANDOM.nextInt(100));
            ids.add(bookRepository.save(b).getBookId());
        }
        return ids;
    }

    int generateEnrollments(int count, List<Long> personIds, List<Long> courseIds, List<Long> accountIds) {
        int created = 0;
        int attempts = 0;
        while (created < count && attempts < count * 5) {
            attempts++;
            Enrollment e = new Enrollment();
            e.setPersonId(randomFrom(personIds));
            e.setCourseId(randomFrom(courseIds));
            e.setRegistrarId(randomFrom(accountIds));
            e.setAmount(BigDecimal.valueOf(1000 + RANDOM.nextInt(5000)));
            e.setPaymentType(randomFrom(PAYMENT_TYPES));
            String status = randomFrom(ENROLLMENT_STATUSES);
            e.setStatus(status);
            if ("started".equals(status) || "completed".equals(status)) {
                e.setLastAttendanceDate(randomDateBetween(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 8, 1)));
            }
            if ("cancelled".equals(status)) {
                e.setCancelReason("學員時間無法配合");
            }
            try {
                enrollmentRepository.save(e);
                created++;
            } catch (RuntimeException duplicatePersonCourse) {
                // person+course 已存在（UNIQUE 約束），略過重試下一組
            }
        }
        return created;
    }

    int generateContactRecords(int count, List<Long> personIds, List<Long> accountIds) {
        for (int i = 0; i < count; i++) {
            ContactRecord c = new ContactRecord();
            c.setPersonId(randomFrom(personIds));
            c.setContactDate(randomDateTimeBetween(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 8, 25)));
            c.setMethod(randomFrom(CONTACT_METHODS));
            c.setContent("例行關懷聯絡紀錄 #" + i);
            c.setMoodRating(1 + RANDOM.nextInt(5));
            c.setResult(randomFrom(CONTACT_RESULTS));
            if (RANDOM.nextBoolean()) {
                c.setFollowUpAction("安排下次回訪");
                c.setNextContactDate(randomDateBetween(LocalDate.of(2026, 8, 26), LocalDate.of(2026, 12, 31)));
            }
            c.setCreatedBy(randomFrom(accountIds));
            contactRecordRepository.save(c);
        }
        return count;
    }

    int generateInventoryTransactions(int count, List<Long> bookIds, List<Long> personIds) {
        int created = 0;
        int attempts = 0;
        while (created < count && attempts < count * 5) {
            attempts++;
            boolean isSale = RANDOM.nextBoolean();
            Long bookId = randomFrom(bookIds);
            if (isSale) {
                int quantity = 1 + RANDOM.nextInt(3);
                BigDecimal unitPrice = BigDecimal.valueOf(150 + RANDOM.nextInt(200));
                try {
                    inventoryTransactionRepository.recordSale(bookId, randomFrom(personIds), quantity,
                            unitPrice, BigDecimal.ZERO, "duplicate", "INV-" + String.format("%06d", created));
                    created++;
                } catch (IllegalStateException stockNotEnough) {
                    // 該書庫存不足，換下一組重試
                }
            } else {
                var t = new org.center.model.InventoryTransaction();
                t.setBookId(bookId);
                t.setTransactionType("purchase");
                t.setSupplier(randomFrom(BOOK_SUPPLIERS));
                int quantity = 5 + RANDOM.nextInt(20);
                t.setQuantity(quantity);
                BigDecimal unitPrice = BigDecimal.valueOf(80 + RANDOM.nextInt(150));
                t.setUnitPrice(unitPrice);
                t.setDiscount(BigDecimal.ZERO);
                t.setNetAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)));
                t.setDocumentNumber("PO-" + String.format("%06d", created));
                t.setInspectionStatus("accepted");
                inventoryTransactionRepository.save(t);
                bookRepository.findById(bookId).ifPresent(book -> {
                    book.setCurrentStock(book.getCurrentStock() + quantity);
                    bookRepository.update(book);
                });
                created++;
            }
        }
        return created;
    }

    private String randomChineseName() {
        String surname = randomFrom(SURNAMES);
        int givenLength = 1 + RANDOM.nextInt(2);
        StringBuilder given = new StringBuilder();
        for (int i = 0; i < givenLength; i++) {
            given.append(randomFrom(GIVEN_CHARS));
        }
        return surname + given;
    }

    private String randomMobilePhone() {
        StringBuilder sb = new StringBuilder("09");
        for (int i = 0; i < 8; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private LocalDate randomDateBetween(LocalDate start, LocalDate end) {
        long startEpoch = start.toEpochDay();
        long endEpoch = end.toEpochDay();
        long randomEpoch = startEpoch + (long) (RANDOM.nextDouble() * (endEpoch - startEpoch));
        return LocalDate.ofEpochDay(randomEpoch);
    }

    private LocalDateTime randomDateTimeBetween(LocalDate start, LocalDate end) {
        LocalDate date = randomDateBetween(start, end);
        return date.atTime(RANDOM.nextInt(24), RANDOM.nextInt(60));
    }

    private <T> T randomFrom(T[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    private <T> T randomFrom(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }
}
