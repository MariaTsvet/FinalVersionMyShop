package nntc.isip.myshop;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Optional;

public class ProductsController {

    @FXML
    public ImageView imageView;

    @FXML
    public Button imageUploadButton;

    @FXML
    private TableView<Product> tableView;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, Float> priceColumn;

    @FXML
    public TextField fieldID;

    @FXML
    public TextField fieldName;

    @FXML
    public TextField fieldPrice;

    @FXML
    public Button btnEdit;

    @FXML
    public Button btnDelete;

    private Boolean disableEditOrDeleteBtnsFlag = true;


    // private Stage primaryStage; // Ссылка на главное окно
    private DatabaseManager primaryDatabaseManager;

    public void setPrimaryDatabaseManager(DatabaseManager dm) {
        this.primaryDatabaseManager = dm;
    }

    private boolean showConfirmationDialog(String message) {
        // Создаем диалог подтверждения
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение действия");
        alert.setHeaderText(message);
        // alert.setContentText("Все несохраненные данные будут потеряны.");

        // Ожидание ответа пользователя
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    public void updateTable() {
        ObservableList<Product> data = primaryDatabaseManager.productsFetchData();
        idColumn.setCellValueFactory(new PropertyValueFactory<Product, Integer>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<Product, Float>("price"));
        tableView.setItems(data);
    }

    @FXML
    public void addRow() {
        primaryDatabaseManager.productsInsertData(fieldName.getText(), Float.parseFloat(fieldPrice.getText()));
        updateTable();
        fieldID.clear();
        fieldName.clear();
        fieldPrice.clear();
    }

    @FXML
    public void editRow() {
        primaryDatabaseManager.productsUpdateData(Integer.parseInt(fieldID.getText()), fieldName.getText(), Float.parseFloat(fieldPrice.getText().trim()));
        updateTable();
        fieldID.clear();
        fieldName.clear();
        fieldPrice.clear();
    }

    @FXML
    public void deleteRow() {
        if (showConfirmationDialog(String.format("Действительно удалить запись %s с ID=%s и ценой %s?", fieldName.getText(), fieldID.getText(), fieldPrice.getText()))) {
            primaryDatabaseManager.productsDeleteData(Integer.parseInt(fieldID.getText()));
            updateTable();
            fieldID.clear();
            fieldName.clear();
            fieldPrice.clear();
        }
    }

    // Обработчик для клика по строкам в TableView
    @FXML
    private void onRowClick(MouseEvent event) {
        if (event.getClickCount() == 1) {  // Обработка одиночного клика
            Product selectedProduct = tableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                fieldID.setText(String.format("%d", selectedProduct.getId()));
                fieldName.setText(selectedProduct.getName());
                fieldPrice.setText(String.format("%s", selectedProduct.getPrice()));
                loadImageFromDatabase(Integer.parseInt(fieldID.getText().trim()));
            }
        }
    }

    // Запустить updateTable() сразу после отрисовки fxml-разметки
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            System.out.println("TableView был отрисован.");
            btnEdit.setDisable(disableEditOrDeleteBtnsFlag);
            btnDelete.setDisable(disableEditOrDeleteBtnsFlag);
            updateTable();
        });

        // Добавляем слушатель на изменение текста в TextField
        fieldID.textProperty().addListener((observable, oldValue, newValue) -> {
            // Если поле не пустое, активируем кнопку, иначе деактивируем
            disableEditOrDeleteBtnsFlag = (newValue.trim().isEmpty() && fieldName.getText().isEmpty());
            btnEdit.setDisable(disableEditOrDeleteBtnsFlag);
            btnDelete.setDisable(disableEditOrDeleteBtnsFlag);
        });

        fieldName.textProperty().addListener((observable, oldValue, newValue) -> {
            // Если поле не пустое, активируем кнопку, иначе деактивируем
            disableEditOrDeleteBtnsFlag = (newValue.trim().isEmpty() && fieldID.getText().isEmpty());
            btnEdit.setDisable(disableEditOrDeleteBtnsFlag);
            btnDelete.setDisable(disableEditOrDeleteBtnsFlag);
        });

        fieldPrice.textProperty().addListener((observable, oldValue, newValue) -> {
            // Если поле не пустое, активируем кнопку, иначе деактивируем
            disableEditOrDeleteBtnsFlag = (newValue.trim().isEmpty() && fieldID.getText().isEmpty());
            btnEdit.setDisable(disableEditOrDeleteBtnsFlag);
            btnDelete.setDisable(disableEditOrDeleteBtnsFlag);
        });

        imageUploadButton.setOnAction(event -> openFileChooser());

    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null && !fieldID.getText().isEmpty()) {
            displayImage(file);
            saveImageToDatabase(file, Integer.parseInt(fieldID.getText().trim()));
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация!");
            alert.setHeaderText("Для установки изображения выберите товар");
            alert.showAndWait();
        }
    }

    private void displayImage(File file) {
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);

    }

    private void saveImageToDatabase(File file, int productId) {
        primaryDatabaseManager.saveProductImage(file, productId);
    }

    private void loadImageFromDatabase(int productId) {
        imageView.setImage(primaryDatabaseManager.getProductImage(productId));
    }


}