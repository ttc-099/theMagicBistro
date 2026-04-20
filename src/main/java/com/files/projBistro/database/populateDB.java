package com.files.projBistro.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class populateDB {

    private static final String URL = "jdbc:sqlite:bistroTrue.db";

    public void seedData() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            // Check if data already exists
            boolean hasData = checkIfDataExists(conn);
            if (hasData) {
                System.out.println("✅ Database already populated. Skipping seed.");
                return;
            }

            // 1. Seed USERS
            String userSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {
                pstmt.setString(1, "admin");
                pstmt.setString(2, "secure123");
                pstmt.setString(3, "Admin");
                pstmt.executeUpdate();

                pstmt.setString(1, "chloe_fan");
                pstmt.setString(2, "pass1");
                pstmt.setString(3, "Customer");
                pstmt.executeUpdate();

                pstmt.setString(1, "mimi_fan");
                pstmt.setString(2, "pass2");
                pstmt.setString(3, "Customer");
                pstmt.executeUpdate();
            }

            // 2. Seed CHARACTERS
            String charSql = "INSERT INTO characters (name, image_path, theme_color) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(charSql)) {
                pstmt.setString(1, "Chloe");
                pstmt.setString(2, "/images/chloe.png");
                pstmt.setString(3, "#FFD700");
                pstmt.executeUpdate();

                pstmt.setString(1, "Mimi");
                pstmt.setString(2, "/images/mimi.png");
                pstmt.setString(3, "#FF69B4");
                pstmt.executeUpdate();

                pstmt.setString(1, "Metsu");
                pstmt.setString(2, "/images/metsu.png");
                pstmt.setString(3, "#4B0082");
                pstmt.executeUpdate();

                pstmt.setString(1, "Laniard");
                pstmt.setString(2, "/images/laniard.png");
                pstmt.setString(3, "#808080");
                pstmt.executeUpdate();
            }

            // 3. Seed DIALOGUE
            String diagSql = "INSERT INTO character_dialogue (character_id, trigger_type, dialogue_text) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(diagSql)) {
                pstmt.setInt(1, 1);  // Chloe
                pstmt.setString(2, "ITEM_SELECTED");
                pstmt.setString(3, "Excellent choice! This will maximize your performance.");
                pstmt.executeUpdate();

                pstmt.setInt(1, 2);  // Mimi
                pstmt.setString(2, "ORDER_COMPLETE");
                pstmt.setString(3, "Enjoy your treats! Stay sweet!");
                pstmt.executeUpdate();

                pstmt.setInt(1, 3);  // Metsu
                pstmt.setString(2, "LOGIN_GREET");
                pstmt.setString(3, "Target acquired. Welcome back to the Bistro.");
                pstmt.executeUpdate();
            }

            // 4. Seed FOOD ITEMS
            String foodSql = "INSERT INTO food_items (name, price, stock, item_type, character_id, is_active) VALUES (?, ?, ?, ?, ?, 1)";
            try (PreparedStatement pstmt = conn.prepareStatement(foodSql)) {
                pstmt.setString(1, "Nitro Nachos");
                pstmt.setDouble(2, 8.50);
                pstmt.setInt(3, 50);
                pstmt.setString(4, "Main");
                pstmt.setInt(5, 1);  // Chloe
                pstmt.executeUpdate();

                pstmt.setString(1, "Cyber Cake");
                pstmt.setDouble(2, 5.00);
                pstmt.setInt(3, 20);
                pstmt.setString(4, "Dessert");
                pstmt.setInt(5, 2);  // Mimi
                pstmt.executeUpdate();

                pstmt.setString(1, "Metsu Mint");
                pstmt.setDouble(2, 3.50);
                pstmt.setInt(3, 100);
                pstmt.setString(4, "Drink");
                pstmt.setInt(5, 3);  // Metsu
                pstmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Database seeded successfully!");

        } catch (SQLException e) {
            System.out.println("❌ Error seeding database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean checkIfDataExists(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             var rs = pstmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}