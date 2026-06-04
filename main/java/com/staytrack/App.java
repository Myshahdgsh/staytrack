package com.staytrack;

import com.staytrack.controllers.LoginController;
import com.staytrack.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        DatabaseInitializer.initialize();
        Scene scene = new Scene(new LoginController(stage).getView(), 1080, 700);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setTitle("StayTrack - Hostel Management System");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
