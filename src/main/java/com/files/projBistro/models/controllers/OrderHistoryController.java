package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.models.Order;
import com.files.projBistro.models.dao.OrderDAO;
import com.files.projBistro.models.models.ThemeManager;
import com.files.projBistro.models.userModel.User;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.*;

public class OrderHistoryController {

    @FXML private Label statusLabel;
    @FXML private TilePane itemsTilePane; // grid of item cards

    private final OrderDAO orderDAO = new OrderDAO();
    private User loggedInUser;
    private MenuController menuController;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        loadOrderHistory(); // load when user is set
    }

    public void setMenuController(MenuController controller) {
        this.menuController = controller;
    }

    @FXML
    public void initialize() {
        itemsTilePane.setHgap(20);
        itemsTilePane.setVgap(20);
        itemsTilePane.setPrefColumns(3);
    }

    @FXML
    public void handleBackToMenu() {
        SoundHelper.playTapSound();
        if (menuController != null) {
            menuController.refreshTheme();
            menuController.showMenuView();
        }
    }

    @FXML
    public void loadOrderHistory() {
        SoundHelper.playTapSound();
        if (loggedInUser == null) return;

        itemsTilePane.getChildren().clear();

        // get all completed orders for this user
        List<Order> orders = orderDAO.getCompletedOrdersByUserId(loggedInUser.getId(), loggedInUser.getUsername());

        if (orders.isEmpty()) {
            statusLabel.setText("No completed orders found.");
            return;
        }

        // collect all items from all orders
        List<FoodItem> allItems = new ArrayList<>();
        for (Order order : orders) {
            List<FoodItem> items = orderDAO.getOrderItems(order.getOrderId());
            allItems.addAll(items);
        }

        if (allItems.isEmpty()) {
            statusLabel.setText("No items found in your order history.");
            return;
        }

        // remove duplicates by name - keep first occurrence
        Map<String, FoodItem> uniqueItems = new LinkedHashMap<>();
        for (FoodItem item : allItems) {
            if (!uniqueItems.containsKey(item.getName())) {
                uniqueItems.put(item.getName(), item);
            }
        }

        List<FoodItem> displayItems = new ArrayList<>(uniqueItems.values());
        statusLabel.setText(displayItems.size() + " unique item(s) found in your order history");

        for (FoodItem item : displayItems) {
            VBox itemCard = createOrderedItemCard(item);
            itemsTilePane.getChildren().add(itemCard);
        }
    }

    // creates a card for an item in order history
    // has "order again" button that adds current version of item to cart
    private VBox createOrderedItemCard(FoodItem item) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(200);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("item-card");

        // Image loading
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("item-image");

        // try to load image from the item's image path
        boolean imageLoaded = false;
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                URL resource = getClass().getResource(item.getImagePath());
                if (resource != null) {
                    Image img = new Image(resource.toExternalForm());
                    if (!img.isError()) {
                        imageView.setImage(img);
                        imageLoaded = true;
                    } else {
                        System.out.println("Image error for: " + item.getImagePath());
                    }
                } else {
                    System.out.println("Resource not found: " + item.getImagePath());
                }
            } catch (Exception e) {
                System.out.println("Exception loading image: " + e.getMessage());
            }
        }

        if (!imageLoaded) {
            // Placeholder when no image
            imageView.setImage(null);
        }

        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setMinHeight(110);
        imageContainer.getChildren().add(imageView);

        // Item name
        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add("item-name");
        nameLabel.setMaxWidth(180);
        nameLabel.setAlignment(Pos.CENTER);

        // Price
        Label priceLabel = new Label(String.format("RM%.2f", item.getPrice()));
        priceLabel.getStyleClass().add("item-price");

        // Order Again button
        Button orderAgainBtn = new Button("🔄 Order Again");
        orderAgainBtn.setPrefWidth(120);
        orderAgainBtn.getStyleClass().add("add-button");

        // check if item still exists and has stock
        FoodItem freshItem = menuController != null ? menuController.getItemByName(item.getName()) : null;
        if (freshItem == null || freshItem.getStock() <= 0) {
            orderAgainBtn.setText("Out of Stock");
            orderAgainBtn.setDisable(true);
            orderAgainBtn.getStyleClass().remove("add-button");
            orderAgainBtn.getStyleClass().add("out-of-stock-button");
        } else {
            final FoodItem itemToAdd = freshItem;
            orderAgainBtn.setOnAction(e -> {
                if (menuController != null) {
                    menuController.addItemToCart(itemToAdd);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Added to Cart");
                    alert.setHeaderText(null);
                    alert.setContentText(item.getName() + " has been added to your cart!");
                    alert.showAndWait();
                }
            });
        }

        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, orderAgainBtn);
        return card;
    }

    public void refreshTheme() {
        Scene scene = itemsTilePane.getScene();
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
        loadOrderHistory();
    }
}