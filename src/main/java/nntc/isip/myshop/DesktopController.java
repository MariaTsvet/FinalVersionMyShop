package nntc.isip.myshop;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Dialog;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

public class DesktopController {

    @FXML
    private LineChart<String, Number> orderChart;

    private Stage primaryStage; // Ссылка на главное окно
    private DatabaseManager primaryDatabaseManager; // Ссылка на главное окно

    // Метод для инициализации Stage из Application
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setPrimaryDatabaseManager(DatabaseManager dm) {
        this.primaryDatabaseManager = dm;
    }

    public void handleMenuClose(ActionEvent event) {

        System.out.println("Метод handleMenuClose вызван.");

        // Проверяем, инициализирован ли Stage
        if (primaryStage != null) {
            boolean shouldClose = showCloseConfirmationDialog();
            if (shouldClose) {
                System.out.println("Пользователь подтвердил закрытие. Окно будет закрыто.");
                // Получаем настройки
                Preferences prefs = Preferences.userNodeForPackage(DesktopApplication.class);
                prefs.putDouble("windowX", primaryStage.getX());
                prefs.putDouble("windowY", primaryStage.getY());
                prefs.putDouble("windowWidth", primaryStage.getWidth());
                prefs.putDouble("windowHeight", primaryStage.getHeight());
                String username = "user%";

                if (primaryDatabaseManager != null) {
                    this.primaryDatabaseManager.disconnect();
                }

                primaryStage.close(); // Закрытие окна
            } else {
                System.out.println("Пользователь отменил закрытие.");
            }
        } else {
            System.err.println("Stage не был установлен!");
        }
    }

    private boolean showCloseConfirmationDialog() {
        // Создаем диалог подтверждения
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение закрытия");
        alert.setHeaderText("Вы уверены, что хотите выйти?");
        alert.setContentText("Все несохраненные данные будут потеряны.");

        Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(icon);

        // Ожидание ответа пользователя
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void showinfoWindow() throws IOException {
        // Загружаем FXML файл для окна справки
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("info-view.fxml"));
        VBox infoContent = fxmlLoader.load(); // Загружаем содержимое окна справки

        // Создаем диалоговое окно
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("О программе");
        
        dialog.getDialogPane().setContent(infoContent); // Добавляем содержимое в диалоговое окно

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        // Обработчик закрытия окна
        stage.setOnCloseRequest(event -> {
            System.out.println("Закрытие окна справки...");
            dialog.close(); // Закрыть диалог
        });

        // Показываем диалог в модальном режиме
        dialog.showAndWait();
    }

    public void showLoginWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        VBox infoContent = fxmlLoader.load();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Настройки доступа к СУБД");
        dialog.getDialogPane().setContent(infoContent);
        Stage stageLogin = (Stage) dialog.getDialogPane().getScene().getWindow();
        stageLogin.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        stageLogin.setOnCloseRequest(event -> {
            System.out.println("Закрытие окна настроек доступа к СУБД...");
            dialog.close(); // Закрыть диалог
        });

        dialog.showAndWait();
    }

    public void showProductsWindow(ActionEvent actionEvent) throws IOException {
            // Загружаем FXML файл для окна справки
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("products-view.fxml"));

            VBox infoContent = fxmlLoader.load(); // Загружаем содержимое окна справки

            ProductsController controller = fxmlLoader.getController();
            controller.setPrimaryDatabaseManager(primaryDatabaseManager);

            // Создаем диалоговое окно
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Управление товарами");

            dialog.getDialogPane().setContent(infoContent); // Добавляем содержимое в диалоговое окно

            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));


            // Обработчик закрытия окна
            stage.setOnCloseRequest(event -> {
                System.out.println("Закрытие окна с товарами...");
                dialog.close(); // Закрыть диалог
            });

            // Показываем диалог в модальном режиме
            dialog.showAndWait();
    }

    public void showCustomersWindow(ActionEvent actionEvent) throws IOException {
        // Загружаем FXML файл для окна справки
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("customers-view.fxml"));

        VBox infoContent = fxmlLoader.load(); // Загружаем содержимое окна справки

        CustomersController controller = fxmlLoader.getController();
        controller.setPrimaryDatabaseManager(primaryDatabaseManager);

        // Создаем диалоговое окно
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Управление покупателями");

        dialog.getDialogPane().setContent(infoContent); // Добавляем содержимое в диалоговое окно

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        // Обработчик закрытия окна
        stage.setOnCloseRequest(event -> {
            System.out.println("Закрытие окна с покупателями...");
            dialog.close(); // Закрыть диалог
        });

        // Показываем диалог в модальном режиме
        dialog.showAndWait();
    }

    public void showOrdersWindow(ActionEvent actionEvent) throws IOException {
        // Загружаем FXML файл для окна справки
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("orders-view.fxml"));

        VBox infoContent = fxmlLoader.load(); // Загружаем содержимое окна справки

        OrdersController controller = fxmlLoader.getController();
        controller.setPrimaryDatabaseManager(primaryDatabaseManager);

        // Создаем диалоговое окно
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Список заказов");

        dialog.getDialogPane().setContent(infoContent); // Добавляем содержимое в диалоговое окно

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        // Обработчик закрытия окна
        stage.setOnCloseRequest(event -> {
            System.out.println("Закрытие окна с заказами...");
            dialog.close(); // Закрыть диалог
        });

        // Показываем диалог в модальном режиме
        dialog.showAndWait();
    }

    public void showStatisticsWindow(ActionEvent actionEvent) throws IOException {
        // Загружаем FXML файл для окна справки
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("statistics.fxml"));

        VBox infoContent = fxmlLoader.load(); // Загружаем содержимое окна справки

        StatisticsController controller = fxmlLoader.getController();
        controller.setPrimaryDatabaseManager(primaryDatabaseManager);

        // Создаем диалоговое окно
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Статистика");

        dialog.getDialogPane().setContent(infoContent); // Добавляем содержимое в диалоговое окно

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

        // Обработчик закрытия окна
        stage.setOnCloseRequest(event -> {
            System.out.println("Закрытие окна статистики...");
            dialog.close(); // Закрыть диалог
        });

        // Показываем диалог в модальном режиме
        dialog.showAndWait();
    }


    private void saveExcel(File file) {
        try (Workbook workbook = new HSSFWorkbook()) {
            // Создаем 3 листа
            for (int i = 1; i <= 3; i++) {
                Sheet sheet = workbook.createSheet("Лист " + i);

                // Заполняем 2 строки по 3 столбца
                for (int rowNum = 0; rowNum < 2; rowNum++) {
                    Row row = sheet.createRow(rowNum);
                    for (int colNum = 0; colNum < 3; colNum++) {
                        Cell cell = row.createCell(colNum);
                        cell.setCellValue("Лист " + i + " R" + (rowNum + 1) + "C" + (colNum + 1));
                    }
                }
            }

            // Сохранение файла
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                System.out.println("Excel-файл сохранён: " + file.getAbsolutePath());
                showAlert("Файл сохранён", "Файл успешно сохранён:\n" + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сохранить файл: " + e.getMessage());
        }
    }

    public void saveExcelFile() {
        if (primaryStage == null) {
            System.err.println("Stage не инициализирован.");
            return;
        }

        // Создаём диалог выбора файла
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить Excel-файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файл (*.xls)", "*.xls"));
        fileChooser.setInitialFileName("example.xls");

        // Показываем диалог
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            saveExcel(file);
        } else {
            System.out.println("Сохранение отменено.");
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}