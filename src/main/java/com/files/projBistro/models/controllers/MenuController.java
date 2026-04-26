package com.files.projBistro.models.controllers;

import com.files.projBistro.models.dao.DialogueDAO;
import com.files.projBistro.models.dao.LoginDAO;
import com.files.projBistro.models.dao.MenuDAO;
import com.files.projBistro.models.dao.OrderDAO;
import com.files.projBistro.models.models.*;
import com.files.projBistro.models.userModel.User;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.files.projBistro.models.database.DatabaseConnection;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MenuController {

    // ==================== FXML FIELDS ====================
    @FXML private TabPane categoryTabs;
    @FXML private ListView<FoodItem> cartListView;
    @FXML private Label totalLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label dialogueLabel;
    @FXML private ImageView characterImageView;
    @FXML private Button authBtn;
    @FXML private StackPane mainContentArea;
    @FXML private VBox menuView;

    // ==================== DATA FIELDS ====================
    private Order currentOrder;
    private User loggedInUser = null;
    private int selectedRating = 0;
    private final Map<String, List<FoodItem>> menuItemsByCharacter = new HashMap<>();
    private final String[] characters = {"Chloe", "Mimi", "Metsu", "Laniard"};
    private boolean isInMenuView = true;

    // ==================== DAOs ====================
    private final MenuDAO menuDAO = new MenuDAO();
    private final LoginDAO loginDAO = new LoginDAO();
    private final DialogueDAO dialogueDAO = new DialogueDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final Random random = new Random();

    // ==================== MANAGERS ====================
    private CartManager cartManager;
    private MainController mainController;


    // ==================== INITIALIZATION ====================
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        currentOrder = new Order(1, "Guest");
        cartManager = new CartManager(currentOrder, cartListView, totalLabel);
        loadAllMenuItems();
        createCharacterTabs();
        updateWelcomeMessage();
        updateAuthButtonState();
        applyCSS();
    }

    private void applyCSS() {
        Scene scene = welcomeLabel.getScene();
        if (scene != null) {
            URL cssUrl = getClass().getResource("/styles/light.css");
            if (cssUrl != null) {
                String css = cssUrl.toExternalForm();
                if (!scene.getStylesheets().contains(css)) {
                    scene.getStylesheets().add(css);
                    System.out.println("CSS loaded: " + css);
                }
            }
        }
    }

    // ==================== CART OPERATIONS ====================
    public void addItemToCart(FoodItem item) {
        cartManager.addItem(item);
    }

    private void removeFromCart(FoodItem item) {
        cartManager.removeItem(item);
    }

    @FXML
    private void handleClearCart() {
        SoundHelper.playTapSound();
        if (!cartManager.isEmpty()) {
            cartManager.clearCart();
            dialogueLabel.setText("Cart cleared. Ready for new orders!");
        }
    }

    // ==================== MENU LOADING ====================
    private void loadAllMenuItems() {
        for (String character : characters) {
            List<FoodItem> items = menuDAO.getItemsByCharacter(character);
            menuItemsByCharacter.put(character, items);
            System.out.println("Loaded " + items.size() + " items for " + character);
        }
    }

    public void refreshMenu() {
        loadAllMenuItems();
        createCharacterTabs();
        // Re-apply tab styles after refresh
        Platform.runLater(() -> {
            updateTabStyles();
        });
    }

    private void createCharacterTabs() {
        categoryTabs.getTabs().clear();
        categoryTabs.setStyle("-fx-background-color: transparent;");

        for (String character : characters) {
            Tab tab = new Tab(character);
            tab.setClosable(false);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

            TilePane tilePane = new TilePane();
            tilePane.setHgap(20);
            tilePane.setVgap(20);
            tilePane.setPadding(new Insets(20));
            tilePane.setPrefColumns(3);
            tilePane.setStyle("-fx-background-color: transparent;");

            List<FoodItem> items = menuItemsByCharacter.get(character);
            if (items != null && !items.isEmpty()) {
                for (FoodItem item : items) {
                    tilePane.getChildren().add(createItemCard(item, character));
                }
            } else {
                Label emptyLabel = new Label("No items available for " + character);
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
                tilePane.getChildren().add(emptyLabel);
            }

            scrollPane.setContent(tilePane);
            tab.setContent(scrollPane);
            categoryTabs.getTabs().add(tab);
        }

        setupTabSelectionListener();

        if (!categoryTabs.getTabs().isEmpty()) {
            Tab firstTab = categoryTabs.getTabs().get(0);
            categoryTabs.getSelectionModel().select(0);
            Platform.runLater(() -> {
                updateTabStyles();
                // Force the TabPane to layout properly
                categoryTabs.requestLayout();
            });
        }
    }

    private void setupTabSelectionListener() {
        categoryTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newTab) -> {
            if (newTab != null) {
                SoundHelper.playTapSound();
                updateCharacterUI(newTab.getText(), "WELCOME");
                updateTabStyles();
            }
        });
    }

    private void updateTabStyles() {
        Tab selected = categoryTabs.getSelectionModel().getSelectedItem();
        for (Tab tab : categoryTabs.getTabs()) {
            // Remove both classes first
            tab.getStyleClass().removeAll("menu-tab", "menu-tab-selected");

            if (tab == selected) {
                tab.getStyleClass().add("menu-tab-selected");
            } else {
                tab.getStyleClass().add("menu-tab");
            }
        }
    }


    // ==================== ITEM CARD ====================
    private VBox createItemCard(FoodItem item, String character) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(200);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("item-card");

        ImageView imageView = createImageView(item);
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setMinHeight(110);
        imageContainer.getChildren().add(imageView);

        Label nameLabel = createLabel(item.getName(), "item-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);
        nameLabel.setAlignment(Pos.CENTER);

        Label descLabel = createLabel(item.getItemType() + " • " + getStockStatus(item.getStock()), "item-desc");
        Label priceLabel = createLabel(String.format("RM%.2f", item.getPrice()), "item-price");

        Button detailsBtn = createDetailsButton(item);
        Button addBtn = createAddButton(item, character);

        card.getChildren().addAll(imageContainer, nameLabel, descLabel, priceLabel, detailsBtn, addBtn);
        return card;
    }

    private ImageView createImageView(FoodItem item) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("item-image");

        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                URL resource = getClass().getResource(item.getImagePath());
                if (resource != null) {
                    String imageUrl = resource.toExternalForm();
                    imageView.setImage(new Image(imageUrl));
                }
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }
        return imageView;
    }

    private Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private Button createDetailsButton(FoodItem item) {
        Button btn = new Button("View Details");
        btn.setPrefWidth(120);
        btn.getStyleClass().add("details-button");
        btn.setOnAction(e -> {
            SoundHelper.playTapSound();
            showItemDetails(item);
        });
        return btn;
    }

    private Button createAddButton(FoodItem item, String character) {
        Button btn = new Button("Add to Cart");
        btn.setPrefWidth(120);

        if (item.getStock() <= 0) {
            btn.setText("Out of Stock");
            btn.setDisable(true);
            btn.getStyleClass().add("out-of-stock-button");
        } else {
            btn.getStyleClass().add("add-button");
            btn.setOnAction(e -> {
                SoundHelper.playTapSound();
                addItemToCart(item);
                updateCharacterUI(character, "ITEM_SELECTED");
            });
        }
        return btn;
    }

    // ==================== CHARACTER UI ====================
    private void updateCharacterUI(String character, String dialogueType) {
        int charId = getCharacterId(character);
        dialogueLabel.setText(dialogueDAO.getRandomDialogue(charId, dialogueType));

        String imagePath = getCharacterImagePath(character);
        if (imagePath != null) {
            try {
                URL resource = getClass().getResource(imagePath);
                if (resource != null) {
                    characterImageView.setImage(new Image(resource.toExternalForm()));
                }
            } catch (Exception e) {
                System.err.println("Could not load character image: " + imagePath);
            }
        }
    }

    private String getCharacterImagePath(String character) {
        switch (character) {
            case "Chloe": return "/icons/ic_Chloe/icon_chloe.png";
            case "Mimi": return "/icons/ic_Mimi/icon_mimi.png";
            case "Metsu": return "/icons/ic_Metsu/icon_metsu.png";
            case "Laniard": return "/icons/ic_Lan/icon_Lan.png";
            default: return "/icons/ic_Chloe/icon_Chloe.png";
        }
    }

    // ==================== HELPERS ====================
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

    public FoodItem getItemByName(String itemName) {
        return menuItemsByCharacter.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getName().equals(itemName))
                .findFirst()
                .orElse(null);
    }

    // ==================== AUTHENTICATION ====================

    // Modify handleAuthAction (replace existing)
    @FXML
    private void handleAuthAction() {
        SoundHelper.playTapSound();
        if (loggedInUser != null) {
            handleLogout();
        }
    }

    // Modify handleLogout (replace existing)
    private void handleLogout() {
        ThemeManager.resetToDefault();
        loggedInUser = null;
        cartManager.clearCart();

        if (mainController != null) {
            mainController.showLoginView();
        }
    }

    // ==================== LOGIN DIALOGS ====================
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
        dialog.setResultConverter(button -> button == loginButtonType ? loginDAO.verifyLogin(username.getText(), password.getText()) : null);

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            if (loggedInUser != null && !loggedInUser.equals(user)) {
                cartManager.clearCart();
            }
            loggedInUser = user;
            updateWelcomeMessage();
            updateAuthButtonState();
            dialogueLabel.setText("Welcome back, " + user.getUsername() + "!");
            refreshMenu();
        });
    }

    private void showLoginDialog() {
        showLoginDialog("Login", "Enter your credentials");
    }

    // ==================== USER STATE ====================
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        updateWelcomeMessage();
        updateAuthButtonState();
        if (user != null) {
            dialogueLabel.setText("Welcome back, " + user.getUsername() + "!");
        }
    }

    // Update the auth button state method:
    private void updateAuthButtonState() {
        if (loggedInUser != null) {
            authBtn.setText("🚪 Logout");
        } else {
            authBtn.setText("🔐 Login");
        }
    }

// Remove handleSwitchAccount() method entirely

    private void updateWelcomeMessage() {
        welcomeLabel.setText(loggedInUser != null ? "Welcome, " + loggedInUser.getUsername() + "!" : "Welcome, Guest! Please log in to order.");
    }

    // ==================== ORDER SUBMISSION ====================
    @FXML
    private void handleSubmitOrder() {
        SoundHelper.playTapSound();

        if (cartManager.isEmpty()) {
            showAlert("Empty Cart", "Please add items to your cart before submitting.");
            return;
        }

        // Force login for ALL orders - no guests allowed
        if (loggedInUser == null) {
            showAlert("Login Required", "Please log in or create an account to place an order.");
            showLoginDialog();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Order");
        confirm.setHeaderText("Submit your order?");
        confirm.setContentText(String.format("Total: RM%.2f\n\nItems: %d", currentOrder.getTotalPrice(), currentOrder.getItems().size()));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String paymentMethod = PaymentProcessor.showPaymentMethodDialog();
        if (paymentMethod == null) return;

        if ("Credit/Debit Card".equals(paymentMethod) && !PaymentProcessor.processCardPayment(currentOrder.getTotalPrice())) return;

        int userId = loggedInUser.getId();
        if (userId == -1) {
            showAlert("Error", "Could not find user account.");
            return;
        }

        // Get the REAL database order ID
        int savedOrderId = orderDAO.saveOrder(currentOrder, userId);
        if (savedOrderId == -1) {
            showAlert("Order Failed", "There was an error processing your order. Please try again.");
            return;
        }

        // Update the order object with the real ID
        currentOrder.setOrderId(savedOrderId);

        saveInvoiceToFile(currentOrder, savedOrderId);

        SoundHelper.playSuccess();

        // Show the real order ID to the customer
        showAlert("Order Submitted!", "Your order #" + savedOrderId + " has been placed successfully!\n\nInvoice saved to your computer.");

        showFeedbackDialog();

        int charId = random.nextInt(4) + 1;
        dialogueLabel.setText(dialogueDAO.getRandomDialogue(charId, "ORDER_COMPLETE"));

        cartManager.clearCart();
        refreshMenu();
    }

    // ==================== FEEDBACK ====================
    private void showFeedbackDialog() {
        Dialog<Void> feedbackDialog = new Dialog<>();
        feedbackDialog.setTitle("Rate Your Experience");
        feedbackDialog.setHeaderText("How was your meal?");
        feedbackDialog.setResizable(true);

        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        feedbackDialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);
        content.setAlignment(Pos.CENTER);

        Label instructionLabel = new Label("Tap a star to rate your experience!");
        instructionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Star rating using Labels (better than Buttons for emoji)
        HBox starBox = new HBox(5);
        starBox.setAlignment(Pos.CENTER);
        starBox.setStyle("-fx-padding: 10;");

        Label[] starLabels = new Label[5];

        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            Label starLabel = new Label("☆");
            starLabel.setStyle("-fx-font-size: 35px; -fx-text-fill: #f1c40f; -fx-cursor: hand; -fx-font-family: 'Segoe UI Emoji';");
            starLabel.setOnMouseClicked(e -> {
                selectedRating = rating;
                // Update all stars
                for (int j = 0; j < 5; j++) {
                    starLabels[j].setText(j < rating ? "★" : "☆");
                }
            });
            starLabels[i] = starLabel;
            starBox.getChildren().add(starLabel);
        }

        // Comment field
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Share your experience... (optional)");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-font-size: 13px;");

        content.getChildren().addAll(instructionLabel, starBox, commentArea);
        feedbackDialog.getDialogPane().setContent(content);

        feedbackDialog.setResultConverter(button -> {
            if (button == submitButton) {
                if (selectedRating > 0) {
                    saveFeedback(selectedRating, commentArea.getText());
                    showAlert("Thank You!", "Thanks for your feedback!");
                } else {
                    showAlert("No Rating", "Please tap a star to rate your experience.");
                }
            }
            return null;
        });

        feedbackDialog.showAndWait();
    }

    private void saveFeedback(int rating, String comment) {
        if (loggedInUser == null) return;

        String orderSql = "SELECT order_id FROM orders WHERE user_id = ? AND status = 'completed' ORDER BY order_date DESC LIMIT 1";
        int orderId = -1;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(orderSql)) {
            pstmt.setInt(1, loggedInUser.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) orderId = rs.getInt("order_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (orderId == -1) return;

        String sql = "INSERT INTO feedback (user_id, order_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedInUser.getId());
            pstmt.setInt(2, orderId);
            pstmt.setInt(3, rating);
            pstmt.setString(4, comment != null ? comment : "");
            pstmt.executeUpdate();
            System.out.println("✅ Feedback saved for order #" + orderId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== INVOICE PDF ====================
    private void saveInvoiceToFile(Order order, int savedOrderId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Invoice");
        fileChooser.setInitialFileName("invoice_" + savedOrderId + "_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try {
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("CAMO-GEAR BISTRO").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("ORDER RECEIPT").setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));

            // ✅ Use display order ID in invoice
            document.add(new Paragraph("Order #: " + order.getOrderId()));
            document.add(new Paragraph("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(new Paragraph("Customer: " + (loggedInUser != null ? loggedInUser.getUsername() : "Guest")));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.addCell(new Cell().add(new Paragraph("Item")).setBold());
            table.addCell(new Cell().add(new Paragraph("Price")).setBold());

            for (FoodItem item : order.getItems()) {
                table.addCell(item.getName());
                table.addCell(String.format("RM%.2f", item.getPrice()));
            }

            table.addCell(new Cell().add(new Paragraph("TOTAL:")).setBold());
            table.addCell(new Cell().add(new Paragraph(String.format("RM%.2f", order.getTotalPrice()))).setBold());

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Thank you for dining with us!").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Please come again!").setTextAlignment(TextAlignment.CENTER));

            document.close();
            showAlert("Invoice Saved", "PDF invoice saved to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Export Failed", "Could not save PDF: " + e.getMessage());
        }
    }

    // ==================== ITEM DETAILS ====================
    private void showItemDetails(FoodItem item) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Dish Details");
        dialog.setHeaderText(item.getName());
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.OK_DONE));

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        HBox infoRow = new HBox(20);
        Label priceLabel = new Label(String.format("Price: RM%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        String stockStatus = getStockStatus(item.getStock());
        Label stockLabel = new Label("Stock: " + stockStatus);
        if (item.getStock() <= 0) stockLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        else if (item.getStock() <= 10) stockLabel.setStyle("-fx-text-fill: #f39c12;");

        infoRow.getChildren().addAll(priceLabel, stockLabel);

        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setText(item.getDescription() != null && !item.getDescription().isBlank() ? item.getDescription() : "No description is available for this dish.");
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(5);
        descriptionArea.setPrefWidth(380);

        ScrollPane scrollPane = new ScrollPane(descriptionArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        scrollPane.setMaxHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setPadding(Insets.EMPTY);

        content.getChildren().addAll(infoRow, descLabel, scrollPane);

        // Add image if exists
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                URL resource = getClass().getResource(item.getImagePath());
                if (resource != null) {
                    ImageView imageView = new ImageView(new Image(resource.toExternalForm()));
                    imageView.setFitHeight(120);
                    imageView.setFitWidth(120);
                    imageView.setPreserveRatio(true);
                    StackPane imageContainer = new StackPane(imageView);
                    imageContainer.setAlignment(Pos.CENTER);
                    imageContainer.setStyle("-fx-padding: 10; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 8;");
                    content.getChildren().add(0, imageContainer);
                }
            } catch (Exception e) {}
        }

        dialog.getDialogPane().setContent(content);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    // ==================== NAVIGATION ====================
    public void showMenuView() {
        if (mainContentArea != null && menuView != null) {
            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(menuView);
            isInMenuView = true;
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            Scene scene = welcomeLabel.getScene();
            if (scene != null) {
                String currentTheme = ThemeManager.getTheme();
                if (currentTheme != null) {
                    URL cssUrl = getClass().getResource(currentTheme);
                    if (cssUrl != null) {
                        String css = cssUrl.toExternalForm();
                        if (!scene.getStylesheets().contains(css)) {
                            scene.getStylesheets().add(css);
                        }
                    }
                }
            }

            if (loader.getController() instanceof OrderHistoryController) {
                OrderHistoryController controller = loader.getController();
                controller.setLoggedInUser(loggedInUser);
                controller.setMenuController(this);
                controller.refreshTheme();
            }

            if (loader.getController() instanceof SettingsController) {
                SettingsController controller = loader.getController();
                controller.setLoggedInUser(loggedInUser);
                controller.setMenuController(this);
            }

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(node);
            isInMenuView = false;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load view: " + fxmlPath);
        }
    }

    @FXML
    private void handleOpenSettings() {
        SoundHelper.playTapSound();
        loadView("/settingsView.fxml");
    }

    @FXML
    private void handleOrderHistory() {
        SoundHelper.playTapSound();

        if (loggedInUser == null) {
            showAlert("Login Required", "Please log in to view your order history.");
            showLoginDialog();
            return;
        }
        loadView("/orderHistoryView.fxml");
    }

    // ==================== THEME ====================
    public void refreshTheme() {
        Scene scene = welcomeLabel.getScene();
        if (scene != null) {
            String theme = ThemeManager.getTheme();
            if (theme != null) {
                URL cssUrl = getClass().getResource(theme);
                if (cssUrl != null) {
                    String css = cssUrl.toExternalForm();
                    scene.getStylesheets().clear();
                    scene.getStylesheets().add(css);
                }
            }
        }
        if (isInMenuView) {
            createCharacterTabs();
        }
    }

    // ==================== ALERTS ====================
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}