package com.files.projBistro.controllers;

import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.Order;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.TilePane;
import javafx.scene.control.Button;

public class MenuController {

    // Change this from ListView to TilePane to match menuView.fxml
    @FXML private TilePane chloeGrid;

    // Keeping the cart as a ListView is fine for a vertical sidebar
    @FXML private ListView<FoodItem> cartListView;
    @FXML private Label totalLabel;

    private Order currentOrder;

    @FXML
    public void initialize() {
        currentOrder = new Order(1, "Guest");

        // Let's create a test item using your Builder
        FoodItem testItem = new FoodItem.FoodItemBuilder()
                .setName("Nitro Nachos")
                .setPrice(8.50)
                .build();

        // Instead of adding to a list, we create a small visual "Button"
        // to represent the item in the grid
        Button foodButton = new Button(testItem.getName() + "\n£" + testItem.getPrice());
        foodButton.setPrefSize(100, 100); // Make it a nice square card

        // Add functionality to the button
        foodButton.setOnAction(e -> {
            addItemToCart(testItem);
        });

        // Add the visual button to the Grid
        chloeGrid.getChildren().add(foodButton);
    }

    // A helper method so both the button and the "Add to Order" btn work
    private void addItemToCart(FoodItem item) {
        currentOrder.addItem(item);
        cartListView.getItems().setAll(currentOrder.getItems());
        totalLabel.setText(String.format("Total: £%.2f", currentOrder.getTotalPrice()));
    }

    @FXML
    private void handleAddItem() {
        // Since we are using a Grid, there isn't a "selected item" in the same way.
        // For now, let's just use the buttons inside the grid to add items!
    }
}