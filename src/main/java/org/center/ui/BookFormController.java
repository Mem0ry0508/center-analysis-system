package org.center.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.center.model.Book;
import org.center.service.BookService;

import java.math.BigDecimal;

public class BookFormController implements DialogController {

    @FXML private TextField titleField;
    @FXML private TextField isbnField;
    @FXML private TextField authorField;
    @FXML private TextField categoryField;
    @FXML private TextField supplierField;
    @FXML private TextField costField;
    @FXML private TextField listPriceField;
    @FXML private TextField storageLocationField;
    @FXML private TextField safetyStockField;
    @FXML private TextField currentStockField;

    private final BookService bookService = new BookService();
    private Book book;
    private Stage dialogStage;
    private boolean saved;

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setBook(Book book) {
        this.book = book;
        titleField.setText(book.getTitle());
        isbnField.setText(book.getIsbn());
        authorField.setText(book.getAuthor());
        categoryField.setText(book.getCategory());
        supplierField.setText(book.getSupplier());
        costField.setText(book.getCost() == null ? "" : book.getCost().toPlainString());
        listPriceField.setText(book.getListPrice() == null ? "" : book.getListPrice().toPlainString());
        storageLocationField.setText(book.getStorageLocation());
        safetyStockField.setText(book.getBookId() == null ? "5" : String.valueOf(book.getSafetyStock()));
        currentStockField.setText(book.getBookId() == null ? "0" : String.valueOf(book.getCurrentStock()));
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            showError("書名為必填");
            return;
        }
        BigDecimal cost;
        BigDecimal listPrice;
        int safetyStock;
        int currentStock;
        try {
            cost = new BigDecimal(costField.getText().trim());
            listPrice = new BigDecimal(listPriceField.getText().trim());
            safetyStock = Integer.parseInt(safetyStockField.getText().trim());
            currentStock = Integer.parseInt(currentStockField.getText().trim());
        } catch (RuntimeException e) {
            showError("成本／售價／庫存欄位請輸入數字");
            return;
        }

        book.setTitle(title);
        book.setIsbn(isbnField.getText());
        book.setAuthor(authorField.getText());
        book.setCategory(categoryField.getText());
        book.setSupplier(supplierField.getText());
        book.setCost(cost);
        book.setListPrice(listPrice);
        book.setStorageLocation(storageLocationField.getText());
        book.setSafetyStock(safetyStock);
        book.setCurrentStock(currentStock);
        try {
            bookService.save(book);
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
