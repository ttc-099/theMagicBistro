package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.models.Order;
import com.files.projBistro.models.dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.fxml.FXML;

import java.util.List;

public class ActiveOrdersController {

    @FXML private ListView<Order> ordersListView;
    @FXML private Label statusLabel;

    private OrderDAO orderDAO = new OrderDAO();
    private Order selectedOrder = null;

    @FXML
    public void initialize() {
        System.out.println("=== ActiveOrdersController initialized ===");
        setupListView();
        refreshOrders();
    }

    private void setupListView() {
        ordersListView.setCellFactory(listView -> new ListCell<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);

                if (empty || order == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                VBox card = new VBox(10);
                card.setMaxWidth(Double.MAX_VALUE);
                card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15;");

                // header with order id and customer
                HBox headerBox = new HBox(15);
                headerBox.setAlignment(Pos.CENTER_LEFT);

                Label orderIdLabel = new Label("order #" + order.getOrderId());
                orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

                Label customerLabel = new Label("customer: " + order.getCustomerName());
                customerLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label statusBadge = new Label(order.getStatus());
                statusBadge.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 11px;");

                headerBox.getChildren().addAll(orderIdLabel, customerLabel, spacer, statusBadge);

                // ===== PHONE NUMBER ROW =====
                HBox phoneBox = new HBox(10);
                phoneBox.setAlignment(Pos.CENTER_LEFT);


                String phoneNumber = order.getCustomerPhone();
                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    phoneNumber = "No phone number!";
                }

                Label phoneLabel = new Label(" \uD83D\uDCDE Phone: " + phoneNumber);
                phoneLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

                phoneBox.getChildren().addAll(phoneLabel);

                // total and date
                HBox metaBox = new HBox(20);
                metaBox.setAlignment(Pos.CENTER_LEFT);

                Label totalLabel = new Label(String.format("Total: £%.2f", order.getTotalPrice()));
                totalLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

                String dateStr = order.getOrderDate() != null ? order.getOrderDate().toString() : "unknown date";
                Label dateLabel = new Label("Placed: " + dateStr);
                dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");

                metaBox.getChildren().addAll(totalLabel, dateLabel);

                Separator separator = new Separator();

                // items
                Label itemsLabel = new Label("Ordered items:");
                itemsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 13px;");

                VBox itemsBox = new VBox(5);
                itemsBox.setStyle("-fx-padding: 5 0 0 15;");

                for (FoodItem item : order.getItems()) {
                    HBox itemRow = new HBox(10);
                    itemRow.setAlignment(Pos.CENTER_LEFT);

                    Label bulletLabel = new Label("•");
                    bulletLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

                    Label itemNameLabel = new Label(item.getName());
                    itemNameLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

                    Label itemPriceLabel = new Label(String.format("£%.2f", item.getPrice()));
                    itemPriceLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

                    Region itemSpacer = new Region();
                    HBox.setHgrow(itemSpacer, Priority.ALWAYS);

                    itemRow.getChildren().addAll(bulletLabel, itemNameLabel, itemSpacer, itemPriceLabel);
                    itemsBox.getChildren().add(itemRow);
                }

                card.getChildren().addAll(headerBox, phoneBox, metaBox, separator, itemsLabel, itemsBox);
                setGraphic(card);
            }
        });

        // track selection
        ordersListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedOrder = newVal;
            if (newVal != null) {
                System.out.println("DEBUG: selected order #" + newVal.getOrderId());
            }
        });
    }

    @FXML
    public void refreshOrders() {
        System.out.println("=== refreshOrders() called ===");
        List<Order> orders = orderDAO.getActiveOrders();

        System.out.println("Orders list size: " + orders.size());
        for (Order o : orders) {
            System.out.println("  - order #" + o.getOrderId() + " | " + o.getCustomerName());
        }

        ordersListView.setItems(FXCollections.observableArrayList(orders));

        if (orders.isEmpty()) {
            statusLabel.setText("No active orders");
        } else if (orders.size() == 1) {
            statusLabel.setText("1 active order");
        } else {
            statusLabel.setText(orders.size() + " active orders");
        }

        ordersListView.refresh();
    }

    @FXML
    private void handleCompleteOrder() {
        Order selected = ordersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please click on an order to select it first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Complete order");
        confirm.setHeaderText("Complete order #" + selected.getOrderId() + "?");
        confirm.setContentText("Mark this order as completed?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (orderDAO.completeOrder(selected.getOrderId())) {
                refreshOrders();
                showAlert("Success", "Order #" + selected.getOrderId() + " completed!");
            } else {
                showAlert("Error", "Failed to complete order.");
            }
        }
    }

    @FXML
    private void handleCancelOrder() {
        Order selected = ordersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please click on an order to select it first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("cancel order");
        confirm.setHeaderText("cancel order #" + selected.getOrderId() + "?");
        confirm.setContentText("this action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (orderDAO.cancelOrder(selected.getOrderId())) {
                refreshOrders();
                showAlert("Success", "Order #" + selected.getOrderId() + " cancelled.");
            } else {
                showAlert("Error", "Failed to cancel order.");
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}