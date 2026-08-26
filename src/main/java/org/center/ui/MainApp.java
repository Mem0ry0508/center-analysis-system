package org.center.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneRouter.init(primaryStage);
        SceneRouter.show("/fxml/login.fxml", "中心營運分析系統 - 登入");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
