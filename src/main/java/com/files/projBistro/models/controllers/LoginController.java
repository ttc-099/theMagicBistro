package com.files.projBistro.models.controllers;

import com.files.projBistro.models.dao.LoginDAO;
import com.files.projBistro.models.userModel.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;

    private LoginDAO loginDAO = new LoginDAO();
    private MainController mainController;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String inputUser = usernameField.getText();
        String inputPass = passwordField.getText();

        // check for empty fields
        if (inputUser.isEmpty() || inputPass.isEmpty()) {
            errorLabel.setText("Please enter both a username and a password.");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // username length check
        if (inputUser.length() > 50) {
            errorLabel.setText("Your username is too long (max 50 characters)!");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            usernameField.requestFocus();
            return;
        }

        User loggedInUser = loginDAO.verifyLogin(inputUser, inputPass);

        if (loggedInUser != null) {
            if (loggedInUser.isAdmin()) {
                System.out.println("Admin access granted.");
                if (mainController != null) {
                    mainController.showAdminView(loggedInUser);
                }
            } else {
                System.out.println("Customer access granted.");
                if (mainController != null) {
                    mainController.showMenuView(loggedInUser);
                }
            }
        } else {
            errorLabel.setText("Invalid username or password.");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            usernameField.requestFocus();
            passwordField.clear();
        }
    }

    // just changes cursor to hand on hover
    @FXML
    private void onLoginButtonHover() {
        loginBtn.setStyle("-fx-cursor: hand;");
    }

    @FXML
    private void onLoginButtonExit() {
        loginBtn.setStyle("-fx-cursor: hand;");
    }

    @FXML
    private void handleShowRegister(ActionEvent event) {
        if (mainController != null) {
            mainController.showRegisterView();
        }
    }
}