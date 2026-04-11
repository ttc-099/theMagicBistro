package com.files.projBistro.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Optional;

public class DashController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // initial setup for the admin dashboard
        System.out.println("Admin Dashboard initialized...");
    }


    @FXML
    private void showOrders() {
        statusLabel.setText("Viewing Live Order Feed...");
        // later: load liveOrdersView.fxml into contentArea
    }

    @FXML
    private void showStockAlerts() {
        statusLabel.setText("Checking Stock Levels...");
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        // return to login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 400, 500);
        stage.setScene(scene);
        stage.setTitle("Camo-Gear Bistro | Login");
        stage.show();
    }

    private boolean checkAdminClearance() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Security Clearance");
        dialog.setHeaderText("Action Restricted: Admin PIN Required");
        dialog.setContentText("Enter 4-digit PIN:");

        Optional<String> result = dialog.showAndWait();
        return result.isPresent() && result.get().equals("1234"); // Your secret PIN
    }

    @FXML
    private void showMenuManagement() {
        // Tactical Security: Only proceed if PIN is correct
        if (!checkAdminClearance()) {
            statusLabel.setText("Access Denied: Incorrect PIN.");
            return;
        }
        System.out.println("Searching for FXML at: " + getClass().getResource("/menuEditView.fxml"));
        try {
            // 1. Create the loader
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("menuEditView.fxml"));
            // 2. Actually LOAD the file into a Node (the visual part)
            Node node = loader.load();
            // 3. Inject it into the center of your dashboard
            contentArea.getChildren().setAll(node);
            statusLabel.setText("Inventory System Online. Welcome, Admin.");
        } catch (IOException e) {
            statusLabel.setText("Tactical Error: Could not find menuEditView.fxml");
            e.printStackTrace();
        }
    }
}