package com.files.projBistro.models.controllers;

import com.files.projBistro.models.dao.LoginDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;
    @FXML private Button registerBtn;

    private LoginDAO loginDAO = new LoginDAO();
    private MainController mainController;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();

        // >>>>> VALIDATION: All fields required >>>>>
        if (username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("⚠️ All fields are required.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // >>>>> VALIDATION: Username length >>>>>
        if (username.length() < 3) {
            statusLabel.setText("Your username must be at least 3 characters long.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (username.length() > 50) {
            statusLabel.setText("Your username is too long (max 50 characters)!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // >>>>> VALIDATION: Password strength >>>>>
        if (password.length() < 4) {
            statusLabel.setText("Your password must be at least 4 characters.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (password.length() > 100) {
            statusLabel.setText("Your password is too long (max 100 characters)!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // >>>>> VALIDATION: Phone number (Malaysian format) >>>>>
        if (!phone.matches("0\\d{9,10}")) {
            statusLabel.setText("Please use a Malaysian format: 0123456789 (10-11 digits starting with 0).");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            boolean success = loginDAO.registerUser(username, password, phone, "Customer");
            if (success) {
                statusLabel.setText("Registration successful! Redirecting to login...");
                statusLabel.setStyle("-fx-text-fill: #2ecc71;");

                // Go back to login view after successful registration
                if (mainController != null) {
                    // Small delay so user sees success message
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                    pause.setOnFinished(e -> mainController.showLoginView());
                    pause.play();
                }
            } else {
                statusLabel.setText("That username already exists. Try a different name?");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        if (mainController != null) {
            mainController.showLoginView();
        }
    }

    @FXML
    private void onRegisterButtonHover() {
        registerBtn.setStyle("-fx-cursor: hand;");
    }

    @FXML
    private void onRegisterButtonExit() {
        registerBtn.setStyle("-fx-cursor: hand;");
    }
}