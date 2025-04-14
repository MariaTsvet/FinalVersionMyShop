package nntc.isip.myshop;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

public class ErrorDialog {

    public static void showError(String title, String message) {
        // Создаём модальное окно
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // Убираем заголовок
        alert.setContentText(message);

        // Делаем окно модальным
        alert.initModality(Modality.APPLICATION_MODAL);

        // Отображаем окно и ждём, пока пользователь закроет его
        alert.showAndWait();
    }
}
