package org.center.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.center.model.Account;
import org.center.model.ContactRecord;
import org.center.service.ContactRecordService;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ContactRecordFormController implements DialogController {

    @FXML private TextField personIdField;
    @FXML private DatePicker contactDateField;
    @FXML private TextField contactTimeField;
    @FXML private ComboBox<String> methodField;
    @FXML private TextArea contentField;
    @FXML private Spinner<Integer> moodRatingField;
    @FXML private ComboBox<String> resultField;
    @FXML private TextField followUpActionField;
    @FXML private DatePicker nextContactDateField;

    private final ContactRecordService contactRecordService = new ContactRecordService();
    private ContactRecord contactRecord;
    private Stage dialogStage;
    private boolean saved;

    @FXML
    private void initialize() {
        methodField.setItems(FXCollections.observableArrayList("phone", "email", "line", "letter", "in_person"));
        resultField.setItems(FXCollections.observableArrayList(
                "connected", "no_answer", "left_message", "declined", "rescheduled"));
        moodRatingField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setContactRecord(ContactRecord contactRecord) {
        this.contactRecord = contactRecord;
        personIdField.setText(contactRecord.getPersonId() == null ? "" : String.valueOf(contactRecord.getPersonId()));
        LocalDateTime contactDate = contactRecord.getContactDate() == null ? LocalDateTime.now() : contactRecord.getContactDate();
        contactDateField.setValue(contactDate.toLocalDate());
        contactTimeField.setText(String.format("%02d:%02d", contactDate.getHour(), contactDate.getMinute()));
        methodField.setValue(contactRecord.getMethod());
        contentField.setText(contactRecord.getContent());
        if (contactRecord.getMoodRating() != null) {
            moodRatingField.getValueFactory().setValue(contactRecord.getMoodRating());
        }
        resultField.setValue(contactRecord.getResult());
        followUpActionField.setText(contactRecord.getFollowUpAction());
        nextContactDateField.setValue(contactRecord.getNextContactDate());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        Long personId;
        LocalTime time;
        try {
            String personIdText = personIdField.getText() == null ? "" : personIdField.getText().trim();
            personId = Long.valueOf(personIdText);
            time = LocalTime.parse(padTime(contactTimeField.getText()));
        } catch (RuntimeException e) {
            showError("人員 ID 需為數字、聯絡時間請用 HH:mm 格式");
            return;
        }
        if (contactDateField.getValue() == null) {
            showError("請選擇聯絡日期");
            return;
        }

        contactRecord.setPersonId(personId);
        contactRecord.setContactDate(contactDateField.getValue().atTime(time));
        contactRecord.setMethod(methodField.getValue());
        contactRecord.setContent(contentField.getText());
        contactRecord.setMoodRating(moodRatingField.getValue());
        contactRecord.setResult(resultField.getValue());
        contactRecord.setFollowUpAction(followUpActionField.getText());
        contactRecord.setNextContactDate(nextContactDateField.getValue());
        Account current = Session.getCurrentAccount();
        if (contactRecord.getCreatedBy() == null && current != null) {
            contactRecord.setCreatedBy(current.getAccountId());
        }
        try {
            contactRecordService.save(contactRecord);
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

    private String padTime(String text) {
        String trimmed = text == null ? "" : text.trim();
        return trimmed.isEmpty() ? "00:00" : trimmed;
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
