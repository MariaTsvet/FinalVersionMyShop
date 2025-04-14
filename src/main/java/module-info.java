module nntc.isip.myshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs; // Класс Preferences позволяет сохранять данные между сеансами приложения
    requires java.sql;
    requires java.desktop;
    requires org.apache.poi.poi;


    opens nntc.isip.myshop to javafx.fxml;
    exports nntc.isip.myshop;
}