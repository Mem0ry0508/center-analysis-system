package org.center.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import org.center.model.Enrollment;
import org.center.service.EnrollmentService;

import java.util.List;
import java.util.Optional;

public class EnrollmentListController {

    @FXML private TableView<Enrollment> table;
    @FXML private TableColumn<Enrollment, String> idColumn;
    @FXML private TableColumn<Enrollment, String> personColumn;
    @FXML private TableColumn<Enrollment, String> courseColumn;
    @FXML private TableColumn<Enrollment, String> statusColumn;
    @FXML private TableColumn<Enrollment, String> amountColumn;
    @FXML private TableColumn<Enrollment, String> attendanceColumn;
    @FXML private TextField searchField;

    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final ObservableList<Enrollment> allEnrollments = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getEnrollmentId())));
        personColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPersonId())));
        courseColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCourseId())));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        amountColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAmount() == null ? "" : c.getValue().getAmount().toPlainString()));
        attendanceColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getLastAttendanceDate() == null ? "" : c.getValue().getLastAttendanceDate().toString()));
        loadData();
    }

    private void loadData() {
        try {
            allEnrollments.setAll(enrollmentService.findAll());
            table.setItems(allEnrollments);
        } catch (RuntimeException e) {
            showError("讀取報名資料失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        if (keyword.isEmpty()) {
            table.setItems(allEnrollments);
            return;
        }
        List<Enrollment> filtered = allEnrollments.stream()
                .filter(e -> String.valueOf(e.getPersonId()).equals(keyword))
                .toList();
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleAdd() {
        EnrollmentFormController controller = SceneRouter.openDialog("/fxml/enrollment-form.fxml", "新增報名",
                (EnrollmentFormController c) -> c.setEnrollment(new Enrollment()));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Enrollment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆報名資料");
            return;
        }
        EnrollmentFormController controller = SceneRouter.openDialog("/fxml/enrollment-form.fxml", "編輯報名",
                (EnrollmentFormController c) -> c.setEnrollment(selected));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleCancel() {
        Enrollment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆報名資料");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setTitle("取消報名");
        dialog.setContentText("取消原因：");
        Optional<String> reason = dialog.showAndWait();
        if (reason.isEmpty()) {
            return;
        }
        try {
            enrollmentService.cancel(selected.getEnrollmentId(), reason.get());
            loadData();
        } catch (RuntimeException e) {
            showError("取消報名失敗：" + rootMessage(e));
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
