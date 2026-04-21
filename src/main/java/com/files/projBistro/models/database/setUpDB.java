package com.files.projBistro.models.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class setUpDB {

    // 1. Connection string for our definitive tactical database
    private static final String URL = "jdbc:sqlite:bistroTrue.db";

    public void initializeDatabase() {
        String[] queries = {
                // 2. CORE USER & SECURITY TABLES
                // handles login and tactical admin overrides
                "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT UNIQUE, " +
                        "password TEXT, " +
                        "role TEXT, " + // 'Admin' or 'Customer'
                        "admin_pin TEXT, " +
                        "phone_number TEXT);",

                // 3. CHARACTER CATEGORIES
                // the "Owners" of the menus (Chloe, Mimi, etc.)
                "CREATE TABLE IF NOT EXISTS characters (" +
                        "character_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "image_path TEXT, " +
                        "theme_color TEXT);",

                // 4. REACTIVE FLAVOR TEXT
                // stores the dialogue triggers for the UI
                "CREATE TABLE IF NOT EXISTS character_dialogue (" +
                        "dialogue_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "character_id INTEGER, " +
                        "trigger_type TEXT, " + // e.g., 'ITEM_SELECTED'
                        "dialogue_text TEXT, " +
                        "FOREIGN KEY(character_id) REFERENCES characters(character_id));",

                // 5. MENU & INVENTORY (Master Data)
                // the items available for purchase with stock tracking
                "CREATE TABLE IF NOT EXISTS food_items (" +
                        "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "price REAL, " +
                        "stock INTEGER, " +
                        "item_type TEXT, " +
                        "character_id INTEGER, " +
                        "image_path TEXT, " +
                        "is_active INTEGER DEFAULT 1, " + // Soft delete logic
                        "FOREIGN KEY(character_id) REFERENCES characters(character_id));",

                // 6. TRANSACTION LOGS (Mission Logs)
                // tracks completed orders for reporting
                "CREATE TABLE IF NOT EXISTS orders (" +
                        "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "total_price REAL, " +
                        "status TEXT DEFAULT 'pending', " +
                        "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id));",

                // 7. ORDER DETAILS (Junction Table)
                // specific items in each order (preserves price at time of purchase)
                "CREATE TABLE IF NOT EXISTS order_items (" +
                        "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "order_id INTEGER, " +
                        "item_id INTEGER, " +
                        "quantity INTEGER, " +
                        "price_at_purchase REAL, " +
                        "FOREIGN KEY(order_id) REFERENCES orders(order_id), " +
                        "FOREIGN KEY(item_id) REFERENCES food_items(item_id));"
        };

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Execute the deployment sequence
            for (String sql : queries) {
                stmt.execute(sql);
            }

            System.out.println("All 6 Tactical Tables synchronized with ERD.");

        } catch (SQLException e) {
            System.out.println("❌ Critical Error: Database deployment failed.");
            e.printStackTrace();
        }
    }
}