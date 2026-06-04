module com.staytrack.app {
    requires java.sql;
    requires com.mysql.cj;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports com.staytrack;
    exports com.staytrack.models;
    opens com.staytrack.models to javafx.base;
}
