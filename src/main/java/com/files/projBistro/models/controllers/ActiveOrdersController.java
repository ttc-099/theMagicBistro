package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.models.Order;
import com.files.projBistro.models.dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.fxml.FXML;

import java.util.List;
import java.util.WeakHashMap;

public class ActiveOrdersController {

    @FXML private ListView<Order> ordersListView;
    @FXML private Label statusLabel;
    @FXML private Label selectedCountLabel;

    private OrderDAO orderDAO = new OrderDAO();
    private boolean isUpdating = false;
    // Cache to store card VBoxes for each order to prevent rebuilding
    private final WeakHashMap<Order, VBox> cardCache = new WeakHashMap<>();

    @FXML
    public void initialize() {
        System.out.println("=== ActiveOrdersController initialized ===");
        setupListView();
        refreshOrders();
    }

    private void setupListView() {
        ordersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        ordersListView.setCellFactory(listView -> new ListCell<Order>() {

            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);

                if (empty || order == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Try to get cached card, create new if not exists
                VBox card = cardCache.get(order);
                if (card == null) {
                    card = createOrderCard(order);
                    cardCache.put(order, card);
                } else {
                    // Update only the checkbox state and status badge (lightweight updates)
                    updateCardState(card, order);
                }

                setGraphic(card);
            }
        });

        // Update selected count without any refresh
        ordersListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Order>) change -> {
            updateSelectedCount();
            // Update checkbox states without rebuilding cards
            updateAllCheckboxStates();
        });
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox(10);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15;");
        card.setUserData(order); // Store order reference

        // Checkbox for selection
        CheckBox selectCheckBox = new CheckBox();
        selectCheckBox.setSelected(ordersListView.getSelectionModel().getSelectedItems().contains(order));
        selectCheckBox.setOnAction(event -> {
            if (isUpdating) return;
            isUpdating = true;
            if (selectCheckBox.isSelected()) {
                ordersListView.getSelectionModel().select(order);
            } else {
                ordersListView.getSelectionModel().clearSelection(ordersListView.getItems().indexOf(order));
            }
            isUpdating = false;
        });

        // Header
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

        headerBox.getChildren().addAll(selectCheckBox, orderIdLabel, customerLabel, spacer, statusBadge);

        // Phone number row
        HBox phoneBox = new HBox(10);
        phoneBox.setAlignment(Pos.CENTER_LEFT);

        String phoneNumber = order.getCustomerPhone();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = "No phone number!";
        }

        Label phoneLabel = new Label(" \uD83D\uDCDE Phone: " + phoneNumber);
        phoneLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        phoneBox.getChildren().addAll(phoneLabel);

        // Total and date
        HBox metaBox = new HBox(20);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        Label totalLabel = new Label(String.format("Total: RM%.2f", order.getTotalPrice()));
        totalLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");

        String dateStr = order.getOrderDate() != null ? order.getOrderDate().toString() : "unknown date";
        Label dateLabel = new Label("Placed: " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");

        metaBox.getChildren().addAll(totalLabel, dateLabel);

        Separator separator = new Separator();

        // Items
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

            Label itemPriceLabel = new Label(String.format("RM%.2f", item.getPrice()));
            itemPriceLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

            Region itemSpacer = new Region();
            HBox.setHgrow(itemSpacer, Priority.ALWAYS);

            itemRow.getChildren().addAll(bulletLabel, itemNameLabel, itemSpacer, itemPriceLabel);
            itemsBox.getChildren().add(itemRow);
        }

        card.getChildren().addAll(headerBox, phoneBox, metaBox, separator, itemsLabel, itemsBox);

        // Store references for later updates
        card.setUserData(new CardData(selectCheckBox, statusBadge, order));

        return card;
    }

    private void updateCardState(VBox card, Order order) {
        CardData data = (CardData) card.getUserData();
        if (data != null) {
            // Update checkbox state without triggering event
            isUpdating = true;
            data.checkBox.setSelected(ordersListView.getSelectionModel().getSelectedItems().contains(order));
            isUpdating = false;

            // Update status badge if changed
            if (!data.statusBadge.getText().equals(order.getStatus())) {
                data.statusBadge.setText(order.getStatus());
            }
        }
    }

    private void updateAllCheckboxStates() {
        isUpdating = true;
        for (Order order : ordersListView.getItems()) {
            VBox card = cardCache.get(order);
            if (card != null) {
                CardData data = (CardData) card.getUserData();
                if (data != null) {
                    data.checkBox.setSelected(ordersListView.getSelectionModel().getSelectedItems().contains(order));
                }
            }
        }
        isUpdating = false;
    }

    // Helper class to store references
    private static class CardData {
        final CheckBox checkBox;
        final Label statusBadge;
        final Order order;

        CardData(CheckBox checkBox, Label statusBadge, Order order) {
            this.checkBox = checkBox;
            this.statusBadge = statusBadge;
            this.order = order;
        }
    }

    private void updateSelectedCount() {
        int selectedCount = ordersListView.getSelectionModel().getSelectedItems().size();
        if (selectedCountLabel != null) {
            selectedCountLabel.setText(selectedCount + " selected");
        }
    }

    @FXML
    public void refreshOrders() {
        System.out.println("=== refreshOrders() called ===");
        List<Order> orders = orderDAO.getActiveOrders();

        // Clear cache for removed orders
        cardCache.keySet().retainAll(orders);

        ordersListView.setItems(FXCollections.observableArrayList(orders));

        if (orders.isEmpty()) {
            statusLabel.setText("No active orders");
        } else if (orders.size() == 1) {
            statusLabel.setText("1 active order");
        } else {
            statusLabel.setText(orders.size() + " active orders");
        }

        updateSelectedCount();
    }

    @FXML
    private void handleCompleteOrders() {
        List<Order> selectedOrders = ordersListView.getSelectionModel().getSelectedItems();

        if (selectedOrders.isEmpty()) {
            showAlert("No selection", "Please select at least one order to complete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Complete Orders");
        confirm.setHeaderText("Complete " + selectedOrders.size() + " order(s)?");
        confirm.setContentText("Mark the selected orders as completed?");

        ButtonType completeButton = new ButtonType("Complete All", ButtonBar.ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(completeButton, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == completeButton) {
            int successCount = 0;
            int failCount = 0;
            StringBuilder failedOrders = new StringBuilder();

            for (Order order : selectedOrders) {
                if (orderDAO.completeOrder(order.getOrderId())) {
                    successCount++;
                } else {
                    failCount++;
                    failedOrders.append("#").append(order.getOrderId()).append(" ");
                }
            }

            refreshOrders();

            if (failCount == 0) {
                showAlert("Success", successCount + " order(s) completed successfully!");
            } else {
                showAlert("Partial Success",
                        successCount + " order(s) completed.\n" +
                                failCount + " failed: " + failedOrders.toString());
            }
        }
    }

    @FXML
    private void handleCancelOrders() {
        List<Order> selectedOrders = ordersListView.getSelectionModel().getSelectedItems();

        if (selectedOrders.isEmpty()) {
            showAlert("No selection", "Please select at least one order to cancel.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Orders");
        confirm.setHeaderText("Cancel " + selectedOrders.size() + " order(s)?");
        confirm.setContentText("This action cannot be undone.");

        ButtonType cancelButton = new ButtonType("Cancel All", ButtonBar.ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(cancelButton, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == cancelButton) {
            int successCount = 0;
            int failCount = 0;

            for (Order order : selectedOrders) {
                if (orderDAO.cancelOrder(order.getOrderId())) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            refreshOrders();

            if (failCount == 0) {
                showAlert("Success", successCount + " order(s) cancelled successfully!");
            } else {
                showAlert("Partial Success",
                        successCount + " order(s) cancelled.\n" + failCount + " failed.");
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