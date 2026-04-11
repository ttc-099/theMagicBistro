package com.files.projBistro.controllers;

import com.files.projBistro.models.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class SettingsController {

    @FXML private VBox settingsRoot;
    @FXML private ToggleButton themeToggle;

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(ThemeManager.getTheme()).toExternalForm());
    }

    @FXML
    private void handleThemeChange() {
        String theme = themeToggle.isSelected() ? "/styles/dark.css" : "/styles/light.css";
        ThemeManager.setTheme(theme);

        // apply it immediately to the settings window to see it change
        applyTheme(settingsRoot.getScene());
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 1. Create the Menu scene
        Scene scene = new Scene(loader.load(), 900, 600);

        // 2. Check the Manager and apply the "Paint"
        String theme = ThemeManager.getTheme();
        if (theme != null) {
            scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Menu");
        stage.show();
    }
}