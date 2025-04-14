package nntc.isip.myshop;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class LoginController {

    // поля ввода из login-view.fxml
    public TextField subdAddress;
    public TextField subdPort;
    public TextField subdDbname;
    public TextField subdSchema;
    public TextField subdUser;
    public TextField subdPassword;

    // настройки, сохраняемые в пространстве пользователя
    private Preferences prefs;

    // метод жизненного цикла контроллера JavaFX, который вызывается после отрисовки FXML
    // здесь можно считать сохранённые данные из Preferences или подставить умолчальные
    // (если пока сохранённых данных нет -- если это первый запуск и данные ещё не сохранялись)
    @FXML
    public void initialize() {
        prefs = Preferences.userNodeForPackage(DesktopApplication.class);
        subdAddress.setText(prefs.get("subdAddress", "localhost"));
        subdPort.setText(prefs.get("subdPort", "5432"));
        subdDbname.setText(prefs.get("subdDbname", "postgres"));
        subdSchema.setText(prefs.get("subdSchema", "public"));
        subdUser.setText(prefs.get("subdUser", "postgres"));
        subdPassword.setText(prefs.get("subdPassword", "postgres"));
    }

    // метод, вызываемый при нажатии на кнопку "Подключиться!"
    @FXML
    public void connect(ActionEvent actionEvent) {

        // сохраняем параметры в Preferences
        prefs.put("subdAddress", subdAddress.getText());
        prefs.put("subdPort", subdPort.getText());
        prefs.put("subdDbname", subdDbname.getText());
        prefs.put("subdSchema", subdSchema.getText());
        prefs.put("subdUser", subdUser.getText());
        prefs.put("subdPassword", subdPassword.getText());

        // формируем информационное сообщение
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Перезапустите приложение!");
        a.setHeaderText("Перезапустите приложение!");
        a.setContentText("Данные для подключения к БД сохранены.");
        a.show(); // показываем (оно висит, пока пользователь на отреагирует)

        // закрываем окно программы, когда пользователь отреагировал...
        Stage s = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        s.close();
    }
}
