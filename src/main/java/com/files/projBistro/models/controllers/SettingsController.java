package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.ThemeManager;
import com.files.projBistro.models.userModel.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class SettingsController {

    @FXML private VBox settingsRoot;
    @FXML private ToggleButton themeToggle;

    private User loggedInUser;
    private MenuController menuController;  // Reference to parent MenuController

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public void setMenuController(MenuController controller) {
        this.menuController = controller;
    }

    @FXML
    public void initialize() {
        String currentTheme = ThemeManager.getTheme();
        if (currentTheme != null && currentTheme.contains("dark")) {
            themeToggle.setSelected(true);
            themeToggle.setText("Dark Mode");
        } else {
            themeToggle.setSelected(false);
            themeToggle.setText("Light Mode");
        }
        applyThemeToCurrentScene();
    }

    @FXML
    private void handleThemeChange() {
        SoundHelper.playTapSound();

        String newTheme;
        if (themeToggle.isSelected()) {
            newTheme = "/styles/dark.css";
            themeToggle.setText("Dark Mode");
        } else {
            newTheme = "/styles/light.css";
            themeToggle.setText("Light Mode");
        }
        ThemeManager.setTheme(newTheme);
        applyThemeToCurrentScene();

        // Refresh the menu controller if it exists
        if (menuController != null) {
            menuController.refreshTheme();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Theme Changed");
        alert.setHeaderText(null);
        alert.setContentText("Theme has been changed.");
        alert.showAndWait();
    }

    private void applyThemeToCurrentScene() {
        Scene currentScene = settingsRoot.getScene();
        if (currentScene != null) {
            ThemeManager.applyToScene(currentScene, getClass());
        }
    }

    @FXML
    public void handleBackToMenu() {
        SoundHelper.playTapSound();

        if (menuController != null) {
            menuController.refreshTheme();  // Ensure theme is applied
            menuController.showMenuView();
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {

        SoundHelper.playTapSound();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 400, 500);
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro (Login)");
        stage.show();
    }
}