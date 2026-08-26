package org.center.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.center.model.Course;
import org.center.service.CourseService;

import java.util.List;
import java.util.Locale;

public class CourseListController {

    @FXML private TableView<Course> table;
    @FXML private TableColumn<Course, String> nameColumn;
    @FXML private TableColumn<Course, Long> instructorIdColumn;
    @FXML private TableColumn<Course, Integer> capacityColumn;
    @FXML private TableColumn<Course, String> classTimeSlotColumn;
    @FXML private TableColumn<Course, String> classroomColumn;
    @FXML private TableColumn<Course, String> statusColumn;
    @FXML private TextField searchField;

    private final CourseService courseService = new CourseService();
    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        instructorIdColumn.setCellValueFactory(new PropertyValueFactory<>("instructorId"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        classTimeSlotColumn.setCellValueFactory(new PropertyValueFactory<>("classTimeSlot"));
        classroomColumn.setCellValueFactory(new PropertyValueFactory<>("classroom"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadData();
    }

    private void loadData() {
        try {
            allCourses.setAll(courseService.findAll());
            table.setItems(allCourses);
        } catch (RuntimeException e) {
            showError("讀取課程資料失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            table.setItems(allCourses);
            return;
        }
        List<Course> filtered = allCourses.stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase(Locale.ROOT).contains(keyword))
                .toList();
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleAdd() {
        CourseFormController controller = SceneRouter.openDialog("/fxml/course-form.fxml", "新增課程",
                (CourseFormController c) -> c.setCourse(new Course()));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Course selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆課程資料");
            return;
        }
        CourseFormController controller = SceneRouter.openDialog("/fxml/course-form.fxml", "編輯課程",
                (CourseFormController c) -> c.setCourse(selected));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleDeactivate() {
        Course selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆課程資料");
            return;
        }
        try {
            courseService.deactivate(selected.getCourseId());
            loadData();
        } catch (RuntimeException e) {
            showError("停課失敗：" + rootMessage(e));
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
