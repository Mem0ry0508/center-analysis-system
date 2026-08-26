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
    private void handleLogout() {
        Session.clear();
        SceneRouter.show("/fxml/login.fxml", "中心營運分析系統 - 登入");
    }
}
