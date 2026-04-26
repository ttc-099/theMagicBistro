package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.models.Order;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

public class CartManager {
    private final Order currentOrder;
    private final ListView<FoodItem> cartListView;
    private final Label totalLabel;

    public CartManager(Order order, ListView<FoodItem> listView, Label label) {
        this.currentOrder = order;
        this.cartListView = listView;
        this.totalLabel = label;
        setupCellFactory();
    }

    private void setupCellFactory() {
        cartListView.setCellFactory(lv -> new javafx.scene.control.ListCell<FoodItem>() {
            @Override
            protected void updateItem(FoodItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(item.getName());
                    nameLabel.setPrefWidth(150);

                    javafx.scene.control.Label priceLabel = new javafx.scene.control.Label(String.format("RM%.2f", item.getPrice()));

                    javafx.scene.control.Button removeBtn = new javafx.scene.control.Button("✖");
                    removeBtn.setOnAction(e -> removeItem(item));

                    hbox.getChildren().addAll(nameLabel, priceLabel, removeBtn);
                    setGraphic(hbox);
                }
            }
        });
    }

    public void addItem(FoodItem item) {
        currentOrder.addItem(item);
        refreshDisplay();
    }

    public void removeItem(FoodItem item) {
        currentOrder.removeItem(item);
        refreshDisplay();
    }

    public void clearCart() {
        currentOrder.getItems().clear();
        currentOrder.calculateTotal();
        refreshDisplay();
    }

    public void refreshDisplay() {
        cartListView.setItems(FXCollections.observableArrayList(currentOrder.getItems()));
        totalLabel.setText(String.format("Total: RM%.2f", currentOrder.getTotalPrice()));
    }

    public boolean isEmpty() {
        return currentOrder.getItems().isEmpty();
    }

    public Order getOrder() {
        return currentOrder;
    }
}