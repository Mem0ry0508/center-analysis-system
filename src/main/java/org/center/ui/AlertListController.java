package org.center.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.center.model.Alert;
import org.center.service.AlertGenerationService;
import org.center.service.AlertService;

public class AlertListController {

    @FXML private TableView<Alert> table;
    @FXML private TableColumn<Alert, String> alertTypeColumn;
    @FXML private TableColumn<Alert, Integer> severityColumn;
    @FXML private TableColumn<Alert, String> dueDateColumn;
    @FXML private TableColumn<Alert, String> priorityTierColumn;
    @FXML private TableColumn<Alert, String> messageColumn;
    @FXML private TableColumn<Alert, String> statusColumn;

    private final AlertService alertService = new AlertService();
    private final AlertGenerationService alertGenerationService = new AlertGenerationService();

    @FXML
    private void initialize() {
        alertTypeColumn.setCellValueFactory(new PropertyValueFactory<>("alertType"));
        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        priorityTierColumn.setCellValueFactory(new PropertyValueFactory<>("priorityTier"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadOpenAlerts();
    }

    /**
     * 未結案警示用 AlertService 的 MaxHeap 排序輸出，最緊急的排最上面。
     */
    private void loadOpenAlerts() {
        try {
            table.setItems(FXCollections.observableArrayList(alertService.findOpenAlertsByPriority()));
        } catch (RuntimeException e) {
            showError("讀取警示失敗：" + rootMessage(e));
        }
    }

    private void loadAllAlerts() {
        try {
            table.setItems(FXCollections.observableArrayList(alertService.findAll()));
        } catch (RuntimeException e) {
            showError("讀取警示失敗：" + rootMessage(e));
        }
    }

    /** 未結案警示改用 MinHeap 依到期日排序（最早到期／已逾期在最上面）。 */
    @FXML
    private void handleSortByDueDate() {
        try {
            table.setItems(FXCollections.observableArrayList(alertService.findOpenAlertsByDueDate()));
        } catch (RuntimeException e) {
            showError("讀取警示失敗：" + rootMessage(e));
        }
    }

    /** 掃描低庫存／逾期回訪／逾期課程，產生新的未結案警示。 */
    @FXML
    private void handleGenerate() {
        try {
            int created = alertGenerationService.generateAll();
            showInfo("本次新增 " + created + " 筆警示");
            loadOpenAlerts();
        } catch (RuntimeException e) {
            showError("產生警示失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleAdd() {
        AlertFormController controller = SceneRouter.openDialog("/fxml/alert-form.fxml", "新增警示",
                (AlertFormController c) -> c.setAlert(new Alert()));
        if (controller.isSaved()) {
            loadOpenAlerts();
        }
    }

    @FXML
    private void handleResolve() {
        Alert selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆警示");
            return;
        }
        try {
            alertService.resolve(selected.getAlertId());
            loadOpenAlerts();
        } catch (RuntimeException e) {
            showError("標記已解決失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleShowAll() {
        loadAllAlerts();
    }

    @FXML
    private void handleRefresh() {
        loadOpenAlerts();
    }

    @FXML
    private void handleBack() {
        SceneRouter.show("/fxml/main-menu.fxml", "中心營運分析系統 - 主選單");
    }

    private void showInfo(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(AlertType.ERROR, message);
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
