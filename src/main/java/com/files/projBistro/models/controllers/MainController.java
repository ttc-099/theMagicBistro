package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.ThemeManager;
import com.files.projBistro.models.userModel.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML private BorderPane mainRoot;
    @FXML private StackPane contentArea;

    private Scene mainScene;

    @FXML
    public void initialize() {
        showLoginView();
    } // start at login screen

    // login and register should always be light mode
    public void setMainScene(Scene scene) {
        this.mainScene = scene;
        // force light mode for login
        forceLightMode();
    }

    // removes any dark mode css so login looks right
    private void forceLightMode() {
        if (mainScene != null) {
            mainScene.getStylesheets().removeIf(sheet ->
                    sheet.contains("dark.css") || sheet.contains("light.css")
            );
            URL lightCss = getClass().getResource("/styles/light.css");
            if (lightCss != null) {
                mainScene.getStylesheets().add(lightCss.toExternalForm());
            }
        }
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginView.fxml"));
            Node loginView = loader.load();

            LoginController loginController = loader.getController();
            loginController.setMainController(this);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loginView);

            // Ensure light mode is applied
            forceLightMode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMenuView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));
            Node menuView = loader.load();

            MenuController menuController = loader.getController();
            menuController.setLoggedInUser(user);
            menuController.setMainController(this);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(menuView);

            // Apply saved theme using mainScene
            if (mainScene != null) {
                String savedTheme = ThemeManager.getTheme();
                URL cssUrl = getClass().getResource(savedTheme != null ? savedTheme : "/styles/light.css");
                if (cssUrl != null) {
                    mainScene.getStylesheets().clear();
                    mainScene.getStylesheets().add(cssUrl.toExternalForm());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAdminView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashView.fxml"));
            Node adminView = loader.load();

            DashController dashController = loader.getController();
            dashController.setLoggedInUser(user);
            dashController.setMainController(this);  // Pass reference

            contentArea.getChildren().clear();
            contentArea.getChildren().add(adminView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/registerView.fxml"));
            Node registerView = loader.load();

            RegisterController registerController = loader.getController();
            registerController.setMainController(this);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(registerView);

            // keep registration screen light
            forceLightMode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}