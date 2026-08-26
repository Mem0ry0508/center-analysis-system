package org.center.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.center.model.Alert;
import org.center.service.AlertService;

public class AlertFormController implements DialogController {

    @FXML private ComboBox<String> alertTypeField;
    @FXML private Spinner<Integer> severityField;
    @FXML private DatePicker dueDateField;
    @FXML private ComboBox<String> priorityTierField;
    @FXML private TextField relatedPersonIdField;
    @FXML private TextArea triggerReasonField;
    @FXML private TextArea messageField;

    private final AlertService alertService = new AlertService();
    private Alert alert;
    private Stage dialogStage;
    private boolean saved;

    @FXML
    private void initialize() {
        alertTypeField.setItems(FXCollections.observableArrayList(
                "low_stock", "overdue_contact", "incomplete_course", "data_quality"));
        priorityTierField.setItems(FXCollections.observableArrayList("正常", "注意", "優先處理"));
        severityField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
        alertTypeField.setValue(alert.getAlertType());
        if (alert.getSeverity() > 0) {
            severityField.getValueFactory().setValue(alert.getSeverity());
        }
        dueDateField.setValue(alert.getDueDate());
        priorityTierField.setValue(alert.getPriorityTier());
        relatedPersonIdField.setText(alert.getRelatedPersonId() == null ? "" : String.valueOf(alert.getRelatedPersonId()));
        triggerReasonField.setText(alert.getTriggerReason());
        messageField.setText(alert.getMessage());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String triggerReason = triggerReasonField.getText() == null ? "" : triggerReasonField.getText().trim();
        String message = messageField.getText() == null ? "" : messageField.getText().trim();
        if (triggerReason.isEmpty() || message.isEmpty()) {
            showError("觸發原因與訊息為必填");
            return;
        }
        Long relatedPersonId;
        try {
            String text = relatedPersonIdField.getText() == null ? "" : relatedPersonIdField.getText().trim();
            relatedPersonId = text.isEmpty() ? null : Long.valueOf(text);
        } catch (NumberFormatException e) {
            showError("相關人員 ID 請輸入數字");
            return;
        }

        alert.setAlertType(alertTypeField.getValue());
        alert.setSeverity(severityField.getValue());
        alert.setDueDate(dueDateField.getValue());
        alert.setPriorityTier(priorityTierField.getValue());
        alert.setRelatedPersonId(relatedPersonId);
        alert.setTriggerReason(triggerReason);
        alert.setMessage(message);
        try {
            alertService.save(alert);
            saved = true;
            dialogStage.close();
        } catch (RuntimeException e) {
            showError("儲存失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        javafx.scene.control.Alert dialog = new javafx.scene.control.Alert(AlertType.ERROR, message);
        dialog.setHeaderText(null);
        dialog.showAndWait();
    }

    private String rootMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }
}
