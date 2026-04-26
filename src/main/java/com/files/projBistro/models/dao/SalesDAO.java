package com.files.projBistro.models.dao;

import com.files.projBistro.models.database.DatabaseConnection;
import com.files.projBistro.models.models.Order;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// this class handles sales reporting and analytics
// it can generate summaries of revenue, popular items, and recent orders
public class SalesDAO {

    // helper method to get a database connection
    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // this is a simple data holder for sales summary numbers
    // it holds total revenue, number of orders, items sold, and average order value
    public static class SalesSummary {
        private double totalRevenue;
        private int totalOrders;
        private int totalItemsSold;
        private double avgOrderValue;

        // getters
        public double getTotalRevenue() { return totalRevenue; }
        public int getTotalOrders() { return totalOrders; }
        public int getTotalItemsSold() { return totalItemsSold; }
        public double getAvgOrderValue() { return avgOrderValue; }

        // setters
        public void setTotalRevenue(double v) { totalRevenue = v; }
        public void setTotalOrders(int v) { totalOrders = v; }
        public void setTotalItemsSold(int v) { totalItemsSold = v; }
        public void setAvgOrderValue(double v) { avgOrderValue = v; }
    }

    // this is a data holder for popular items
    // it holds the item name, how many times it was ordered, and revenue generated
    public static class PopularItem {
        private String name;
        private int timesOrdered;
        private double revenue;

        public PopularItem(String name, int timesOrdered, double revenue) {
            this.name = name;
            this.timesOrdered = timesOrdered;
            this.revenue = revenue;
        }

        public String getName() { return name; }
        public int getTimesOrdered() { return timesOrdered; }
        public double getRevenue() { return revenue; }
    }

    // get a summary of sales within a date range
    // includes total revenue, order count, items sold, and average order value
    public SalesSummary getSalesSummary(LocalDate startDate, LocalDate endDate) {
        SalesSummary summary = new SalesSummary();

        // sql to count orders and sum revenue (excludes cancelled orders)
        String orderSql = "SELECT COUNT(*) AS order_count, COALESCE(SUM(o.total_price), 0) AS revenue " +
                "FROM orders o WHERE COALESCE(o.status, 'pending') <> 'cancelled'";

        // sql to count total items sold
        String itemsSql = "SELECT COALESCE(SUM(COALESCE(oi.quantity, 1)), 0) AS items_sold " +
                "FROM order_items oi " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE COALESCE(o.status, 'pending') <> 'cancelled'";

        // add date filtering if dates were provided
        boolean useDateRange = startDate != null && endDate != null;
        if (useDateRange) {
            orderSql += " AND DATE(o.order_date) BETWEEN ? AND ?";
            itemsSql += " AND DATE(o.order_date) BETWEEN ? AND ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(orderSql);
             PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {

            // set date parameters if needed
            if (useDateRange) {
                orderStmt.setString(1, startDate.toString());
                orderStmt.setString(2, endDate.toString());
                itemsStmt.setString(1, startDate.toString());
                itemsStmt.setString(2, endDate.toString());
            }

            // get order and revenue data
            ResultSet orderRs = orderStmt.executeQuery();
            if (orderRs.next()) {
                summary.setTotalOrders(orderRs.getInt("order_count"));
                summary.setTotalRevenue(orderRs.getDouble("revenue"));
            }

            // get items sold count
            ResultSet itemsRs = itemsStmt.executeQuery();
            if (itemsRs.next()) {
                summary.setTotalItemsSold(itemsRs.getInt("items_sold"));
            }

            // calculate average order value (total revenue divided by number of orders)
            summary.setAvgOrderValue(summary.getTotalOrders() > 0 ?
                    summary.getTotalRevenue() / summary.getTotalOrders() : 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    // get the most popular items within a date range
    // returns top 10 items ordered most frequently
    public List<PopularItem> getPopularItems(LocalDate startDate, LocalDate endDate) {
        List<PopularItem> items = new ArrayList<>();

        // fixed sql with coalesce to handle null revenue values
        String sql = "SELECT f.name, " +
                "SUM(COALESCE(oi.quantity, 1)) as times_ordered, " +
                "COALESCE(SUM(COALESCE(oi.quantity, 1) * COALESCE(oi.price_at_purchase, 0)), 0) as revenue " +
                "FROM order_items oi " +
                "JOIN food_items f ON oi.item_id = f.item_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE COALESCE(o.status, 'pending') <> 'cancelled'";

        // add date filtering only if BOTH dates are present
        boolean hasDateRange = (startDate != null && endDate != null);
        if (hasDateRange) {
            sql += " AND DATE(o.order_date) BETWEEN ? AND ?";
        }

        sql += " GROUP BY f.item_id, f.name ORDER BY times_ordered DESC, revenue DESC LIMIT 10";

        System.out.println("[SYSTEM] popular items sql: " + sql);  // SYSTEM print
        if (hasDateRange) {
            System.out.println("[SYSTEM] date range: " + startDate + " to " + endDate);
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (hasDateRange) {
                pstmt.setString(1, startDate.toString());
                pstmt.setString(2, endDate.toString());
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PopularItem item = new PopularItem(
                        rs.getString("name"),
                        rs.getInt("times_ordered"),
                        rs.getDouble("revenue")
                );
                items.add(item);
                System.out.println("[SYSTEM] found popular item: " + item.getName() + " (" + item.getTimesOrdered() + " times)");
            }
        } catch (SQLException e) {
            System.out.println("error in getpopularitems: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[SYSTEM] returning " + items.size() + " popular items");
        return items;
    }

    // ===== NEW: get popular items with category and price filters =====
    public List<PopularItem> getPopularItemsWithFilters(LocalDate startDate, LocalDate endDate,
                                                        String category, Double minPrice, Double maxPrice) {
        List<PopularItem> items = new ArrayList<>();

        String sql = "SELECT f.name, " +
                "SUM(COALESCE(oi.quantity, 1)) as times_ordered, " +
                "COALESCE(SUM(COALESCE(oi.quantity, 1) * COALESCE(oi.price_at_purchase, 0)), 0) as revenue " +
                "FROM order_items oi " +
                "JOIN food_items f ON oi.item_id = f.item_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE COALESCE(o.status, 'pending') <> 'cancelled'";

        // add date filter
        boolean hasDateRange = (startDate != null && endDate != null);
        if (hasDateRange) {
            sql += " AND DATE(o.order_date) BETWEEN ? AND ?";
        }

        // add category filter
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            sql += " AND f.item_type = ?";
        }

        // add price range filter
        if (minPrice != null) {
            sql += " AND f.price >= ?";
        }
        if (maxPrice != null) {
            sql += " AND f.price <= ?";
        }

        sql += " GROUP BY f.item_id, f.name ORDER BY times_ordered DESC, revenue DESC LIMIT 10";

        System.out.println("[SYSTEM] filtered popular items sql: " + sql);

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;

            if (hasDateRange) {
                pstmt.setString(paramIndex++, startDate.toString());
                pstmt.setString(paramIndex++, endDate.toString());
            }

            if (category != null && !category.isEmpty() && !category.equals("All")) {
                pstmt.setString(paramIndex++, category);
            }

            if (minPrice != null) {
                pstmt.setDouble(paramIndex++, minPrice);
            }
            if (maxPrice != null) {
                pstmt.setDouble(paramIndex++, maxPrice);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(new PopularItem(
                        rs.getString("name"),
                        rs.getInt("times_ordered"),
                        rs.getDouble("revenue")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // get recent orders within a date range
    // returns up to 50 most recent orders
    public List<Order> getRecentOrders(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, u.username as customer_name, o.total_price, o.status, o.order_date " +
                "FROM orders o " +
                "LEFT JOIN users u ON o.user_id = u.user_id " +
                "WHERE 1=1";

        // add date filtering if dates were provided
        if (startDate != null && endDate != null) {
            sql += " AND DATE(o.order_date) BETWEEN ? AND ?";
        }

        sql += " ORDER BY o.order_date DESC LIMIT 50";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (startDate != null && endDate != null) {
                pstmt.setString(1, startDate.toString());
                pstmt.setString(2, endDate.toString());
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // create an order object for each row
                Order order = new Order(rs.getInt("order_id"), rs.getString("customer_name"));
                order.setTotalPrice(rs.getDouble("total_price"));
                order.setStatus(rs.getString("status"));
                order.setOrderDate(rs.getTimestamp("order_date"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}