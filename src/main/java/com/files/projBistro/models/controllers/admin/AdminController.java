package com.files.projBistro.models.controllers.admin;

// main admin controller, this is the hub controller that dispatches
// to other admin sub-controllers

import com.files.projBistro.models.controllers.DashController;
import com.files.projBistro.models.controllers.MainController;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Optional;

public class AdminController {

    @FXML private Label statusLabel; // temp status messages

    // inventory tab
    @FXML private TableView<FoodItem> inventoryTable; // shows all menu items
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String> colName;
    @FXML private TableColumn<FoodItem, Double> colPrice;
    @FXML private TableColumn<FoodItem, Integer> colStock;
    @FXML private TableColumn<FoodItem, String> colCategory;
    @FXML private TableColumn<FoodItem, Void> colActive; //vis-invis toggler

    // input field for editing items
    @FXML private TextField nameInput;
    @FXML private TextField priceInput;
    @FXML private TextField stockInput;
    @FXML private ComboBox<String> categoryBox;

    // image
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

    @FXML private TextArea feedbackDisplayArea;

    private VBox parentContentArea; // area beside dashboard
    @FXML private StackPane formStackPane; // holds the form and the gray overlay together

    // preview tab
    @FXML private TabPane previewTabPane;  // shows live menu preview

    // database helpers
    private AdminDAO adminDAO = new AdminDAO();
    private DialogueDAO dialogueDAO = new DialogueDAO();
    private SalesDAO salesDAO = new SalesDAO();
    private boolean isAuthorizedForSession = false;
    private LoginDAO loginDAO = new LoginDAO();
    private User currentAdmin;  // store which admin is logged in

    // child controllers that handle specific tabs
    private AdminInventoryController inventoryController;
    private AdminDialogueController dialogueController;
    private AdminSalesController salesController;
    private AdminPreviewController previewController;

    private User loggedInUser;

    // reference to parent - used for navigation back
    private MainController mainController;

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    // status message that disappears after 3 seconds
    private PauseTransition currentStatusTimer = null;

    private void showTemporaryStatus(String message, String type) {
        statusLabel.setText(message);

        // set color based on type (of message)
        String color;
        switch (type.toLowerCase()) {
            case "green": color = "#2ecc71"; break;
            case "red": color = "#e74c3c"; break;
            case "orange": color = "#f39c12"; break;
            default: color = "#666";
        }
        statusLabel.setStyle("-fx-text-fill: " + color + ";");

        // stop old timer if still running
        if (currentStatusTimer != null) {
            currentStatusTimer.stop();
        }

        // make message disappear after 3 seconds
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
        // create and set up inventory controller
        inventoryController = new AdminInventoryController();
        inventoryController.init(adminDAO, statusLabel, msg -> showTemporaryStatus(msg, "green"), this::isAuthorizedForSession);
        inventoryController.setUIElements(inventoryTable, colId, colName, colPrice, colStock, colCategory, colActive,
                nameInput, priceInput, stockInput, categoryBox, descriptionInput, imagePathLabel, imagePreview);

        // create and set up dialogue controller
        dialogueController = new AdminDialogueController();
        dialogueController.init(adminDAO, dialogueDAO, statusLabel, msg -> showTemporaryStatus(msg, "green"), this::isAuthorizedForSession);
        dialogueController.setUIElements(dialogueCharBox, dialogueTable, colDialogueId, colTrigger, colText,
                triggerInputBox, dialogueInputArea, editTriggerBox, editDialogueArea);

        // create and set up sales controller
        salesController = new AdminSalesController();
        salesController.init(salesDAO, statusLabel, msg -> showTemporaryStatus(msg, "green"));
        salesController.setUIElements(startDatePicker, endDatePicker, totalRevenueLabel, totalOrdersLabel,
                totalItemsSoldLabel, avgOrderValueLabel, popularItemsList, recentOrdersTable,
                colOrderIdSales, colCustomerName, colOrderTotal, colOrderDate, colOrderStatus,
                categoryFilterBox, minPriceFilter, maxPriceFilter, applyFiltersBtn);

        // create and set up preview controller
        previewController = new AdminPreviewController();
        previewController.init(adminDAO, previewTabPane, statusLabel, msg -> showTemporaryStatus(msg, "green"));

        // load initial data for all tabs
        inventoryController.refreshInventoryTable();
        dialogueController.refreshTable();
        salesController.refreshSummary();
        previewController.refreshPreview();

        // image browse button goes to inventory controller
        chooseImageBtn.setOnAction(e -> inventoryController.handleChooseImage());

        // find the gray overlay and pass it to inventory controller
        // overlay is in the main fxml but inventory controller needs it
        // this was prolly the most painful to figure out :(
        if (formStackPane != null) {
            for (Node child : formStackPane.getChildren()) {
                if (child instanceof VBox && "formOverlay".equals(child.getId())) {
                    inventoryController.setFormOverlay((VBox) child);
                    break;
                }
            }
        }

    }

    // pin verification - loops until correct pin or cancel
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

            // must be exactly 4 digits
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
                // wrong pin - ask again
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Access Denied");
                errorAlert.setHeaderText("Incorrect PIN for " + adminUsername);
                errorAlert.setContentText("That's not right. Try again?");
                errorAlert.showAndWait();
                // continue loop
            }
        }
    }

    // used by child controllers to check if admin is allowed
    private boolean isAuthorizedForSession() {
        return isAuthorizedForSession;
    }

    // go back to dashboard (ref: used if pin fails)
    private void goBackToDashboard() {
        if (mainController != null) {
            mainController.showAdminView(loggedInUser);
        } else {
            // Fallback
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
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user; // store user first

        isAuthorizedForSession = false;

        if (!checkAuthorizationOnEntry()) {
            goBackToDashboard(); // pin failed or cancelled
        }
    }


    public void setParentContentArea(VBox contentArea) {
        this.parentContentArea = contentArea;
    }

    // these just forward button clicks to the child controllers
    @FXML private void handleAddItemPopup() { inventoryController.handleAddItemPopup(); }
    @FXML private void handleSaveItem() { inventoryController.handleSaveItem(); }
    @FXML private void handleHardDeleteItem() { inventoryController.handleHardDeleteItem(); }
    @FXML private void clearInputs() { inventoryController.clearInputs(); }
    @FXML private void handleChooseImage() { inventoryController.handleChooseImage(); }

    @FXML private void handleAddDialogue() { dialogueController.handleAddDialogue(); }
    @FXML private void handleUpdateDialogue() { dialogueController.handleUpdateDialogue(); }
    @FXML private void handleDeleteDialogue() { dialogueController.handleDeleteDialogue(); }
    @FXML private void clearDialogueSelection() { dialogueController.clearDialogueSelection(); }

    @FXML private void refreshSalesSummary() { salesController.refreshSummary(); }
    @FXML private void handleExportSales() { salesController.handleExportSales(); }

    @FXML private void refreshMenuPreview() { previewController.refreshPreview(); }

    // scrapped method
//    @FXML
//    private void handleBack() {
//        if (mainController != null && loggedInUser != null) {
//            mainController.showMenuView(loggedInUser);
//        } else {
//            // Fallback to old behavior
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));
//                Scene scene = new Scene(loader.load(), 1024, 700);
//                MenuController menuController = loader.getController();
//                menuController.setLoggedInUser(loggedInUser);
//                menuController.refreshMenu();
//
//                Stage stage = (Stage) statusLabel.getScene().getWindow();
//                stage.setScene(scene);
//                stage.setTitle("Camogear Bistro (Menu)");
//                stage.show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                showTemporaryStatus("Error returning to menu.", "red");
//            }
//        }
//    }


    @FXML
    private void refreshInventoryTable() {
        inventoryController.refreshInventoryTable();
    }

    @FXML
    private void handleAddMultipleItemsPopup() {
        if (inventoryController != null) {
            inventoryController.handleAddMultipleItemsPopup();
        }
    }
}