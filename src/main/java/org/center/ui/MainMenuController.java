package org.center.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.center.model.Account;

public class MainMenuController {

    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        Account account = Session.getCurrentAccount();
        if (account != null) {
            welcomeLabel.setText("歡迎，" + account.getUsername() + "（角色：" + account.getRole() + "）");
        }
    }

    @FXML
    private void handlePerson() {
        SceneRouter.show("/fxml/person-list.fxml", "中心營運分析系統 - 人員管理");
    }

    @FXML
    private void handleCourse() {
        SceneRouter.show("/fxml/course-list.fxml", "中心營運分析系統 - 課程管理");
    }

    @FXML
    private void handleEnrollment() {
        SceneRouter.show("/fxml/enrollment-list.fxml", "中心營運分析系統 - 報名管理");
    }

    @FXML
    private void handleContact() {
        SceneRouter.show("/fxml/contact-list.fxml", "中心營運分析系統 - 聯絡紀錄");
    }

    @FXML
    private void handleInventory() {
        SceneRouter.show("/fxml/book-list.fxml", "中心營運分析系統 - 庫存管理");
    }

    @FXML
    private void handleDashboard() {
        SceneRouter.show("/fxml/dashboard.fxml", "中心營運分析系統 - 儀表板");
    }

    @FXML
    private void handleAnalytics() {
        SceneRouter.show("/fxml/analytics.fxml", "中心營運分析系統 - 營運分析");
    }

    @FXML
    private void handleAlert() {
        SceneRouter.show("/fxml/alert-list.fxml", "中心營運分析系統 - 優先警示");
    }

    @FXML
    private void handleAudit() {
        SceneRouter.show("/fxml/audit-log.fxml", "中心營運分析系統 - 稽核紀錄");
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        SceneRouter.show("/fxml/login.fxml", "中心營運分析系統 - 登入");
    }
}
