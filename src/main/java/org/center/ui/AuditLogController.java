package org.center.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.center.algorithm.MergeSort;
import org.center.model.AuditLog;
import org.center.service.AuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 稽核紀錄檢視（唯讀）。audit_logs 為 append-only，畫面上沒有新增／修改／刪除。
 * 顯示前用自訂 MergeSort 依時間新到舊排序。
 */
public class AuditLogController {

    @FXML private TableView<AuditLog> table;
    @FXML private TableColumn<AuditLog, String> timeColumn;
    @FXML private TableColumn<AuditLog, String> actorColumn;
    @FXML private TableColumn<AuditLog, String> actionColumn;
    @FXML private TableColumn<AuditLog, String> tableColumn;
    @FXML private TableColumn<AuditLog, String> recordColumn;
    @FXML private TableColumn<AuditLog, String> reasonColumn;

    private final AuditService auditService = new AuditService();

    @FXML
    private void initialize() {
        timeColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().toString()));
        actorColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getActorId() == null ? "系統" : String.valueOf(c.getValue().getActorId())));
        actionColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));
        tableColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTableName()));
        recordColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRecordId() == null ? "" : String.valueOf(c.getValue().getRecordId())));
        reasonColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReason()));
        loadData();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        try {
            List<AuditLog> logs = new ArrayList<>(auditService.findAll());
            MergeSort.sort(logs, Comparator.comparing(
                    (AuditLog l) -> l.getCreatedAt() == null ? LocalDateTime.MIN : l.getCreatedAt()).reversed());
            table.setItems(FXCollections.observableArrayList(logs));
        } catch (RuntimeException e) {
            showError("讀取稽核紀錄失敗：" + rootMessage(e));
        }
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
