package nntc.isip.myshop;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class OrdersController {


    @FXML
    private TableView<Order> tableView;
    @FXML
    private TableColumn<Order, Integer> idColumn;
    @FXML
    public TableColumn<Order, String> dateColumn;
    @FXML
    public TableColumn<Order, String> customerColumn;

    @FXML
    public TextField fieldID;

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
        ObservableList<Order> data = primaryDatabaseManager.orderFetchData();
        idColumn.setCellValueFactory(new PropertyValueFactory<Order, Integer>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("date"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("customer"));
        tableView.setItems(data);
        fieldID.clear();
    }

    @FXML
    public void addRow() {
        // открываем форму для нового заказа
        try {
            showOrderWindow(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fieldID.clear();
    }

    @FXML
    public void editRow() {
        // открываем форму существующего заказа
        try {
            showOrderWindow(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fieldID.clear();
    }

    @FXML
    public void deleteRow() {
        if (showConfirmationDialog(String.format("Действительно удалить заказ №%s", fieldID.getText()))) {
            primaryDatabaseManager.orderDeleteData(Integer.parseInt(fieldID.getText()));
            updateTable();
            fieldID.clear();
        }
    }

    // Обработчик для клика по строкам в TableView
    @FXML
    private void onRowClick(MouseEvent event) {
        if (event.getClickCount() == 1) {  // Обработка одиночного клика
            Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                fieldID.setText(String.format("%d", selectedOrder.getId()));
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
            disableEditOrDeleteBtnsFlag = (newValue.trim().isEmpty());
            btnEdit.setDisable(disableEditOrDeleteBtnsFlag);
            btnDelete.setDisable(disableEditOrDeleteBtnsFlag);
        });

    }

    public void showOrderWindow(boolean modeEdit) throws IOException {
        // Загружаем FXML файл для окна справки
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("order-view.fxml"));

        VBox infoContent = fxmlLoader.load(); // Загружаем содержимое окна

        OrderController controller = fxmlLoader.getController();
        controller.setPrimaryDatabaseManager(primaryDatabaseManager);

        if(modeEdit) {
            controller.setOrderId(fieldID.getText());
        }

        // Создаем диалоговое окно
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Заказ");

        dialog.getDialogPane().setContent(infoContent); // Добавляем содержимое в диалоговое окно

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        stage.setResizable(true);
        stage.setMaxWidth(1920);
        stage.setMaxHeight(1080);

        // Обработчик закрытия окна
        stage.setOnCloseRequest(event -> {
            System.out.println("Закрытие окна управления деталями заказа...");
            dialog.close(); // Закрыть диалог
            updateTable();
        });

        // Показываем диалог в модальном режиме
        dialog.showAndWait();
    }

}
