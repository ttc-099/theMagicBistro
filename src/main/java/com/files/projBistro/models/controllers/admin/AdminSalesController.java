package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.models.Order;
import com.files.projBistro.models.dao.SalesDAO;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

public class AdminSalesController {

    // ui elements
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

    // Data access
    private SalesDAO salesDAO;
    private Consumer<String> showStatus;

    // >>>>>>> INITIALIZATION >>>>>>>>>>>>>>>
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

    // >>>>>>> UI SETUP >>>>>>>>>>>>>>>
    private void setupTableColumns() {
        colOrderIdSales.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));
        colCustomerName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("customerName"));
        colOrderTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalPrice"));
        colOrderDate.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderDate"));
        colOrderStatus.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
    }

    // custom styling for popular items list - each item shown as a card
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

                    Label revenueLabel = new Label(String.format("revenue: RM%.2f", item.getRevenue()));
                    revenueLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12px; -fx-font-weight: bold;");

                    statsBox.getChildren().addAll(timesLabel, revenueLabel);
                    card.getChildren().addAll(nameLabel, statsBox);
                    setGraphic(card);
                }
            }
        });
    }

    private void setupFilters() {
        categoryFilterBox.setItems(FXCollections.observableArrayList(
                "All", "Main", "Appetizer", "Dessert", "Drink", "Special"
        ));
        categoryFilterBox.setValue("All");
        applyFiltersBtn.setOnAction(e -> refreshSummaryWithFilters());
    }

    // refresh all sales data using selected date range
    public void refreshSummary() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (salesDAO == null) {
            showStatus.accept("Sales data not available.");
            return;
        }

        try {
            SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(startDate, endDate);
            updateSummaryLabels(summary);

            List<SalesDAO.PopularItem> popularItems = salesDAO.getPopularItems(startDate, endDate);
            popularItemsList.setItems(FXCollections.observableArrayList(popularItems));

            List<Order> recentOrders = salesDAO.getRecentOrders(startDate, endDate);
            recentOrdersTable.setItems(FXCollections.observableArrayList(recentOrders));

            showStatus.accept("Sales summary updated!");
        } catch (Exception e) {
            showStatus.accept("Error loading sales data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // refresh with category and price filters applied
    public void refreshSummaryWithFilters() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (salesDAO == null) {
            showStatus.accept("Sales data not available.");
            return;
        }

        try {
            // parse category filter
            String category = categoryFilterBox.getValue();
            if ("All".equals(category)) category = null;

            // Parse price filters with validation
            Double minPrice = parsePriceFilter(minPriceFilter);
            Double maxPrice = parsePriceFilter(maxPriceFilter);

            if (minPrice == null && hasInvalidPriceFilter()) return;

            // get filtered popular items
            List<SalesDAO.PopularItem> popularItems = salesDAO.getPopularItemsWithFilters(
                    startDate, endDate, category, minPrice, maxPrice);
            popularItemsList.setItems(FXCollections.observableArrayList(popularItems));

            // get summary and recent orders
            SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(startDate, endDate);
            updateSummaryLabels(summary);

            List<Order> recentOrders = salesDAO.getRecentOrders(startDate, endDate);
            recentOrdersTable.setItems(FXCollections.observableArrayList(recentOrders));

            showStatus.accept("Sales summary updated with filters!");
        } catch (Exception e) {
            showStatus.accept("Error applying filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to parse price filter
    private Double parsePriceFilter(TextField priceField) {
        String text = priceField.getText();
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasInvalidPriceFilter() {
        String minText = minPriceFilter.getText();
        String maxText = maxPriceFilter.getText();
        return (minText != null && !minText.trim().isEmpty()) ||
                (maxText != null && !maxText.trim().isEmpty());
    }

    private void updateSummaryLabels(SalesDAO.SalesSummary summary) {
        totalRevenueLabel.setText(String.format("RM%.2f", summary.getTotalRevenue()));
        totalOrdersLabel.setText(String.valueOf(summary.getTotalOrders()));
        totalItemsSoldLabel.setText(String.valueOf(summary.getTotalItemsSold()));
        avgOrderValueLabel.setText(String.format("RM%.2f", summary.getAvgOrderValue()));
    }

    // export sales report to either CSV or PDF
    public void handleExportSales() {
        ChoiceDialog<String> formatDialog = new ChoiceDialog<>("CSV", "CSV", "PDF");
        formatDialog.setTitle("Export Format");
        formatDialog.setHeaderText("Choose export format");
        formatDialog.setContentText("Select file format:");

        Optional<String> formatResult = formatDialog.showAndWait();
        if (formatResult.isEmpty()) return;

        String selectedFormat = formatResult.get();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (salesDAO == null) {
            showStatus.accept("Cannot export: Sales data not available.");
            return;
        }

        try {
            SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(startDate, endDate);
            List<SalesDAO.PopularItem> popularItems = salesDAO.getPopularItems(startDate, endDate);
            List<Order> recentOrders = salesDAO.getRecentOrders(startDate, endDate);

            if ("CSV".equals(selectedFormat)) {
                exportToCSV(summary, popularItems, recentOrders, startDate, endDate);
            } else {
                exportToPDF(summary, popularItems, recentOrders, startDate, endDate);
            }
        } catch (Exception e) {
            showStatus.accept("Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToCSV(SalesDAO.SalesSummary summary, List<SalesDAO.PopularItem> popularItems,
                             List<Order> recentOrders, LocalDate startDate, LocalDate endDate) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Sales Report");
        fileChooser.setInitialFileName("sales_report_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        File file = fileChooser.showSaveDialog(getSceneWindow());
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(file)) {
            writeCSVHeader(writer, startDate, endDate);
            writeCSVMetrics(writer, summary);
            writeCSVPopularItems(writer, popularItems);
            writeCSVRecentOrders(writer, recentOrders);
            showStatus.accept("✅ Sales report exported to CSV!");
        } catch (IOException e) {
            showStatus.accept("❌ Export failed: " + e.getMessage());
        }
    }

    private void exportToPDF(SalesDAO.SalesSummary summary, List<SalesDAO.PopularItem> popularItems,
                             List<Order> recentOrders, LocalDate startDate, LocalDate endDate) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Sales Report");
        fileChooser.setInitialFileName("sales_report_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        File file = fileChooser.showSaveDialog(getSceneWindow());
        if (file == null) return;

        try {
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            writePDFHeader(document, startDate, endDate);
            writePDFMetrics(document, summary);
            writePDFPopularItems(document, popularItems);
            writePDFRecentOrders(document, recentOrders);

            document.close();
            showStatus.accept("✅ Sales report exported to PDF!");
        } catch (Exception e) {
            showStatus.accept("❌ PDF export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // csv writing helpers
    private void writeCSVHeader(PrintWriter writer, LocalDate startDate, LocalDate endDate) {
        writer.println("CAMO-GEAR BISTRO - SALES REPORT");
        writer.println("Generated: " + LocalDate.now());
        String range = (startDate != null ? startDate : "Beginning") + " to " + (endDate != null ? endDate : "Today");
        writer.println("Date Range: " + range);
        writer.println();
    }

    private void writeCSVMetrics(PrintWriter writer, SalesDAO.SalesSummary summary) {
        writer.println("KEY METRICS");
        writer.println("Metric,Value");
        writer.printf("Total Revenue,%.2f%n", summary.getTotalRevenue());
        writer.printf("Total Orders,%d%n", summary.getTotalOrders());
        writer.printf("Total Items Sold,%d%n", summary.getTotalItemsSold());
        writer.printf("Average Order Value,%.2f%n", summary.getAvgOrderValue());
        writer.println();
    }

    private void writeCSVPopularItems(PrintWriter writer, List<SalesDAO.PopularItem> popularItems) {
        writer.println("MOST POPULAR ITEMS");
        writer.println("Item Name,Times Ordered,Revenue");
        for (SalesDAO.PopularItem item : popularItems) {
            writer.printf("\"%s\",%d,%.2f%n", escapeCsv(item.getName()), item.getTimesOrdered(), item.getRevenue());
        }
        writer.println();
    }

    private void writeCSVRecentOrders(PrintWriter writer, List<Order> recentOrders) {
        writer.println("RECENT ORDERS");
        writer.println("Order ID,Customer Name,Total,Date,Status");
        for (Order o : recentOrders) {
            String customer = o.getCustomerName() != null ? o.getCustomerName() : "Guest";
            String date = formatDate(o);
            writer.printf("%d,\"%s\",%.2f,%s,%s%n", o.getOrderId(), escapeCsv(customer), o.getTotalPrice(), date, o.getStatus());
        }
    }

    // pdf writing helpers
    private void writePDFHeader(Document document, LocalDate startDate, LocalDate endDate) {
        document.add(new Paragraph("CAMO-GEAR BISTRO")
                .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("SALES REPORT")
                .setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Generated: " + LocalDate.now())
                .setTextAlignment(TextAlignment.CENTER));
        String range = (startDate != null ? startDate : "Beginning") + " to " + (endDate != null ? endDate : "Today");
        document.add(new Paragraph("Date Range: " + range)
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph(" "));
    }

    private void writePDFMetrics(Document document, SalesDAO.SalesSummary summary) {
        document.add(new Paragraph("KEY METRICS").setBold().setFontSize(14));
        Table metricsTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
        metricsTable.setWidth(UnitValue.createPercentValue(100));
        metricsTable.addCell("Total Revenue:");
        metricsTable.addCell(String.format("RM%.2f", summary.getTotalRevenue()));
        metricsTable.addCell("Total Orders:");
        metricsTable.addCell(String.valueOf(summary.getTotalOrders()));
        metricsTable.addCell("Total Items Sold:");
        metricsTable.addCell(String.valueOf(summary.getTotalItemsSold()));
        metricsTable.addCell("Average Order Value:");
        metricsTable.addCell(String.format("RM%.2f", summary.getAvgOrderValue()));
        document.add(metricsTable);
        document.add(new Paragraph(" "));
    }

    private void writePDFPopularItems(Document document, List<SalesDAO.PopularItem> popularItems) {
        document.add(new Paragraph("MOST POPULAR ITEMS").setBold().setFontSize(14));
        Table popularTable = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}));
        popularTable.setWidth(UnitValue.createPercentValue(100));
        popularTable.addCell("Item Name");
        popularTable.addCell("Times Ordered");
        popularTable.addCell("Revenue");

        for (SalesDAO.PopularItem item : popularItems) {
            popularTable.addCell(item.getName());
            popularTable.addCell(String.valueOf(item.getTimesOrdered()));
            popularTable.addCell(String.format("RM%.2f", item.getRevenue()));
        }
        document.add(popularTable);
        document.add(new Paragraph(" "));
    }

    private void writePDFRecentOrders(Document document, List<Order> recentOrders) {
        document.add(new Paragraph("RECENT ORDERS").setBold().setFontSize(14));
        Table ordersTable = new Table(UnitValue.createPercentArray(new float[]{20, 30, 15, 25, 10}));
        ordersTable.setWidth(UnitValue.createPercentValue(100));
        ordersTable.addCell("Order ID");
        ordersTable.addCell("Customer");
        ordersTable.addCell("Total");
        ordersTable.addCell("Date");
        ordersTable.addCell("Status");

        for (Order o : recentOrders) {
            ordersTable.addCell(String.valueOf(o.getOrderId()));
            ordersTable.addCell(o.getCustomerName() != null ? o.getCustomerName() : "Guest");
            ordersTable.addCell(String.format("RM%.2f", o.getTotalPrice()));
            ordersTable.addCell(formatDate(o));
            ordersTable.addCell(o.getStatus());
        }
        document.add(ordersTable);
    }

    // ==================== HELPER METHODS ====================
    private String formatDate(Order order) {
        return order.getOrderDate() != null ? order.getOrderDate().toString() : "Unknown";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return value.replace("\"", "\"\"");
        }
        return value;
    }

    private javafx.stage.Window getSceneWindow() {
        if (startDatePicker != null && startDatePicker.getScene() != null) {
            return startDatePicker.getScene().getWindow();
        }
        return null;
    }


}