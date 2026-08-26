package org.center.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * 統一管理主視窗場景切換與彈出對話框，UI Controller 不直接操作 Stage/FXMLLoader。
 */
public final class SceneRouter {

    private static Stage primaryStage;

    private SceneRouter() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void show(String fxmlPath, String title) {
        Parent root = load(fxmlPath, null);
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 960, 640);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle(title);
        primaryStage.show();
    }

    public static <T> T openDialog(String fxmlPath, String title, Consumer<T> initializer) {
        FXMLLoader loader = new FXMLLoader(SceneRouter.class.getResource(fxmlPath));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("載入對話框失敗：" + fxmlPath, e);
        }

        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);
        Scene scene = new Scene(root);
        applyStylesheet(scene);
        dialog.setScene(scene);

        T controller = loader.getController();
        if (controller instanceof DialogController dialogController) {
            dialogController.setDialogStage(dialog);
        }
        if (initializer != null) {
            initializer.accept(controller);
        }
        dialog.showAndWait();
        return controller;
    }

    private static Parent load(String fxmlPath, Consumer<Object> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneRouter.class.getResource(fxmlPath));
            Parent root = loader.load();
            if (initializer != null) {
                initializer.accept(loader.getController());
            }
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("載入畫面失敗：" + fxmlPath, e);
        }
    }

    private static void applyStylesheet(Scene scene) {
        scene.getStylesheets().add(SceneRouter.class.getResource("/fxml/app.css").toExternalForm());
    }
}
