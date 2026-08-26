package org.center.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.center.model.ContactRecord;
import org.center.service.ContactRecordService;

public class ContactRecordListController {

    @FXML private TableView<ContactRecord> table;
    @FXML private TableColumn<ContactRecord, Long> personIdColumn;
    @FXML private TableColumn<ContactRecord, String> contactDateColumn;
    @FXML private TableColumn<ContactRecord, String> methodColumn;
    @FXML private TableColumn<ContactRecord, String> resultColumn;
    @FXML private TableColumn<ContactRecord, String> nextContactDateColumn;

    private final ContactRecordService contactRecordService = new ContactRecordService();
    private final ObservableList<ContactRecord> records = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        personIdColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("personId"));
        contactDateColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("contactDate"));
        methodColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("method"));
        resultColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("result"));
        nextContactDateColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nextContactDate"));
        loadData();
    }

    private void loadData() {
        try {
            records.setAll(contactRecordService.findAll());
            table.setItems(records);
        } catch (RuntimeException e) {
            showError("讀取聯絡紀錄失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleAdd() {
        ContactRecordFormController controller = SceneRouter.openDialog("/fxml/contact-form.fxml", "新增聯絡紀錄",
                (ContactRecordFormController c) -> c.setContactRecord(new ContactRecord()));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        ContactRecord selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆聯絡紀錄");
            return;
        }
        ContactRecordFormController controller = SceneRouter.openDialog("/fxml/contact-form.fxml", "編輯聯絡紀錄",
                (ContactRecordFormController c) -> c.setContactRecord(selected));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleDelete() {
        ContactRecord selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆聯絡紀錄");
            return;
        }
        try {
            contactRecordService.delete(selected.getContactId());
            loadData();
        } catch (RuntimeException e) {
            showError("刪除失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleBack() {
        SceneRouter.show("/fxml/main-menu.fxml", "中心營運分析系統 - 主選單");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
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
