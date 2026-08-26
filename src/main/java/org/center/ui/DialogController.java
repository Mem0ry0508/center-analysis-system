package org.center.ui;

import javafx.stage.Stage;

/**
 * SceneRouter.openDialog 載入的 FXML Controller 若需要自行關閉視窗（儲存/取消後），
 * 實作此介面以取得對應的 Stage。
 */
public interface DialogController {
    void setDialogStage(Stage stage);
}
