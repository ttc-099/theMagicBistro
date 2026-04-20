package com.files.projBistro.models.dao;

import com.files.projBistro.database.DatabaseConnection;
import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Order> getActiveOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.order_id, o.user_id, u.username, u.phone_number, o.total_price, o.status, o.order_date " +
                "FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "WHERE o.status = 'pending' " +
                "ORDER BY o.order_date DESC";

        System.out.println("=== getActiveOrders() called ===");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                int orderId = rs.getInt("order_id");
                String username = rs.getString("username");
                String phoneNumber = rs.getString("phone_number");
                double totalPrice = rs.getDouble("total_price");
                String status = rs.getString("status");
                Timestamp orderDate = rs.getTimestamp("order_date");

                Order order = new Order(orderId, username);
                order.setCustomerPhone(phoneNumber != null ? phoneNumber : "no phone");
                order.setTotalPrice(totalPrice);
                order.setStatus(status);
                order.setOrderDate(orderDate);

                orders.add(order);
            }
            System.out.println("DEBUG: found " + rowCount + " orders in database");

        } catch (SQLException e) {
            System.out.println("SQL ERROR in getActiveOrders: " + e.getMessage());
            e.printStackTrace();
            return orders;
        }

        for (Order order : orders) {
            loadOrderItems(order);
        }

        return orders;
    }

    private void loadOrderItems(Order order) {
        String sql = "SELECT oi.item_id, oi.quantity, oi.price_at_purchase, f.name, f.item_type, f.stock " +
                "FROM order_items oi " +
                "JOIN food_items f ON oi.item_id = f.item_id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, order.getOrderId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FoodItem item = new FoodItem.FoodItemBuilder()
                            .setId(rs.getInt("item_id"))
                            .setName(rs.getString("name"))
                            .setPrice(rs.getDouble("price_at_purchase"))
                            .setItemType(rs.getString("item_type"))
                            .build();
                    order.addItem(item);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL ERROR in loadOrderItems for order #" + order.getOrderId() + ": " + e.getMessage());
        }
    }

    public boolean completeOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'completed' WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'cancelled' WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== FIXED: Better error messages for stock validation =====
    public int saveOrder(Order order, int userId) {
        String orderSql = "INSERT INTO orders (user_id, total_price, status, order_date) VALUES (?, ?, 'pending', CURRENT_TIMESTAMP)";
        String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // First, validate all items have sufficient stock BEFORE inserting anything
            String stockCheckSql = "SELECT item_id, name, stock FROM food_items WHERE item_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(stockCheckSql)) {
                for (FoodItem item : order.getItems()) {
                    checkStmt.setInt(1, item.getId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int currentStock = rs.getInt("stock");
                        String itemName = rs.getString("name");
                        if (currentStock <= 0) {
                            conn.rollback();
                            System.err.println("❌ Out of stock: " + itemName + " (ID: " + item.getId() + ")");
                            return -1;
                        }
                    } else {
                        conn.rollback();
                        System.err.println("❌ Item not found: ID " + item.getId());
                        return -1;
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);
                pstmt.setDouble(2, order.getTotalPrice());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int orderId = rs.next() ? rs.getInt(1) : -1;

                if (orderId == -1) {
                    conn.rollback();
                    return -1;
                }

                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                    for (FoodItem item : order.getItems()) {
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, item.getId());
                        itemStmt.setInt(3, 1);
                        itemStmt.setDouble(4, item.getPrice());
                        itemStmt.executeUpdate();
                    }
                }

                // Now update stock levels (they should all pass since we validated)
                updateStockLevels(conn, order.getItems());
                conn.commit();
                System.out.println("saveOrder: successfully saved order #" + orderId);
                return orderId;
            }
        } catch (SQLException e) {
            System.err.println("❌ Order save failed: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    // ===== FIXED: Throws specific exception with item name =====
    private void updateStockLevels(Connection conn, List<FoodItem> items) throws SQLException {
        String checkSql = "SELECT name, stock FROM food_items WHERE item_id = ?";
        String updateSql = "UPDATE food_items SET stock = stock - 1 WHERE item_id = ? AND stock > 0";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            for (FoodItem item : items) {
                // Double-check stock before updating
                checkStmt.setInt(1, item.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int currentStock = rs.getInt("stock");
                    String itemName = rs.getString("name");
                    if (currentStock <= 0) {
                        throw new SQLException("Out of stock: " + itemName);
                    }
                }

                updateStmt.setInt(1, item.getId());
                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Failed to update stock for item ID: " + item.getId());
                }
            }
        }
    }

    public int getUserId(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}