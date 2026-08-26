package org.center.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.center.model.Book;
import org.center.service.InventoryService;

import java.math.BigDecimal;

public class SaleFormController implements DialogController {

    @FXML private ComboBox<Book> bookField;
    @FXML private TextField personIdField;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;
    @FXML private TextField discountField;
    @FXML private TextField invoiceNumberField;

    private final InventoryService inventoryService = new InventoryService();
    private Stage dialogStage;
    private boolean saved;

    @FXML
    private void initialize() {
        StringConverter<Book> converter = new StringConverter<>() {
            @Override
            public String toString(Book book) {
                return book == null ? "" : book.getTitle() + "（庫存 " + book.getCurrentStock() + "）";
            }

            @Override
            public Book fromString(String string) {
                return null;
            }
        };
        bookField.setConverter(converter);
        bookField.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                setText(empty || book == null ? null : converter.toString(book));
            }
        });
    }

    @Override
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setBooks(ObservableList<Book> books, Book preSelected) {
        bookField.setItems(books);
        if (preSelected != null) {
            bookField.setValue(preSelected);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        Book book = bookField.getValue();
        if (book == null) {
            showError("請選擇要銷售的書籍");
            return;
        }
        Long personId;
        int quantity;
        BigDecimal unitPrice;
        BigDecimal discount;
        try {
            String personIdText = personIdField.getText() == null ? "" : personIdField.getText().trim();
            personId = personIdText.isEmpty() ? null : Long.valueOf(personIdText);
            quantity = Integer.parseInt(quantityField.getText().trim());
            unitPrice = new BigDecimal(unitPriceField.getText().trim());
            String discountText = discountField.getText() == null ? "" : discountField.getText().trim();
            discount = discountText.isEmpty() ? BigDecimal.ZERO : new BigDecimal(discountText);
        } catch (RuntimeException e) {
            showError("人員ID／數量／單價／折扣請輸入數字");
            return;
        }
        if (quantity <= 0) {
            showError("數量需大於 0");
            return;
        }

        try {
            inventoryService.recordSale(book.getBookId(), personId, quantity, unitPrice, discount,
                    "receipt", invoiceNumberField.getText());
            saved = true;
            dialogStage.close();
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError("記錄銷售失敗：" + rootMessage(e));
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
