package org.center.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.center.model.Person;
import org.center.service.PersonService;

public class PersonFormController implements DialogController {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> genderField;
    @FXML private TextField occupationField;
    @FXML private TextField mobilePhoneField;
    @FXML private TextField emailField;
    @FXML private TextField lineIdField;
    @FXML private DatePicker birthdayField;
    @FXML private ComboBox<String> statusField;
    @FXML private TextArea noteField;

    private final PersonService personService = new PersonService();
    private Person person;
    private Stage dialogStage;
    private boolean saved;

    @FXML
    private void initialize() {
        genderField.setItems(FXCollections.observableArrayList("男", "女", "其他"));
        statusField.setItems(FXCollections.observableArrayList("active", "inactive"));
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setPerson(Person person) {
        this.person = person;
        nameField.setText(person.getName());
        genderField.setValue(person.getGender());
        occupationField.setText(person.getOccupation());
        mobilePhoneField.setText(person.getMobilePhone());
        emailField.setText(person.getEmail());
        lineIdField.setText(person.getLineId());
        birthdayField.setValue(person.getBirthday());
        statusField.setValue(person.getStatus() == null ? "active" : person.getStatus());
        noteField.setText(person.getNote());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            showError("姓名為必填");
            return;
        }
        person.setName(name);
        person.setGender(genderField.getValue());
        person.setOccupation(occupationField.getText());
        person.setMobilePhone(mobilePhoneField.getText());
        person.setEmail(emailField.getText());
        person.setLineId(lineIdField.getText());
        person.setBirthday(birthdayField.getValue());
        person.setStatus(statusField.getValue());
        person.setNote(noteField.getText());
        try {
            personService.save(person);
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
