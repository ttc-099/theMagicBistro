package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.database.DatabaseConnection;
import com.files.projBistro.models.userModel.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AdminFeedbackController {

    @FXML private TextField searchField;
    @FXML private Button searchBtn;
//    @FXML private Label infoIcon;
//    @FXML private Tooltip infoTooltip;

    // stats cards at the top
    @FXML private Label avgRatingLabel;
    @FXML private Label totalResponsesLabel;
    @FXML private Label responseRateLabel;

    // loyalty table - shows top customers by spending
    @FXML private TableView<LoyaltyEntry> loyaltyTable;
    @FXML private TableColumn<LoyaltyEntry, String> colUsername;
    @FXML private TableColumn<LoyaltyEntry, Integer> colTotalOrders;
    @FXML private TableColumn<LoyaltyEntry, Double> colTotalSpent;
    @FXML private TableColumn<LoyaltyEntry, String> colPhoneNumber;
    @FXML private TableColumn<LoyaltyEntry, String> colLastOrder;

    // feedback table - shows all customer reviews
    @FXML private TableView<FeedbackEntry> feedbackTable;
    @FXML private TableColumn<FeedbackEntry, Integer> colOrderId;
    @FXML private TableColumn<FeedbackEntry, String> colCustomerName;
    @FXML private TableColumn<FeedbackEntry, String> colFeedbackDate;
    @FXML private TableColumn<FeedbackEntry, String> colRatingStars;
    @FXML private TableColumn<FeedbackEntry, String> colCommentPreview;

    // filter controls
    @FXML private ComboBox<Integer> ratingFilter;
    @FXML private DatePicker startDateFilter;
    @FXML private DatePicker endDateFilter;

    // details panel (for full comment)
    @FXML private ScrollPane detailScrollPane;
    @FXML private VBox detailsContent;

    private ObservableList<LoyaltyEntry> loyaltyList = FXCollections.observableArrayList();
    private ObservableList<FeedbackEntry> feedbackList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupRatingFilter();
        // setupInfoIcon(); not used anymore
        setupTableSelectionListener();
        refreshAllData();
    }

    // connect table columns to object properties
    private void setupTableColumns() {
        // Loyalty table columns
        colUsername.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        colTotalOrders.setCellValueFactory(cellData -> cellData.getValue().totalOrdersProperty().asObject());
        colTotalSpent.setCellValueFactory(cellData -> cellData.getValue().totalSpentProperty().asObject());
        colPhoneNumber.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
        colLastOrder.setCellValueFactory(cellData -> cellData.getValue().lastOrderProperty());

        // feedback table
        colOrderId.setCellValueFactory(cellData -> cellData.getValue().orderIdProperty().asObject());
        colCustomerName.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        colFeedbackDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colRatingStars.setCellValueFactory(cellData -> cellData.getValue().ratingStarsProperty());
        colCommentPreview.setCellValueFactory(cellData -> cellData.getValue().commentPreviewProperty());
    }

    private void setupRatingFilter() {
        ratingFilter.getItems().addAll(1, 2, 3, 4, 5);
        ratingFilter.setPromptText("All Ratings");
    }

//    private void setupInfoIcon() {
//        Tooltip.install(infoIcon, infoTooltip);
//        infoTooltip.setText("Filter by customer name, order ID, or rating.\nClick any row to view full feedback details.");
//    }

    private void setupTableSelectionListener() {
        feedbackTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                showFullFeedbackDetails(selected);
            }
        });
    }

    private void refreshAllData() {
        loadStats();
        loadLoyaltyData();
        loadFeedbackData();
    }

    // load the summary stats from database
    private void loadStats() {
        String sql = "SELECT COUNT(*) as total_feedback, AVG(rating) as avg_rating FROM feedback";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int totalFeedback = rs.getInt("total_feedback");
                double avgRating = rs.getDouble("avg_rating");

                totalResponsesLabel.setText(String.valueOf(totalFeedback));
                avgRatingLabel.setText(String.format("%.1f", avgRating));

                // Calculate response rate (total feedback / total orders)
                String orderSql = "SELECT COUNT(*) as total_orders FROM orders WHERE status = 'completed'";
                try (Statement stmt2 = conn.createStatement();
                     ResultSet rs2 = stmt2.executeQuery(orderSql)) {
                    if (rs2.next()) {
                        int totalOrders = rs2.getInt("total_orders");
                        double rate = totalOrders > 0 ? (double) totalFeedback / totalOrders * 100 : 0;
                        responseRateLabel.setText(String.format("%.1f%%", rate));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // load customer loyalty data - shows who spends the most
    private void loadLoyaltyData() {
        loyaltyList.clear();
        String sql = "SELECT u.username, u.phone_number, " +
                "COUNT(o.order_id) as total_orders, " +
                "COALESCE(SUM(o.total_price), 0) as total_spent, " +
                "MAX(o.order_date) as last_order " +
                "FROM users u " +
                "LEFT JOIN orders o ON u.user_id = o.user_id AND o.status = 'completed' " +
                "WHERE u.role = 'Customer' " +
                "GROUP BY u.user_id " +
                "ORDER BY total_spent DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LoyaltyEntry entry = new LoyaltyEntry(
                        rs.getString("username"),
                        rs.getInt("total_orders"),
                        rs.getDouble("total_spent"),
                        rs.getString("phone_number"),
                        rs.getTimestamp("last_order") != null ? rs.getTimestamp("last_order").toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Never"
                );
                loyaltyList.add(entry);
            }
            loyaltyTable.setItems(loyaltyList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFeedbackData() {
        loadFeedbackDataWithFilters(null, null, null);
    }

    // load feedback with optional filters (rating, date range)
    private void loadFeedbackDataWithFilters(Integer rating, LocalDate startDate, LocalDate endDate) {
        feedbackList.clear();

        StringBuilder sql = new StringBuilder(
                "SELECT f.feedback_id, f.order_id, f.rating, f.comment, f.created_at, " +
                        "u.username, o.total_price " +
                        "FROM feedback f " +
                        "JOIN users u ON f.user_id = u.user_id " +
                        "JOIN orders o ON f.order_id = o.order_id " +
                        "WHERE 1=1 "
        );

        if (rating != null) {
            sql.append("AND f.rating = ").append(rating).append(" ");
        }
        if (startDate != null) {
            sql.append("AND DATE(f.created_at) >= '").append(startDate).append("' ");
        }
        if (endDate != null) {
            sql.append("AND DATE(f.created_at) <= '").append(endDate).append("' ");
        }

        sql.append("ORDER BY f.created_at DESC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                // Build star rating display
                int ratingValue = rs.getInt("rating");
                String stars = "";
                for (int i = 0; i < ratingValue; i++) stars += "\u2605";  // ★
                for (int i = ratingValue; i < 5; i++) stars += "\u2606";  // ☆

                String comment = rs.getString("comment");
                String preview = comment != null && comment.length() > 50 ? comment.substring(0, 47) + "..." : (comment != null ? comment : "(No comment)");

                FeedbackEntry entry = new FeedbackEntry(
                        rs.getInt("order_id"),
                        rs.getString("username"),
                        rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        stars,
                        preview,
                        rs.getString("comment"),
                        ratingValue
                );
                feedbackList.add(entry);
            }
            feedbackTable.setItems(feedbackList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // show full feedback when clicking a row - includes ordered items
    private void showFullFeedbackDetails(FeedbackEntry entry) {
        detailsContent.getChildren().clear();

        // load all items from that order
        StringBuilder itemsList = new StringBuilder();
        String sql = "SELECT f.name, oi.quantity, oi.price_at_purchase " +
                "FROM order_items oi " +
                "JOIN food_items f ON oi.item_id = f.item_id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entry.getOrderId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                itemsList.append("• ").append(rs.getString("name"))
                        .append(" x").append(rs.getInt("quantity"))
                        .append(" (RM").append(String.format("%.2f", rs.getDouble("price_at_purchase"))).append(")\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // building the details panel
        Label customerLabel = new Label("Customer: " + entry.getCustomerName());
        customerLabel.setStyle("-fx-font-weight: bold;");

        Label orderLabel = new Label("Order #: " + entry.getOrderId());
        Label dateLabel = new Label("Date: " + entry.getDate());
        Label ratingLabel = new Label("Rating: " + entry.getRatingStars());
        Label itemsLabel = new Label("Items Ordered:");
        itemsLabel.setStyle("-fx-font-weight: bold; -fx-margin-top: 10;");
        TextArea itemsArea = new TextArea(itemsList.toString());
        itemsArea.setEditable(false);
        itemsArea.setWrapText(true);
        itemsArea.setPrefRowCount(4);
        Label commentLabel = new Label("Comment:");
        commentLabel.setStyle("-fx-font-weight: bold; -fx-margin-top: 10;");
        TextArea commentArea = new TextArea(entry.getFullComment() != null ? entry.getFullComment() : "(No comment)");
        commentArea.setEditable(false);
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(3);

        detailsContent.getChildren().addAll(customerLabel, orderLabel, dateLabel, ratingLabel, itemsLabel, itemsArea, commentLabel, commentArea);
    }

//    @FXML (scrapped!)
//    private void handleSearch() {
//        String searchTerm = searchField.getText().toLowerCase();
//        if (searchTerm.isEmpty()) {
//            refreshAllData();
//            return;
//        }
//
//        ObservableList<FeedbackEntry> filtered = FXCollections.observableArrayList();
//        for (FeedbackEntry entry : feedbackList) {
//            if (entry.getCustomerName().toLowerCase().contains(searchTerm) ||
//                    String.valueOf(entry.getOrderId()).contains(searchTerm)) {
//                filtered.add(entry);
//            }
//        }
//        feedbackTable.setItems(filtered);
//    }

    @FXML
    private void handleApplyFilters() {
        Integer rating = ratingFilter.getValue();
        LocalDate startDate = startDateFilter.getValue();
        LocalDate endDate = endDateFilter.getValue();
        loadFeedbackDataWithFilters(rating, startDate, endDate);
    }

    @FXML
    private void handleResetFilters() {
        ratingFilter.setValue(null);
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        searchField.clear();
        loadFeedbackData();
    }

    // >>>>>>>> Inner Classes for Table Data >>>>>>>>>>>>>>>>

    public static class LoyaltyEntry {
        private final SimpleStringProperty username;
        private final SimpleIntegerProperty totalOrders;
        private final SimpleDoubleProperty totalSpent;
        private final SimpleStringProperty phoneNumber;
        private final SimpleStringProperty lastOrder;

        public LoyaltyEntry(String username, int totalOrders, double totalSpent, String phoneNumber, String lastOrder) {
            this.username = new SimpleStringProperty(username);
            this.totalOrders = new SimpleIntegerProperty(totalOrders);
            this.totalSpent = new SimpleDoubleProperty(totalSpent);
            this.phoneNumber = new SimpleStringProperty(phoneNumber != null ? phoneNumber : "N/A");
            this.lastOrder = new SimpleStringProperty(lastOrder);
        }

        public String getUsername() { return username.get(); }
//        public int getTotalOrders() { return totalOrders.get(); }
//        public double getTotalSpent() { return totalSpent.get(); }
//        public String getPhoneNumber() { return phoneNumber.get(); }
//        public String getLastOrder() { return lastOrder.get(); }

        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleIntegerProperty totalOrdersProperty() { return totalOrders; }
        public SimpleDoubleProperty totalSpentProperty() { return totalSpent; }
        public SimpleStringProperty phoneNumberProperty() { return phoneNumber; }
        public SimpleStringProperty lastOrderProperty() { return lastOrder; }
    }

    public static class FeedbackEntry {
        private final SimpleIntegerProperty orderId;
        private final SimpleStringProperty customerName;
        private final SimpleStringProperty date;
        private final SimpleStringProperty ratingStars;
        private final SimpleStringProperty commentPreview;
        private final String fullComment;
        private final int ratingValue;

        public FeedbackEntry(int orderId, String customerName, String date, String ratingStars, String commentPreview, String fullComment, int ratingValue) {
            this.orderId = new SimpleIntegerProperty(orderId);
            this.customerName = new SimpleStringProperty(customerName);
            this.date = new SimpleStringProperty(date);
            this.ratingStars = new SimpleStringProperty(ratingStars);
            this.commentPreview = new SimpleStringProperty(commentPreview);
            this.fullComment = fullComment;
            this.ratingValue = ratingValue;
        }

        public int getOrderId() { return orderId.get(); }
        public String getCustomerName() { return customerName.get(); }
        public String getDate() { return date.get(); }
        public String getRatingStars() { return ratingStars.get(); }
//        public String getCommentPreview() { return commentPreview.get(); }
        public String getFullComment() { return fullComment; }
//        public int getRatingValue() { return ratingValue; }

        public SimpleIntegerProperty orderIdProperty() { return orderId; }
        public SimpleStringProperty customerNameProperty() { return customerName; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty ratingStarsProperty() { return ratingStars; }
        public SimpleStringProperty commentPreviewProperty() { return commentPreview; }
    }
}