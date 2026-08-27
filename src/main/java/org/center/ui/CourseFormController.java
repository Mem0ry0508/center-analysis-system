package org.center.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.center.model.Course;
import org.center.service.CourseService;
import org.center.service.PrerequisiteCycleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @FXML private ListView<String> prerequisiteList;
    @FXML private ComboBox<Course> prerequisiteCombo;
    @FXML private Label prerequisiteHint;

    private final CourseService courseService = new CourseService();
    private final Map<Long, String> courseNameById = new HashMap<>();
    private Course course;
    private Stage dialogStage;
    private boolean saved;

    @FXML
    private void initialize() {
        statusField.setItems(FXCollections.observableArrayList("planned", "ongoing", "ended", "suspended"));
        prerequisiteCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Course item) {
                return item == null ? "" : item.getCourseId() + " - " + item.getName();
            }

            @Override
            public Course fromString(String string) {
                return null;
            }
        });
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
        refreshPrerequisites();
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

    @FXML
    private void handleAddPrerequisite() {
        Course selected = prerequisiteCombo.getValue();
        if (selected == null) {
            showError("請先選擇要加入的先修課程");
            return;
        }
        try {
            courseService.addPrerequisite(course.getCourseId(), selected.getCourseId());
            refreshPrerequisites();
        } catch (PrerequisiteCycleException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("新增先修課程失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleRemovePrerequisite() {
        String selected = prerequisiteList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("請先選擇要移除的先修課程");
            return;
        }
        try {
            long prerequisiteId = Long.parseLong(selected.split(" - ", 2)[0].trim());
            courseService.removePrerequisite(course.getCourseId(), prerequisiteId);
            refreshPrerequisites();
        } catch (RuntimeException e) {
            showError("移除先修課程失敗：" + rootMessage(e));
        }
    }

    private void refreshPrerequisites() {
        prerequisiteList.getItems().clear();
        prerequisiteCombo.getItems().clear();
        prerequisiteCombo.setValue(null);

        if (course.getCourseId() == null) {
            prerequisiteList.setDisable(true);
            prerequisiteCombo.setDisable(true);
            prerequisiteHint.setText("先儲存課程後，重新開啟編輯即可設定先修課程");
            return;
        }
        prerequisiteList.setDisable(false);
        prerequisiteCombo.setDisable(false);
        prerequisiteHint.setText("先修課程的新增／移除會立即寫入資料庫，並用 CustomGraph 偵測循環");

        try {
            List<Course> allCourses = courseService.findAll();
            courseNameById.clear();
            for (Course item : allCourses) {
                courseNameById.put(item.getCourseId(), item.getName());
            }
            List<Long> prerequisiteIds = courseService.findPrerequisiteIds(course.getCourseId());
            for (Long id : prerequisiteIds) {
                prerequisiteList.getItems().add(id + " - " + courseNameById.getOrDefault(id, "?"));
            }
            List<Course> options = new ArrayList<>();
            for (Course item : allCourses) {
                if (!item.getCourseId().equals(course.getCourseId()) && !prerequisiteIds.contains(item.getCourseId())) {
                    options.add(item);
                }
            }
            prerequisiteCombo.getItems().setAll(options);
        } catch (RuntimeException e) {
            showError("讀取先修課程失敗：" + rootMessage(e));
        }
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
