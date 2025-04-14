package nntc.isip.myshop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Dialog;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

public class StatisticsController {
    @FXML
    private LineChart<String, Number> orderChart;

    private Stage stage; // Ссылка на главное окно
    private DatabaseManager primaryDatabaseManager; // Ссылка на главное окно

    public void setPrimaryDatabaseManager(DatabaseManager dm) {
        this.primaryDatabaseManager = dm;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void handleMenuClose(ActionEvent event) {

        System.out.println("Метод handleMenuClose вызван.");

        // Проверяем, инициализирован ли Stage
        if (stage != null) {
            boolean shouldClose = showCloseConfirmationDialog();
            if (shouldClose) {
                System.out.println("Пользователь подтвердил закрытие. Окно будет закрыто.");
                // Получаем настройки
                Preferences prefs = Preferences.userNodeForPackage(DesktopApplication.class);
                prefs.putDouble("windowX", stage.getX());
                prefs.putDouble("windowY", stage.getY());
                prefs.putDouble("windowWidth", stage.getWidth());
                prefs.putDouble("windowHeight", stage.getHeight());

                if (primaryDatabaseManager != null) {
                    this.primaryDatabaseManager.disconnect();
                }

                stage.close(); // Закрытие окна
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
        // alert.setContentText("Все несохраненные данные будут потеряны.");

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

        stageLogin.setOnCloseRequest(event -> {
            System.out.println("Закрытие окна настроек доступа к СУБД...");
            dialog.close(); // Закрыть диалог
        });

        dialog.showAndWait();
    }

    public void chartTest() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Заказы за неделю");

        series.getData().add(new XYChart.Data<>("Понедельник", 10));
        series.getData().add(new XYChart.Data<>("Вторник", 2));
        series.getData().add(new XYChart.Data<>("Среда", 30));
        series.getData().add(new XYChart.Data<>("Четверг", 10));
        series.getData().add(new XYChart.Data<>("Пятница", 12));
        series.getData().add(new XYChart.Data<>("Суббота", 18));
        series.getData().add(new XYChart.Data<>("Воскресенье", 7));

        orderChart.getData().add(series);
    }

    public void chartTesClear() {
        orderChart.getData().clear();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void saveExcelFile() {
        if (stage == null) {
            System.err.println("Stage не инициализирован.");
            return;
        }

        // Создаём диалог выбора файла
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить Excel-файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel файл (*.xls)", "*.xls"));
        fileChooser.setInitialFileName("example.xls");

        // Показываем диалог
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveExcel(file);
        } else {
            System.out.println("Сохранение отменено.");
        }
    }

}
