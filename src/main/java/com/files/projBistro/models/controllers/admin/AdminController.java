package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.controllers.DashController;
import com.files.projBistro.models.dao.LoginDAO;
import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.models.Order;
import com.files.projBistro.models.controllers.MenuController;
import com.files.projBistro.models.dao.AdminDAO;
import com.files.projBistro.models.dao.DialogueDAO;
import com.files.projBistro.models.dao.SalesDAO;
import com.files.projBistro.models.userModel.User;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Optional;

public class AdminController {

    @FXML private Label statusLabel;

    // inventory tab
    @FXML private TableView<FoodItem> inventoryTable;
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String> colName;
    @FXML private TableColumn<FoodItem, Double> colPrice;
    @FXML private TableColumn<FoodItem, Integer> colStock;
    @FXML private TableColumn<FoodItem, String> colCategory;
    @FXML private TableColumn<FoodItem, Void> colActive;
    @FXML private TextField nameInput;
    @FXML private TextField priceInput;
    @FXML private TextField stockInput;
    @FXML private ComboBox<String> categoryBox;
    @FXML private Label imagePathLabel;
    @FXML private ImageView imagePreview;
    @FXML private Button chooseImageBtn;
    @FXML private TextArea descriptionInput;

    // dialogue tab
    @FXML private ComboBox<String> dialogueCharBox;
    @FXML private TableView<DialogueDAO.DialogueEntry> dialogueTable;
    @FXML private TableColumn<DialogueDAO.DialogueEntry, Integer> colDialogueId;
    @FXML private TableColumn<DialogueDAO.DialogueEntry, String> colTrigger;
    @FXML private TableColumn<DialogueDAO.DialogueEntry, String> colText;
    @FXML private ComboBox<String> triggerInputBox;
    @FXML private TextArea dialogueInputArea;
    @FXML private ComboBox<String> editTriggerBox;
    @FXML private TextArea editDialogueArea;

    // sales tab
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label totalItemsSoldLabel;
    @FXML private Label avgOrderValueLabel;
    @FXML private ComboBox<String> categoryFilterBox;
    @FXML private TextField minPriceFilter;
    @FXML private TextField maxPriceFilter;
    @FXML private Button applyFiltersBtn;
    @FXML private ListView<SalesDAO.PopularItem> popularItemsList;
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableColumn<Order, Integer> colOrderIdSales;
    @FXML private TableColumn<Order, String> colCustomerName;
    @FXML private TableColumn<Order, Double> colOrderTotal;
    @FXML private TableColumn<Order, String> colOrderDate;
    @FXML private TableColumn<Order, String> colOrderStatus;

    private VBox parentContentArea;

    // preview tab
    @FXML private TabPane previewTabPane;

    private AdminDAO adminDAO = new AdminDAO();
    private DialogueDAO dialogueDAO = new DialogueDAO();
    private SalesDAO salesDAO = new SalesDAO();
    private boolean isAuthorizedForSession = false;
    private LoginDAO loginDAO = new LoginDAO();
    private User currentAdmin;  // Store which admin is logged in

    private AdminInventoryController inventoryController;
    private AdminDialogueController dialogueController;
    private AdminSalesController salesController;
    private AdminPreviewController previewController;

    private User loggedInUser;

    // ===== FIXED: PauseTransition for status messages (no thread spawning) =====
    private PauseTransition currentStatusTimer = null;

    private void showTemporaryStatus(String message, String type) {
        statusLabel.setText(message);

        // Set color based on type
        String color;
        switch (type.toLowerCase()) {
            case "green": color = "#2ecc71"; break;
            case "red": color = "#e74c3c"; break;
            case "orange": color = "#f39c12"; break;
            default: color = "#666";
        }
        statusLabel.setStyle("-fx-text-fill: " + color + ";");

        // Cancel previous timer if exists
        if (currentStatusTimer != null) {
            currentStatusTimer.stop();
        }

        // Set new timer to clear after 3 seconds
        currentStatusTimer = new PauseTransition(Duration.seconds(3));
        currentStatusTimer.setOnFinished(e -> {
            if (statusLabel.getText().equals(message)) {
                statusLabel.setText("System ready.");
                statusLabel.setStyle("-fx-text-fill: #666;");
            }
        });
        currentStatusTimer.play();
    }

    @FXML
    public void initialize() {

        inventoryController = new AdminInventoryController();
        inventoryController.init(adminDAO, statusLabel, msg -> showTemporaryStatus(msg, "green"), this::isAuthorizedForSession);
        inventoryController.setUIElements(inventoryTable, colId, colName, colPrice, colStock, colCategory, colActive,
                nameInput, priceInput, stockInput, categoryBox, descriptionInput, imagePathLabel, imagePreview);

        dialogueController = new AdminDialogueController();
        dialogueController.init(adminDAO, dialogueDAO, statusLabel, msg -> showTemporaryStatus(msg, "green"), this::isAuthorizedForSession);
        dialogueController.setUIElements(dialogueCharBox, dialogueTable, colDialogueId, colTrigger, colText,
                triggerInputBox, dialogueInputArea, editTriggerBox, editDialogueArea);

        salesController = new AdminSalesController();
        salesController.init(salesDAO, statusLabel, msg -> showTemporaryStatus(msg, "green"));
        salesController.setUIElements(startDatePicker, endDatePicker, totalRevenueLabel, totalOrdersLabel,
                totalItemsSoldLabel, avgOrderValueLabel, popularItemsList, recentOrdersTable,
                colOrderIdSales, colCustomerName, colOrderTotal, colOrderDate, colOrderStatus,
                categoryFilterBox, minPriceFilter, maxPriceFilter, applyFiltersBtn);

        previewController = new AdminPreviewController();
        previewController.init(adminDAO, previewTabPane, statusLabel, msg -> showTemporaryStatus(msg, "green"));

        inventoryController.refreshTable();
        dialogueController.refreshTable();
        salesController.refreshSummary();
        previewController.refreshPreview();

        chooseImageBtn.setOnAction(e -> inventoryController.handleChooseImage());

    }


    private boolean checkAuthorizationOnEntry() {
        String adminUsername = loggedInUser.getUsername();

        while (true) {
            TextInputDialog pinDialog = new TextInputDialog();
            pinDialog.setTitle("Security Clearance");
            pinDialog.setHeaderText("Enter your admin PIN, " + adminUsername);
            pinDialog.setContentText("Enter your 4-digit PIN to access the admin panel:");

            Optional<String> result = pinDialog.showAndWait();

            if (result.isEmpty()) {
                showTemporaryStatus("Access cancelled.", "orange");
                if (parentContentArea != null) parentContentArea.setVisible(false);
                return false;
            }

            String enteredPin = result.get().trim();

            // Validate 4 digits
            if (!enteredPin.matches("\\d{4}")) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Invalid PIN");
                errorAlert.setHeaderText("PIN must be exactly 4 digits (0-9 only).");
                errorAlert.setContentText("You entered: " + enteredPin);
                errorAlert.showAndWait();
                continue;  // Loop again
            }

            User verifiedAdmin = loginDAO.verifyAdminByUsernameAndPin(adminUsername, enteredPin);

            if (verifiedAdmin != null) {
                isAuthorizedForSession = true;
                currentAdmin = verifiedAdmin;
                showTemporaryStatus("Access granted. Welcome, " + currentAdmin.getUsername() + "!", "green");
                if (parentContentArea != null) parentContentArea.setVisible(true);
                return true;
            } else {
                // Wrong PIN
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Access Denied");
                errorAlert.setHeaderText("Incorrect PIN for " + adminUsername);
                errorAlert.setContentText("That's not right. Try again?");
                errorAlert.showAndWait();
                // Continue loop (no need to set isAuthorizedForSession false here, it's already false)
            }
        }
    }

    private boolean isAuthorizedForSession() {
        return isAuthorizedForSession;
    }

    private void goBackToDashboard() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashView.fxml"));
                Scene scene = new Scene(loader.load(), 1024, 700);

                DashController dashController = loader.getController();
                if (loggedInUser != null) {
                    dashController.setLoggedInUser(loggedInUser);
                }

                Stage stage = (Stage) statusLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Camo-Gear Bistro | Admin Dashboard");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;  // ✅ Set FIRST

        isAuthorizedForSession = false;

        if (!checkAuthorizationOnEntry()) {
            goBackToDashboard();
        }
    }

    public void setParentContentArea(VBox contentArea) {
        this.parentContentArea = contentArea;
    }

    @FXML private void handleAddItemPopup() { inventoryController.handleAddItemPopup(); }
    @FXML private void handleSaveItem() { inventoryController.handleSaveItem(); }
    @FXML private void handleHardDeleteItem() { inventoryController.handleHardDeleteItem(); }
    @FXML private void clearInputs() { inventoryController.clearInputs(); }
    @FXML private void handleChooseImage() { inventoryController.handleChooseImage(); }

    @FXML private void handleAddDialogue() { dialogueController.handleAddDialogue(); }
    @FXML private void handleUpdateDialogue() { dialogueController.handleUpdateDialogue(); }
    @FXML private void handleDeleteDialogue() { dialogueController.handleDeleteDialogue(); }
    @FXML private void clearDialogueSelection() { dialogueController.clearDialogueSelection(); }
    @FXML private void loadDialogueTable() { dialogueController.refreshTable(); }

    @FXML private void refreshSalesSummary() { salesController.refreshSummary(); }
    @FXML private void handleExportSales() { salesController.handleExportSales(); }

    @FXML private void refreshMenuPreview() { previewController.refreshPreview(); }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));
            Scene scene = new Scene(loader.load(), 1024, 700);
            MenuController menuController = loader.getController();
            menuController.setLoggedInUser(loggedInUser);
            menuController.refreshMenu();

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Camogear Bistro (Menu)");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showTemporaryStatus("Error returning to menu.", "red");
        }
    }
}