package com.files.projBistro.controllers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;
import javax.swing.*;

public class LoginController {

    // call FXML -- were from loginView
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML // using that fxml...
    public void handleLogin(ActionEvent event) {
        // extract string vals from input field
        String user = usernameField.getText();
        String pass = passwordField.getText();

        //admin check (hard coded sample)
        if (user.equals("admin") && pass.equals("1234")) {
            try {
                switchToMenu(event);
            } catch (IOException e) {
                errorLabel.setText("Error: Could not load the menu screen.");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid credentials. Try again!");
        }
    } // method end

    private void switchToMenu(ActionEvent event) throws IOException {
        // load the fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));

        // get stage after button click event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // create new scene
        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Menu");
        stage.show();
    }








}
