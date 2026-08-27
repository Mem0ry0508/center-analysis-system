package org.center.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.center.model.Course;
import org.center.model.Enrollment;
import org.center.service.CourseService;
import org.center.service.EnrollmentService;

import java.math.BigDecimal;
import java.util.List;

public class EnrollmentFormController implements DialogController {

    private static final List<String> STAGES =
            List.of("contacted", "introduced", "registered", "started", "completed", "cancelled");

    @FXML private TextField personIdField;
    @FXML private ComboBox<Course> courseField;
    @FXML private TextField amountField;
    @FXML private TextField paymentTypeField;
    @FXML private ComboBox<String> statusField;
    @FXML private DatePicker lastAttendanceField;
    @FXML private TextField cancelReasonField;

    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final CourseService courseService = new CourseService();
    private Enrollment enrollment;
    private Stage dialogStage;
    private boolean saved;

    @FXML
    private void initialize() {
        statusField.setItems(FXCollections.observableArrayList(STAGES));
        courseField.setConverter(new StringConverter<>() {
            @Override
            public String toString(Course course) {
                return course == null ? "" : course.getCourseId() + " - " + course.getName();
            }

            @Override
            public Course fromString(String string) {
                return null;
            }
        });
        try {
            courseField.setItems(FXCollections.observableArrayList(courseService.findAll()));
        } catch (RuntimeException e) {
            showError("讀取課程清單失敗：" + rootMessage(e));
        }
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
        personIdField.setText(enrollment.getPersonId() == null ? "" : String.valueOf(enrollment.getPersonId()));
        for (Course course : courseField.getItems()) {
            if (course.getCourseId().equals(enrollment.getCourseId())) {
                courseField.setValue(course);
            }
        }
        amountField.setText(enrollment.getAmount() == null ? "" : enrollment.getAmount().toPlainString());
        paymentTypeField.setText(enrollment.getPaymentType());
        statusField.setValue(enrollment.getStatus() == null ? "contacted" : enrollment.getStatus());
        lastAttendanceField.setValue(enrollment.getLastAttendanceDate());
        cancelReasonField.setText(enrollment.getCancelReason());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        Long personId;
        BigDecimal amount;
        try {
            String personText = personIdField.getText() == null ? "" : personIdField.getText().trim();
            if (personText.isEmpty()) {
                showError("學員編號為必填");
                return;
            }
            personId = Long.valueOf(personText);
            String amountText = amountField.getText() == null ? "" : amountField.getText().trim();
            amount = amountText.isEmpty() ? BigDecimal.ZERO : new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            showError("學員編號與金額請輸入數字");
            return;
        }
        Course course = courseField.getValue();
        if (course == null) {
            showError("請選擇課程");
            return;
        }

        enrollment.setPersonId(personId);
        enrollment.setCourseId(course.getCourseId());
        enrollment.setAmount(amount);
        enrollment.setPaymentType(paymentTypeField.getText());
        enrollment.setStatus(statusField.getValue());
        enrollment.setLastAttendanceDate(lastAttendanceField.getValue());
        enrollment.setCancelReason(cancelReasonField.getText());
        try {
            enrollmentService.save(enrollment);
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
