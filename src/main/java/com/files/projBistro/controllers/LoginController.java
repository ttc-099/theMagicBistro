package com.files.projBistro.controllers;

import com.files.projBistro.models.dao.LoginDAO;
import com.files.projBistro.models.userModel.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class LoginController {

    // call FXML (ref: loginView.fxml)
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void handleLogin(ActionEvent event) {
        String inputUser = usernameField.getText();
        String inputPass = passwordField.getText();

        LoginDAO dao = new LoginDAO();
        // Renamed the object to loggedInUser to avoid the name clash
        User loggedInUser = dao.verifyLogin(inputUser, inputPass);

        if (loggedInUser != null) {
            try {
                // Check the role and deploy the correct scene
                if (loggedInUser.isAdmin()) {
                    System.out.println("Tactical Auth: Admin Access Granted.");
                    switchToDash(event);
                } else {
                    System.out.println("Tactical Auth: Customer Access Granted.");
                    switchToMenu(event);
                }
            } catch (IOException e) {
                errorLabel.setText("System Error: View not found.");
                e.printStackTrace();
            }
        } else {
            // This is what you're seeing now because the DAO might be returning null
            errorLabel.setText("Invalid credentials");
        }
    }

    private void switchToMenu(ActionEvent event) throws IOException {
        // load the fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));

        // get stage after button click event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // create new scene
        Scene scene = new Scene(loader.load(), 1024, 700);
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Menu");
        stage.show();
    }

    private void switchToDash(ActionEvent event) throws IOException {
        // load the fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashView.fxml"));

        // get stage after button click event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // create new scene
        Scene scene = new Scene(loader.load(), 1024, 700);
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Admin Dashboard");
        stage.show();
    }

}

