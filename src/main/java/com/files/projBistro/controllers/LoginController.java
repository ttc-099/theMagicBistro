package com.files.projBistro.controllers;

import com.files.projBistro.models.ThemeManager;
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

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;

    private LoginDAO loginDAO = new LoginDAO();

    @FXML
    public void handleLogin(ActionEvent event) {
        String inputUser = usernameField.getText();
        String inputPass = passwordField.getText();

        // ===== VALIDATION: Fields not empty =====
        if (inputUser.isEmpty() || inputPass.isEmpty()) {
            errorLabel.setText("Please enter both a username and a password.");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // ===== VALIDATION: Sanitize inputs =====
        if (inputUser.length() > 50) {
            errorLabel.setText("Your username is too long (max 50 characters)!");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            usernameField.clear();
            passwordField.clear();
            return;
        }

        User loggedInUser = loginDAO.verifyLogin(inputUser, inputPass);

        if (loggedInUser != null) {
            try {
                if (loggedInUser.isAdmin()) {
                    System.out.println("Admin access granted.");
                    switchToDash(event, loggedInUser);
                } else {
                    System.out.println("Customer access granted.");
                    switchToMenu(event, loggedInUser);
                }
            } catch (IOException e) {
                errorLabel.setText("System error: view not found.");
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid username or password.");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            usernameField.clear();
            passwordField.clear();
            usernameField.requestFocus();
        }
    }

    private void switchToMenu(ActionEvent event, User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 1024, 700);

        ThemeManager.applyToScene(scene, getClass());

        MenuController menuController = loader.getController();
        menuController.setLoggedInUser(user);

        stage.setScene(scene);
        stage.setTitle("Camogear Bistro (Menu)");
        stage.show();
    }

    private void switchToDash(ActionEvent event, User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 1024, 700);

        DashController dashController = loader.getController();
        dashController.setLoggedInUser(user);

        stage.setScene(scene);
        stage.setTitle("Camogear Bistro (Admin Dashboard)");
        stage.show();
    }

    @FXML
    private void onLoginButtonHover() {
        loginBtn.setStyle("-fx-cursor: hand;");
    }

    @FXML
    private void onLoginButtonExit() {
        loginBtn.setStyle("-fx-cursor: hand;");
    }

    @FXML
    private void handleShowRegister(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/registerView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 400, 550);
        stage.setScene(scene);
        stage.setTitle("Camogear Bistro (Registration Window)");
        stage.show();
    }
}