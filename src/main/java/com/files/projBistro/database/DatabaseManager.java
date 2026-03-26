package com.files.projBistro.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // The link to your file
    private static final String URL = "jdbc:sqlite:bistro.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        // SQL code to create your 29-item menu table
        String sql = "CREATE TABLE IF NOT EXISTS FoodItems (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "character_id INTEGER," +
                "image_path TEXT);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Bistro Database Ready!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
