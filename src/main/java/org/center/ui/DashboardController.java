package org.center.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import org.center.analytics.DashboardStats;
import org.center.service.AnalyticsService;

public class DashboardController {

    @FXML private Label totalPeopleValue;
    @FXML private Label activePeopleValue;
    @FXML private Label ongoingCourseValue;
    @FXML private Label openAlertValue;
    @FXML private Label lowStockValue;
    @FXML private Label overdueContactValue;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    private void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            DashboardStats stats = analyticsService.dashboardStats();
            totalPeopleValue.setText(String.valueOf(stats.getTotalPeople()));
            activePeopleValue.setText(String.valueOf(stats.getActivePeople()));
            ongoingCourseValue.setText(stats.getOngoingCourses() + " / " + stats.getTotalCourses());
            openAlertValue.setText(String.valueOf(stats.getOpenAlerts()));
            lowStockValue.setText(String.valueOf(stats.getLowStockBooks()));
            overdueContactValue.setText(String.valueOf(stats.getOverdueContacts()));
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
    private void handleGoToAnalytics() {
        SceneRouter.show("/fxml/analytics.fxml", "中心營運分析系統 - 營運分析");
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
