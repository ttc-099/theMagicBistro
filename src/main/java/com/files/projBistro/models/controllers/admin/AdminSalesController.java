package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.models.Order;
import com.files.projBistro.models.dao.SalesDAO;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class AdminSalesController {

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Label totalRevenueLabel;
    private Label totalOrdersLabel;
    private Label totalItemsSoldLabel;
    private Label avgOrderValueLabel;
    private ListView<SalesDAO.PopularItem> popularItemsList;
    private TableView<Order> recentOrdersTable;
    private TableColumn<Order, Integer> colOrderIdSales;
    private TableColumn<Order, String> colCustomerName;
    private TableColumn<Order, Double> colOrderTotal;
    private TableColumn<Order, String> colOrderDate;
    private TableColumn<Order, String> colOrderStatus;

    // filter UI elements
    private ComboBox<String> categoryFilterBox;
    private TextField minPriceFilter;
    private TextField maxPriceFilter;
    private Button applyFiltersBtn;

    private SalesDAO salesDAO;
    private Consumer<String> showStatus;

    public void init(SalesDAO salesDAO, Label statusLabel, Consumer<String> showStatus) {
        this.salesDAO = salesDAO;
        this.showStatus = showStatus;
    }

    public void setUIElements(DatePicker startDatePicker,
                              DatePicker endDatePicker,
                              Label totalRevenueLabel,
                              Label totalOrdersLabel,
                              Label totalItemsSoldLabel,
                              Label avgOrderValueLabel,
                              ListView<SalesDAO.PopularItem> popularItemsList,
                              TableView<Order> recentOrdersTable,
                              TableColumn<Order, Integer> colOrderIdSales,
                              TableColumn<Order, String> colCustomerName,
                              TableColumn<Order, Double> colOrderTotal,
                              TableColumn<Order, String> colOrderDate,
                              TableColumn<Order, String> colOrderStatus,
                              ComboBox<String> categoryFilterBox,
                              TextField minPriceFilter,
                              TextField maxPriceFilter,
                              Button applyFiltersBtn) {
        this.startDatePicker = startDatePicker;
        this.endDatePicker = endDatePicker;
        this.totalRevenueLabel = totalRevenueLabel;
        this.totalOrdersLabel = totalOrdersLabel;
        this.totalItemsSoldLabel = totalItemsSoldLabel;
        this.avgOrderValueLabel = avgOrderValueLabel;
        this.popularItemsList = popularItemsList;
        this.recentOrdersTable = recentOrdersTable;
        this.colOrderIdSales = colOrderIdSales;
        this.colCustomerName = colCustomerName;
        this.colOrderTotal = colOrderTotal;
        this.colOrderDate = colOrderDate;
        this.colOrderStatus = colOrderStatus;
        this.categoryFilterBox = categoryFilterBox;
        this.minPriceFilter = minPriceFilter;
        this.maxPriceFilter = maxPriceFilter;
        this.applyFiltersBtn = applyFiltersBtn;

        setupTableColumns();
        setupPopularItemsList();
        setupFilters();
    }

    private void setupTableColumns() {
        colOrderIdSales.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));
        colCustomerName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerName"));
        colOrderTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalPrice"));
        colOrderDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderDate"));
        colOrderStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
    }

    private void setupPopularItemsList() {
        popularItemsList.setCellFactory(listView -> new ListCell<SalesDAO.PopularItem>() {
            @Override
            protected void updateItem(SalesDAO.PopularItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(5);
                    card.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #444; -fx-border-radius: 6;");
                    Label nameLabel = new Label(item.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
                    javafx.scene.layout.HBox statsBox = new javafx.scene.layout.HBox(20);
                    Label timesLabel = new Label("ordered: " + item.getTimesOrdered() + " times");
                    timesLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
                    Label revenueLabel = new Label(String.format("revenue: £%.2f", item.getRevenue()));
                    revenueLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12px; -fx-font-weight: bold;");
                    statsBox.getChildren().addAll(timesLabel, revenueLabel);
                    card.getChildren().addAll(nameLabel, statsBox);
                    setGraphic(card);
                }
            }
        });
    }

    private void setupFilters() {
        // populate category dropdown
        categoryFilterBox.setItems(FXCollections.observableArrayList(
                "All", "Main", "Appetizer", "Dessert", "Drink", "Special"
        ));
        categoryFilterBox.setValue("All");

        // set button action
        applyFiltersBtn.setOnAction(e -> refreshSummaryWithFilters());
    }

    public void refreshSummary() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(startDate, endDate);
        totalRevenueLabel.setText(String.format("£%.2f", summary.getTotalRevenue()));
        totalOrdersLabel.setText(String.valueOf(summary.getTotalOrders()));
        totalItemsSoldLabel.setText(String.valueOf(summary.getTotalItemsSold()));
        avgOrderValueLabel.setText(String.format("£%.2f", summary.getAvgOrderValue()));

        List<SalesDAO.PopularItem> popularItems = salesDAO.getPopularItems(startDate, endDate);
        popularItemsList.setItems(FXCollections.observableArrayList(popularItems));

        List<Order> recentOrders = salesDAO.getRecentOrders(startDate, endDate);
        recentOrdersTable.setItems(FXCollections.observableArrayList(recentOrders));

        showStatus.accept("Sales summary updated!");
    }

    public void refreshSummaryWithFilters() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        String category = categoryFilterBox.getValue();
        if ("All".equals(category)) category = null;

        Double minPrice = null;
        Double maxPrice = null;
        try {
            if (minPriceFilter.getText() != null && !minPriceFilter.getText().isEmpty()) {
                minPrice = Double.parseDouble(minPriceFilter.getText());
            }
            if (maxPriceFilter.getText() != null && !maxPriceFilter.getText().isEmpty()) {
                maxPrice = Double.parseDouble(maxPriceFilter.getText());
            }
        } catch (NumberFormatException e) {
            showStatus.accept("Invalid price format...");
            return;
        }

        // get filtered popular items
        List<SalesDAO.PopularItem> popularItems = salesDAO.getPopularItemsWithFilters(startDate, endDate, category, minPrice, maxPrice);
        popularItemsList.setItems(FXCollections.observableArrayList(popularItems));

        // get summary and recent orders (without filters for now)
        SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(startDate, endDate);
        totalRevenueLabel.setText(String.format("£%.2f", summary.getTotalRevenue()));
        totalOrdersLabel.setText(String.valueOf(summary.getTotalOrders()));
        totalItemsSoldLabel.setText(String.valueOf(summary.getTotalItemsSold()));
        avgOrderValueLabel.setText(String.format("£%.2f", summary.getAvgOrderValue()));

        List<Order> recentOrders = salesDAO.getRecentOrders(startDate, endDate);
        recentOrdersTable.setItems(FXCollections.observableArrayList(recentOrders));

        showStatus.accept("Sales summary updated with filters!");
    }

    public void handleExportSales() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(startDate, endDate);
        List<SalesDAO.PopularItem> popularItems = salesDAO.getPopularItems(startDate, endDate);
        List<Order> recentOrders = salesDAO.getRecentOrders(startDate, endDate);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export sales report");
        fileChooser.setInitialFileName("sales_report_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        File file = fileChooser.showSaveDialog(startDatePicker.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write summary section
                writer.println("CAMO-GEAR BISTRO - SALES REPORT");
                writer.println("Generated: " + LocalDate.now());
                writer.println("Date Range: " + (startDate != null ? startDate : "Beginning") + " to " + (endDate != null ? endDate : "Today"));
                writer.println();

                writer.println("KEY METRICS");
                writer.println("Metric,Value");
                writer.printf("Total Revenue,%.2f%n", summary.getTotalRevenue());
                writer.printf("Total Orders,%d%n", summary.getTotalOrders());
                writer.printf("Total Items Sold,%d%n", summary.getTotalItemsSold());
                writer.printf("Average Order Value,%.2f%n", summary.getAvgOrderValue());
                writer.println();

                // Popular items section
                writer.println("MOST POPULAR ITEMS");
                writer.println("Item Name,Times Ordered,Revenue");
                for (SalesDAO.PopularItem item : popularItems) {
                    writer.printf("\"%s\",%d,%.2f%n",
                            escapeCsv(item.getName()),
                            item.getTimesOrdered(),
                            item.getRevenue());
                }
                writer.println();

                // Recent orders section
                writer.println("RECENT ORDERS");
                writer.println("Order ID,Customer Name,Total,Date,Status");
                for (Order o : recentOrders) {
                    String customer = o.getCustomerName() != null ? o.getCustomerName() : "Guest";
                    String date = o.getOrderDate() != null ? o.getOrderDate().toString() : "Unknown";
                    writer.printf("%d,\"%s\",%.2f,%s,%s%n",
                            o.getOrderId(),
                            escapeCsv(customer),
                            o.getTotalPrice(),
                            date,
                            o.getStatus());
                }

                showStatus.accept("Sales report exported to CSV!");
            } catch (IOException e) {
                showStatus.accept("Export failed: " + e.getMessage());
            }
        }
    }

    // Helper method to escape CSV fields (wrap in quotes if contains comma or quote)
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return value.replace("\"", "\"\"");
        }
        return value;
    }
}