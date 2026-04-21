package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.ThemeManager;
import com.files.projBistro.models.userModel.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
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


    }

    private void applyThemeToCurrentScene() {
        Scene currentScene = settingsRoot.getScene();
        if (currentScene != null) {
            ThemeManager.applyToScene(currentScene, getClass());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 1024, 700);
        MenuController menuController = loader.getController();
        menuController.setLoggedInUser(loggedInUser);
        String theme = ThemeManager.getTheme();
        if (theme != null) {
            scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Menu");
        stage.show();
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        // Optional: reset theme on logout? Actually handled in MenuController logout.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 400, 500);
        String theme = ThemeManager.getTheme();
        if (theme != null) {
            scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Login");
        stage.show();
    }
}