package smartpay.lbycpd2.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import smartpay.lbycpd2.SmartPayApplication;
import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() throws IOException {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        // Hardcoded Master Admin Login
        if (user.equals("admin") && pass.equals("admin123")) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(SmartPayApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            scene.getStylesheets().add(SmartPayApplication.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
        } else {
            errorLabel.setText("Invalid admin credentials.");
        }
    }
}