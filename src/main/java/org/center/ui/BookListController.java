package org.center.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.center.model.Book;
import org.center.service.BookService;

public class BookListController {

    @FXML private TableView<Book> table;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, Integer> currentStockColumn;
    @FXML private TableColumn<Book, Integer> safetyStockColumn;
    @FXML private TableColumn<Book, String> supplierColumn;

    private final BookService bookService = new BookService();
    private final ObservableList<Book> books = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        currentStockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        safetyStockColumn.setCellValueFactory(new PropertyValueFactory<>("safetyStock"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        loadData();
    }

    private void loadData() {
        try {
            books.setAll(bookService.findAll());
            table.setItems(books);
        } catch (RuntimeException e) {
            showError("讀取書籍資料失敗：" + rootMessage(e));
        }
    }

    @FXML
    private void handleAdd() {
        BookFormController controller = SceneRouter.openDialog("/fxml/book-form.fxml", "新增書籍",
                (BookFormController c) -> c.setBook(new Book()));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Book selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("請先選擇一本書籍");
            return;
        }
        BookFormController controller = SceneRouter.openDialog("/fxml/book-form.fxml", "編輯書籍",
                (BookFormController c) -> c.setBook(selected));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleRecordSale() {
        Book selected = table.getSelectionModel().getSelectedItem();
        SaleFormController controller = SceneRouter.openDialog("/fxml/sale-form.fxml", "記錄銷售",
                (SaleFormController c) -> c.setBooks(books, selected));
        if (controller.isSaved()) {
            loadData();
        }
    }

    @FXML
    private void handleShowLowStock() {
        try {
            table.setItems(FXCollections.observableArrayList(bookService.findBelowSafetyStock()));
        } catch (RuntimeException e) {
            showError("查詢低庫存書籍失敗：" + rootMessage(e));
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
