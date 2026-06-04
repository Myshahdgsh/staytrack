package com.staytrack.controllers;

import com.staytrack.services.AuthService;
import com.staytrack.util.AlertUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {
    private final Stage stage;
    private final AuthService authService = new AuthService();

    public LoginController(Stage stage) {
        this.stage = stage;
    }

    public Parent getView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("login-root");

        Label brand = new Label("StayTrack");
        brand.getStyleClass().add("brand");
        Label subtitle = new Label("Hostel Management System");
        subtitle.getStyleClass().add("subtitle");

        TextField username = new TextField("admin");
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setText("admin123");
        CheckBox remember = new CheckBox("Remember me");
        Button login = new Button("Login");
        login.getStyleClass().add("primary-button");
        login.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(14, brand, subtitle, username, password, remember, login);
        box.getStyleClass().add("login-card");
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(360);
        box.setPadding(new Insets(36));
        root.setCenter(box);

        login.setOnAction(event -> {
            if (authService.login(username.getText(), password.getText(), remember.isSelected())) {
                Scene scene = new Scene(new MainController(stage).getView(), stage.getScene().getWidth(), stage.getScene().getHeight());
                scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
                stage.setScene(scene);
            } else {
                AlertUtil.error("Login failed", "Invalid username or password.");
            }
        });
        return root;
    }
}
