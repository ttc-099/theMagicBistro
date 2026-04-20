package com.files.projBistro.controllers;

import com.files.projBistro.controllers.admin.AdminController;
import com.files.projBistro.models.userModel.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.io.IOException;

public class DashController {

    @FXML
    private VBox contentArea;  // changed from StackPane to VBox
    @FXML
    private Label statusLabel;

    // menu item containers and labels
    @FXML private HBox inventoryMenuItem;
    @FXML private HBox ordersMenuItem;
    @FXML private HBox stockMenuItem;

    @FXML private Label inventoryLabel;
    @FXML private Label ordersLabel;
    @FXML private Label stockLabel;

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (user != null) {
            statusLabel.setText("Welcome, Admin " + user.getUsername() + "!");
        }
    }

    @FXML
    public void initialize() {
        System.out.println("Admin Dashboard initialized...");

        // setup hover effects after the UI is loaded
        Platform.runLater(() -> setupHoverEffects());
    }

    private void setupHoverEffects() {
        // inventory menu hover
        if (inventoryMenuItem != null && inventoryLabel != null) {
            inventoryMenuItem.setOnMouseEntered(e -> {
                inventoryMenuItem.setStyle("-fx-background-color: #3498db; -fx-padding: 10 15 10 15; -fx-background-radius: 8;");
                inventoryLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            });
            inventoryMenuItem.setOnMouseExited(e -> {
                inventoryMenuItem.setStyle("-fx-background-color: transparent; -fx-padding: 10 15 10 15; -fx-background-radius: 8;");
                inventoryLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");
            });
        }

        // orders menu hover
        if (ordersMenuItem != null && ordersLabel != null) {
            ordersMenuItem.setOnMouseEntered(e -> {
                ordersMenuItem.setStyle("-fx-background-color: #3498db; -fx-padding: 10 15 10 15; -fx-background-radius: 8;");
                ordersLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            });
            ordersMenuItem.setOnMouseExited(e -> {
                ordersMenuItem.setStyle("-fx-background-color: transparent; -fx-padding: 10 15 10 15; -fx-background-radius: 8;");
                ordersLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");
            });
        }

        // stock alerts menu hover
        if (stockMenuItem != null && stockLabel != null) {
            stockMenuItem.setOnMouseEntered(e -> {
                stockMenuItem.setStyle("-fx-background-color: #3498db; -fx-padding: 10 15 10 15; -fx-background-radius: 8;");
                stockLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            });
            stockMenuItem.setOnMouseExited(e -> {
                stockMenuItem.setStyle("-fx-background-color: transparent; -fx-padding: 10 15 10 15; -fx-background-radius: 8;");
                stockLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");
            });
        }
    }

    @FXML
    private void showOrders() {
        statusLabel.setText("Viewing Live Order Feed...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activeOrdersView.fxml"));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
            // make the loaded view expand to fill the VBox
            if (node instanceof Region) {
                VBox.setVgrow((Region) node, Priority.ALWAYS);
            }
            statusLabel.setText("Active Orders");
        } catch (IOException e) {
            statusLabel.setText("Error: Could not load orders view");
            e.printStackTrace();
        }
    }

    @FXML
    private void showStockAlerts() {
        statusLabel.setText("Checking Stock Levels...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lowStockView.fxml"));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
            if (node instanceof Region) {
                VBox.setVgrow((Region) node, Priority.ALWAYS);
            }
            statusLabel.setText("Low Stock Alerts");
        } catch (IOException e) {
            statusLabel.setText("Error: Could not load stock alerts view");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 400, 500);
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Login");
        stage.show();
    }

    @FXML
    private void showMenuManagement() {
        // removed pin check here - now only checked in AdminController when that view loads
        System.out.println("Loading adminView.fxml...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminView.fxml"));
            Node node = loader.load();

            AdminController adminController = loader.getController();
            adminController.setLoggedInUser(loggedInUser);

            contentArea.getChildren().setAll(node);
            if (node instanceof Region) {
                VBox.setVgrow((Region) node, Priority.ALWAYS);
            }
            statusLabel.setText("Camogear Bistro (Admin Dashboard)");
        } catch (IOException e) {
            statusLabel.setText("Error: Could not load admin panel");
            e.printStackTrace();
        }
    }
}