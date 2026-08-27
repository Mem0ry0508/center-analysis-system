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
import org.center.algorithm.BinarySearch;
import org.center.algorithm.MergeSort;
import org.center.model.Person;
import org.center.service.PersonService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PersonListController {

    private static final Comparator<Person> BY_ID =
            Comparator.comparingLong(p -> p.getPersonId() == null ? Long.MIN_VALUE : p.getPersonId());
    private static final Comparator<Person> BY_NAME =
            Comparator.comparing(p -> p.getName() == null ? "" : p.getName());

    @FXML private TableView<Person> table;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> genderColumn;
    @FXML private TableColumn<Person, String> occupationColumn;
    @FXML private TableColumn<Person, String> mobilePhoneColumn;
    @FXML private TableColumn<Person, String> emailColumn;
    @FXML private TableColumn<Person, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private TextField jumpIdField;

    private final PersonService personService = new PersonService();
    private final ObservableList<Person> allPeople = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        occupationColumn.setCellValueFactory(new PropertyValueFactory<>("occupation"));
        mobilePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("mobilePhone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        loadData();
    }

    private void loadData() {
        try {
            allPeople.setAll(personService.findAll());
            table.setItems(allPeople);
        } catch (RuntimeException e) {
            showError("讀取人員資料失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            table.setItems(allPeople);
            return;
        }
        List<Person> filtered = allPeople.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains(keyword))
                .toList();
        table.setItems(FXCollections.observableArrayList(filtered));
    }

    /** 依姓名排序：呼叫自訂 MergeSort（stable）。 */
    @FXML
    private void handleSortByName() {
        sortWith(BY_NAME);
    }

    /** 依編號排序：呼叫自訂 MergeSort。 */
    @FXML
    private void handleSortById() {
        sortWith(BY_ID);
    }

    private void sortWith(Comparator<Person> comparator) {
        List<Person> working = new ArrayList<>(table.getItems());
        MergeSort.sort(working, comparator);
        table.setItems(FXCollections.observableArrayList(working));
    }

    /** 跳到指定編號：先用 MergeSort 依編號排序，再用自訂 BinarySearch 精確定位該列。 */
    @FXML
    private void handleJumpToId() {
        String text = jumpIdField.getText() == null ? "" : jumpIdField.getText().trim();
        long id;
        try {
            id = Long.parseLong(text);
        } catch (NumberFormatException e) {
            showInfo("請輸入數字編號");
            return;
        }
        List<Person> sorted = new ArrayList<>(allPeople);
        MergeSort.sort(sorted, BY_ID);
        Person probe = new Person();
        probe.setPersonId(id);
        int index = BinarySearch.search(sorted, probe, BY_ID);
        if (index < 0) {
            showInfo("查無編號 " + id + " 的人員");
            return;
        }
        Person found = sorted.get(index);
        table.setItems(allPeople);
        table.getSelectionModel().select(found);
        table.scrollTo(found);
    }

    @FXML
    private void handleAdd() {
        PersonFormController controller = SceneRouter.openDialog("/fxml/person-form.fxml", "新增人員",
                (PersonFormController c) -> c.setPerson(new Person()));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Person selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆人員資料");
            return;
        }
        PersonFormController controller = SceneRouter.openDialog("/fxml/person-form.fxml", "編輯人員",
                (PersonFormController c) -> c.setPerson(selected));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleDeactivate() {
        Person selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一筆人員資料");
            return;
        }
        try {
            personService.deactivate(selected.getPersonId());
            loadData();
        } catch (RuntimeException e) {
            showError("停用失敗：" + rootMessage(e));
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
