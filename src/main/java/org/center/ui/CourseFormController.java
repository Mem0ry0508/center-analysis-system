package org.center.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.center.model.Course;
import org.center.service.CourseService;

public class CourseFormController implements DialogController {

    @FXML private TextField nameField;
    @FXML private TextField instructorIdField;
    @FXML private TextField capacityField;
    @FXML private TextField paymentTypeField;
    @FXML private TextField classTimeSlotField;
    @FXML private TextField classroomField;
    @FXML private DatePicker startDateField;
    @FXML private DatePicker completionDateField;
    @FXML private DatePicker firstClassDateField;
    @FXML private ComboBox<String> statusField;
    @FXML private TextField suspendReasonField;

    private final CourseService courseService = new CourseService();
    private Course course;
    private Stage dialogStage;
    private boolean saved;

    @FXML
    private void initialize() {
        statusField.setItems(FXCollections.observableArrayList("planned", "active", "completed", "cancelled"));
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setCourse(Course course) {
        this.course = course;
        nameField.setText(course.getName());
        instructorIdField.setText(course.getInstructorId() == null ? "" : String.valueOf(course.getInstructorId()));
        capacityField.setText(course.getCapacity() == 0 ? "" : String.valueOf(course.getCapacity()));
        paymentTypeField.setText(course.getPaymentType());
        classTimeSlotField.setText(course.getClassTimeSlot());
        classroomField.setText(course.getClassroom());
        startDateField.setValue(course.getStartDate());
        completionDateField.setValue(course.getCompletionDate());
        firstClassDateField.setValue(course.getFirstClassDate());
        statusField.setValue(course.getStatus() == null ? "planned" : course.getStatus());
        suspendReasonField.setText(course.getSuspendReason());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            showError("課程名稱為必填");
            return;
        }
        Long instructorId;
        int capacity;
        try {
            String instructorIdText = instructorIdField.getText() == null ? "" : instructorIdField.getText().trim();
            instructorId = instructorIdText.isEmpty() ? null : Long.valueOf(instructorIdText);
            String capacityText = capacityField.getText() == null ? "" : capacityField.getText().trim();
            capacity = capacityText.isEmpty() ? 0 : Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            showError("授課老師ID與名額請輸入數字");
            return;
        }

        course.setName(name);
        course.setInstructorId(instructorId);
        course.setCapacity(capacity);
        course.setPaymentType(paymentTypeField.getText());
        course.setClassTimeSlot(classTimeSlotField.getText());
        course.setClassroom(classroomField.getText());
        course.setStartDate(startDateField.getValue());
        course.setCompletionDate(completionDateField.getValue());
        course.setFirstClassDate(firstClassDateField.getValue());
        course.setStatus(statusField.getValue());
        course.setSuspendReason(suspendReasonField.getText());
        try {
            courseService.save(course);
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
