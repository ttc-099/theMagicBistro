package com.files.projBistro.models.dao;

import com.files.projBistro.database.DatabaseConnection;
import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.Order;
import java.sql.*;
import java.util.List;
import java.util.Random;

/**
 * Utility class to force/automate sales entries for testing and demonstration.
 */
public class AddSales {

    private final OrderDAO orderDAO = new OrderDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final Random random = new Random();

    /**
     * Forces a specified number of random sales to be added to the database.
     * @param count Number of random orders to generate
     */
    public void forceRandomSales(int count) {
        List<FoodItem> availableItems = menuDAO.getAllActiveItems();
        if (availableItems.isEmpty()) {
            System.out.println("⚠️ No active food items found. Cannot add sales.");
            return;
        }

        for (int i = 0; i < count; i++) {
            // Find a random user to attribute the sale to
            int userId = getRandomUserId();
            if (userId == -1) userId = 1;

            Order randomOrder = new Order("Customer_" + (1000 + random.nextInt(9000)));
            
            // Add 1 to 5 random items to the order
            int itemsInOrder = random.nextInt(5) + 1;
            for (int j = 0; j < itemsInOrder; j++) {
                FoodItem randomItem = availableItems.get(random.nextInt(availableItems.size()));
                randomOrder.addItem(randomItem);
            }

            // Manually insert order to skip stock reduction and force 'completed' status
            forceInsertCompletedOrder(randomOrder, userId);
        }
    }

    /**
     * Forces a sale into the database with completed status and random items.
     * Useful for filling up the Sales Summary with data.
     */
    public boolean forceInsertCompletedOrder(Order order, int userId) {
        String orderSql = "INSERT INTO orders (user_id, total_price, status, order_date) " +
                          "VALUES (?, ?, 'completed', CURRENT_TIMESTAMP)";
        String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, price_at_purchase) " +
                         "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, userId);
                pstmt.setDouble(2, order.getTotalPrice());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int orderId = rs.next() ? rs.getInt(1) : -1;

                if (orderId == -1) {
                    conn.rollback();
                    return false;
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

                conn.commit();
                System.out.println("✅ Forced Completed Order #" + orderId + " into database.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to force sales entry: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private int getRandomUserId() {
        String sql = "SELECT user_id FROM users ORDER BY RANDOM() LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
