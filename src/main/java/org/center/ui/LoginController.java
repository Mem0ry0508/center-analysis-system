package org.center.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.center.model.Account;
import org.center.service.AuthService;

import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showError("請輸入帳號與密碼");
            return;
        }

        loginButton.setDisable(true);
        try {
            Optional<Account> account = authService.login(username, password);
            if (account.isEmpty()) {
                showError("帳號或密碼錯誤，或帳號已停用");
                return;
            }
            Session.setCurrentAccount(account.get());
            SceneRouter.show("/fxml/main-menu.fxml", "中心營運分析系統 - 主選單");
        } catch (RuntimeException e) {
            showError("登入失敗：" + rootMessage(e));
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private String rootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }
}
