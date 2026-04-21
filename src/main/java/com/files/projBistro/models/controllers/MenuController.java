package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.models.Order;
import com.files.projBistro.models.models.ThemeManager;
import com.files.projBistro.models.dao.LoginDAO;
import com.files.projBistro.models.dao.MenuDAO;
import com.files.projBistro.models.dao.DialogueDAO;
import com.files.projBistro.models.dao.OrderDAO;
import com.files.projBistro.models.userModel.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class MenuController {

    @FXML private TabPane categoryTabs;
    @FXML private ListView<FoodItem> cartListView;
    @FXML private Label totalLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label dialogueLabel;
    @FXML private ImageView characterImageView;
    @FXML private Button authBtn;
    @FXML private Button switchAccountBtn;
    @FXML private Button submitBtn;
    @FXML private Button settingsBtn;

    private Order currentOrder;
    private User loggedInUser = null;
    private final Map<String, List<FoodItem>> menuItemsByCharacter = new HashMap<>();
    private final String[] characters = {"Chloe", "Mimi", "Metsu", "Laniard"};

    private final MenuDAO menuDAO = new MenuDAO();
    private final LoginDAO loginDAO = new LoginDAO();
    private final DialogueDAO dialogueDAO = new DialogueDAO();
    private final Random random = new Random();

    private void playRandomTapSound() {
        try {
            String soundFile = random.nextBoolean() ? "/audio/tap1.mp3" : "/audio/tap2.mp3";
            Media sound = new Media(getClass().getResource(soundFile).toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Could not play tap sound: " + e.getMessage());
        }
    }

    private void playOrderSuccessSound() {
        try {
            Media sound = new Media(getClass().getResource("/audio/orderSuccess.mp3").toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Could not play success sound: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        currentOrder = new Order(1, "Guest");
        setupCartListView();
        loadAllMenuItems();
        createCharacterTabs();
        updateWelcomeMessage();
        updateAuthButtonState();

        Platform.runLater(() -> {
            ThemeManager.applyToScene(welcomeLabel.getScene(), getClass());
        });
    }

    public void refreshMenu() {
        System.out.println("refreshing customer menu from database");
        loadAllMenuItems();      // reload from database
        createCharacterTabs();   // rebuild the tabs
    }

    private void setupCartListView() {
        cartListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FoodItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    Label nameLabel = new Label(item.getName());
                    nameLabel.setPrefWidth(150);
                    Label priceLabel = new Label(String.format("£%.2f", item.getPrice()));
                    Button removeBtn = new Button("✖");
                    removeBtn.setOnAction(e -> {
                        playRandomTapSound();
                        removeFromCart(item);
                    });
                    hbox.getChildren().addAll(nameLabel, priceLabel, removeBtn);
                    setGraphic(hbox);
                }
            }
        });
    }

    private void loadAllMenuItems() {
        for (String character : characters) {
            List<FoodItem> items = menuDAO.getItemsByCharacter(character);
            menuItemsByCharacter.put(character, items);
            System.out.println("Loaded " + items.size() + " items for " + character);
        }
    }

    private void createCharacterTabs() {
        categoryTabs.getTabs().clear();
        for (String character : characters) {
            Tab tab = new Tab(character);
            tab.setClosable(false);
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            TilePane tilePane = new TilePane();
            tilePane.setHgap(20);
            tilePane.setVgap(20);
            tilePane.setPadding(new Insets(20));
            tilePane.setPrefColumns(3);
            List<FoodItem> items = menuItemsByCharacter.get(character);
            if (items != null && !items.isEmpty()) {
                for (FoodItem item : items) {
                    VBox itemCard = createItemCard(item, character);
                    tilePane.getChildren().add(itemCard);
                }
            } else {
                Label emptyLabel = new Label("No items available for " + character);
                tilePane.getChildren().add(emptyLabel);
            }
            scrollPane.setContent(tilePane);
            tab.setContent(scrollPane);
            categoryTabs.getTabs().add(tab);
        }
        categoryTabs.getSelectionModel().selectedItemProperty().addListener((obs, old, newTab) -> {
            if (newTab != null) {
                playRandomTapSound();
                String character = newTab.getText();
                updateCharacterUI(character, "WELCOME");
            }
        });
    }

    private void updateCharacterUI(String character, String dialogueType) {
        int charId = getCharacterId(character);
        String dialogue = dialogueDAO.getRandomDialogue(charId, dialogueType);
        dialogueLabel.setText(dialogue);
        String imagePath = getCharacterImagePath(character);
        if (imagePath != null) {
            try {
                Image image = new Image(getClass().getResource(imagePath).toExternalForm());
                characterImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Could not load character image: " + imagePath);
            }
        }
    }

    private String getCharacterImagePath(String character) {
        switch (character) {
            case "Chloe": return "/icons/ic_Chloe/icon_chloe.jpeg";
            case "Mimi": return "/icons/ic_Mimi/icon_mimi.jpeg";
            case "Metsu": return "/icons/ic_Metsu/icon_metsu.jpeg";
            case "Laniard": return "/icons/ic_Lan/icon_Lan.jpeg";
            default: return "/icons/ic_Chloe/icon_Chloe.jpeg";
        }
    }

    private VBox createItemCard(FoodItem item, String character) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(200);
        card.setPadding(new Insets(10));
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                String imageUrl = getClass().getResource(item.getImagePath()).toExternalForm();
                imageView.setImage(new Image(imageUrl));
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }
        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        Label descLabel = new Label(item.getItemType() + " • " + getStockStatus(item.getStock()));
        Label priceLabel = new Label(String.format("£%.2f", item.getPrice()));
        Button detailsBtn = new Button("View Details");
        detailsBtn.setPrefWidth(120);
        detailsBtn.setOnAction(e -> {
            playRandomTapSound();
            showItemDetails(item);
        });
        Button addBtn = new Button("Add to Cart");
        addBtn.setPrefWidth(120);
        if (item.getStock() <= 0) {
            addBtn.setText("Out of Stock");
            addBtn.setDisable(true);
        } else {
            addBtn.setOnAction(e -> {
                playRandomTapSound();
                addItemToCart(item);
                updateCharacterUI(character, "ITEM_SELECTED");
            });
        }
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setMinHeight(110);
        imageContainer.getChildren().add(imageView);
        card.getChildren().addAll(imageContainer, nameLabel, descLabel, priceLabel, detailsBtn, addBtn);
        return card;
    }

    private void showItemDetails(FoodItem item) {
        // Create a custom dialog instead of a simple Alert
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Dish Details");
        dialog.setHeaderText(item.getName());

        // Add OK button to close
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButton);

        // Create content layout
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        // Price and stock info
        HBox infoRow = new HBox(20);
        Label priceLabel = new Label(String.format("Price: £%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        String stockStatus = getStockStatus(item.getStock());
        Label stockLabel = new Label("Stock: " + stockStatus);
        if (item.getStock() <= 0) {
            stockLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else if (item.getStock() <= 10) {
            stockLabel.setStyle("-fx-text-fill: #f39c12;");
        }
        infoRow.getChildren().addAll(priceLabel, stockLabel);

        // Description section with scroll pane
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextArea descriptionArea = new TextArea();
        String description = item.getDescription();
        if (description == null || description.isBlank()) {
            description = "No description is available for this dish.";
        }
        descriptionArea.setText(description);
        descriptionArea.setWrapText(true);      // THIS makes text wrap to next line
        descriptionArea.setEditable(false);     // User can't edit
        descriptionArea.setPrefRowCount(5);     // Show about 5 lines initially
        descriptionArea.setPrefWidth(380);
        descriptionArea.setStyle("-fx-font-size: 13px; -fx-font-family: 'System';");

        // Make the TextArea scrollable if text is very long
        ScrollPane scrollPane = new ScrollPane(descriptionArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        scrollPane.setMaxHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setPadding(Insets.EMPTY);

        // Image preview if available
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);

        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                var resource = getClass().getResource(item.getImagePath());
                if (resource != null) {
                    imageView.setImage(new Image(resource.toExternalForm()));
                }
            } catch (Exception e) {
                // No image, that's fine
            }
        }

        // Only add image if one exists
        if (imageView.getImage() != null) {
            StackPane imageContainer = new StackPane(imageView);
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 8;");
            content.getChildren().add(imageContainer);
        }

        content.getChildren().addAll(infoRow, descLabel, scrollPane);

        dialog.getDialogPane().setContent(content);

        // Make dialog resizable so user can stretch if needed
        dialog.setResizable(true);

        dialog.showAndWait();
    }

    private String getStockStatus(int stock) {
        if (stock > 50) return "In Stock";
        if (stock > 10) return "Low Stock";
        if (stock > 0) return "Limited!";
        return "Out of Stock";
    }

    private int getCharacterId(String characterName) {
        switch (characterName) {
            case "Chloe": return 1;
            case "Mimi": return 2;
            case "Metsu": return 3;
            case "Laniard": return 4;
            default: return 1;
        }
    }

    private void addItemToCart(FoodItem item) {
        currentOrder.addItem(item);
        refreshCartDisplay();
    }

    private void removeFromCart(FoodItem item) {
        currentOrder.removeItem(item);
        refreshCartDisplay();
    }

    private void refreshCartDisplay() {
        cartListView.setItems(FXCollections.observableArrayList(currentOrder.getItems()));
        totalLabel.setText(String.format("Total: £%.2f", currentOrder.getTotalPrice()));
    }

    @FXML
    private void handleAuthAction() {
        playRandomTapSound();
        if (loggedInUser == null) {
            showLoginDialog();
        } else {
            handleLogout();
        }
    }

    @FXML
    private void handleSwitchAccount() {
        playRandomTapSound();
        showLoginDialog("Switch Account", "Log in with another account");
    }

    @FXML
    private void handleSubmitOrder() {
        playRandomTapSound();

        if (currentOrder.getItems().isEmpty()) {
            showAlert("Empty Cart", "Please add items to your cart before submitting.");
            return;
        }

        if (loggedInUser == null) {
            showAlert("Login Required", "Please log in to submit your order.");
            showLoginDialog();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Order");
        confirm.setHeaderText("Submit your order?");
        confirm.setContentText(String.format("Total: £%.2f\n\nItems: %d",
                currentOrder.getTotalPrice(), currentOrder.getItems().size()));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            OrderDAO orderDAO = new OrderDAO();
            int userId = loggedInUser.getId();

            if (userId != -1) {
                int orderId = orderDAO.saveOrder(currentOrder, userId);
                if (orderId != -1) {


                    // save invoice to txt file
                    saveInvoiceToFile(currentOrder, orderId);

                    showAlert("Order Submitted!", "Your order has been placed successfully!\n\nInvoice saved to your computer.");
                    playOrderSuccessSound();

                    Random rand = new Random();
                    int charId = rand.nextInt(4) + 1;
                    String dialogue = dialogueDAO.getRandomDialogue(charId, "ORDER_COMPLETE");
                    dialogueLabel.setText(dialogue);

                    currentOrder.getItems().clear();
                    refreshCartDisplay();
                    loadAllMenuItems();
                    createCharacterTabs();
                } else {
                    showAlert("Order Failed", "There was an error processing your order. Please try again.");
                }
            } else {
                showAlert("Error", "Could not find user account.");
            }
        }
    }

    @FXML
    private void handleClearCart() {
        playRandomTapSound();
        if (!currentOrder.getItems().isEmpty()) {
            currentOrder.getItems().clear();
            refreshCartDisplay();
            dialogueLabel.setText("Cart cleared. Ready for new orders!");
        }
    }

    private void showLoginDialog(String title, String header) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return loginDAO.verifyLogin(username.getText(), password.getText());
            }
            return null;
        });
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            if (loggedInUser != null && !loggedInUser.equals(user)) {
                currentOrder.getItems().clear();
                refreshCartDisplay();
            }
            loggedInUser = user;
            updateWelcomeMessage();
            updateAuthButtonState();
            dialogueLabel.setText("Welcome back, " + user.getUsername() + "!");
            loadAllMenuItems();
            createCharacterTabs();
        });
    }

    private void showLoginDialog() {
        showLoginDialog("Login", "Enter your credentials");
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        updateWelcomeMessage();
        updateAuthButtonState();
        if (user != null) {
            dialogueLabel.setText("Welcome back, " + user.getUsername() + "!");
        }
    }

    private void updateAuthButtonState() {
        if (loggedInUser != null) {
            authBtn.setText("🚪 Logout");
            switchAccountBtn.setVisible(true);
        } else {
            authBtn.setText("🔐 Login");
            switchAccountBtn.setVisible(false);
        }
    }

    private void updateWelcomeMessage() {
        if (loggedInUser != null) {
            welcomeLabel.setText("Welcome, " + loggedInUser.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome, Guest! Please log in to order.");
        }
    }

    private void handleLogout() {
        ThemeManager.resetToDefault();

        loggedInUser = null;
        updateWelcomeMessage();
        updateAuthButtonState();
        dialogueLabel.setText("You have been logged out. Come back soon!");
        currentOrder.getItems().clear();
        refreshCartDisplay();
        loadAllMenuItems();
        createCharacterTabs();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // SAVE INVOICE METHOD - properly placed outside of handleSubmitOrder
    private void saveInvoiceToFile(Order order, int savedOrderId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save invoice");
        fileChooser.setInitialFileName("invoice_" + savedOrderId + "_" + LocalDate.now() + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("==================================================");
                writer.println("              CAMO-GEAR BISTRO");
                writer.println("==================================================");
                writer.println();
                writer.println("                  ORDER RECEIPT");
                writer.println();
                writer.println("--------------------------------------------------");
                writer.println("ORDER DETAILS");
                writer.println("--------------------------------------------------");
                writer.printf("  %-15s %d%n", "Order #:", savedOrderId);
                writer.printf("  %-15s %s%n", "Date:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.printf("  %-15s %s%n", "Customer:", loggedInUser != null ? loggedInUser.getUsername() : "Guest");
                writer.println();

                writer.println("--------------------------------------------------");
                writer.println("ITEMS ORDERED");
                writer.println("--------------------------------------------------");
                writer.printf("  %-35s %10s%n", "Item", "Price");
                writer.println("  " + "-".repeat(45));
                for (FoodItem item : order.getItems()) {
                    String name = item.getName();
                    if (name.length() > 33) {
                        name = name.substring(0, 30) + "...";
                    }
                    writer.printf("  %-35s £%9.2f%n", name, item.getPrice());
                }
                writer.println();
                writer.println("--------------------------------------------------");
                writer.printf("  %-35s £%9.2f%n", "TOTAL:", order.getTotalPrice());
                writer.println();
                writer.println("==================================================");
                writer.println("     Thank you for dining with us!");
                writer.println("         Please come again!");
                writer.println("==================================================");

                showAlert("Invoice Saved", "Invoice saved to TXT!\n\nLocation: " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Export Failed", "Could not save invoice: " + e.getMessage());
            }

        }
    }

    @FXML
    private void handleOpenSettings(javafx.event.ActionEvent event) throws IOException {
        playRandomTapSound();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/settingsView.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(loader.load(), 450, 350);
        SettingsController settingsController = loader.getController();
        settingsController.setLoggedInUser(loggedInUser);

        ThemeManager.applyToScene(scene, getClass());

        stage.setScene(scene);
        stage.setTitle("Camogear Bistro (Settings)");
        stage.show();
    }
}