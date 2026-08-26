package org.center.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import org.center.service.AlertService;
import org.center.service.BookService;
import org.center.service.ContactRecordService;
import org.center.service.CourseService;
import org.center.service.PersonService;

public class DashboardController {

    @FXML private Label totalPeopleValue;
    @FXML private Label activePeopleValue;
    @FXML private Label ongoingCourseValue;
    @FXML private Label openAlertValue;
    @FXML private Label lowStockValue;
    @FXML private Label overdueContactValue;

    private final PersonService personService = new PersonService();
    private final CourseService courseService = new CourseService();
    private final BookService bookService = new BookService();
    private final AlertService alertService = new AlertService();
    private final ContactRecordService contactRecordService = new ContactRecordService();

    @FXML
    private void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            var people = personService.findAll();
            long activeCount = people.stream().filter(p -> "active".equals(p.getStatus())).count();
            totalPeopleValue.setText(String.valueOf(people.size()));
            activePeopleValue.setText(String.valueOf(activeCount));

            var courses = courseService.findAll();
            long ongoingCount = courses.stream().filter(c -> "ongoing".equals(c.getStatus())).count();
            ongoingCourseValue.setText(ongoingCount + " / " + courses.size());

            openAlertValue.setText(String.valueOf(alertService.findOpenAlertsByPriority().size()));
            lowStockValue.setText(String.valueOf(bookService.findBelowSafetyStock().size()));
            overdueContactValue.setText(String.valueOf(contactRecordService.findOverdueFollowUps().size()));
        } catch (RuntimeException e) {
            showError("讀取儀表板統計失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleRefresh() {
        loadStats();
    }

    @FXML
    private void handleGoToPerson() {
        SceneRouter.show("/fxml/person-list.fxml", "中心營運分析系統 - 人員管理");
    }

    @FXML
    private void handleGoToCourse() {
        SceneRouter.show("/fxml/course-list.fxml", "中心營運分析系統 - 課程管理");
    }

    @FXML
    private void handleGoToAlert() {
        SceneRouter.show("/fxml/alert-list.fxml", "中心營運分析系統 - 優先警示");
    }

    @FXML
    private void handleGoToBook() {
        SceneRouter.show("/fxml/book-list.fxml", "中心營運分析系統 - 庫存管理");
    }

    @FXML
    private void handleGoToContact() {
        SceneRouter.show("/fxml/contact-list.fxml", "中心營運分析系統 - 聯絡紀錄");
    }

    @FXML
    private void handleBack() {
        SceneRouter.show("/fxml/main-menu.fxml", "中心營運分析系統 - 主選單");
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String rootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }
}
