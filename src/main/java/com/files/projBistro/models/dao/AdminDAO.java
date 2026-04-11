package com.files.projBistro.models.dao;

import com.files.projBistro.models.FoodItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// sql implementation
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AdminDAO {
    private final String URL = "jdbc:sqlite:bistroTrue.db";

    // 1. add item (already built)
    public boolean addItem(FoodItem item, int charId) {
        String sql = "INSERT INTO food_items(name, price, stock, item_type, character_id, is_active) VALUES(?,?,?,?,?,1)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getStock());
            pstmt.setString(4, item.getItemType());
            pstmt.setInt(5, charId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 2. edit item (new: updates existing tactical data)
    public boolean updateItem(FoodItem item, int characterId) {
        String sql = "UPDATE food_items SET name=?, price=?, stock=?, item_type=?, character_id=? WHERE item_id=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getStock());
            pstmt.setString(4, item.getItemType());
            pstmt.setInt(5, characterId);
            pstmt.setInt(6, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. remove item (soft delete)
    public boolean removeItem(int id) {
        String sql = "UPDATE food_items SET is_active = 0 WHERE item_id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. dialogue management (add new reaction)
    public boolean addDialogue(int charId, String trigger, String text) {
        String sql = "INSERT INTO character_dialogue(character_id, trigger_type, dialogue_text) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, charId);
            pstmt.setString(2, trigger);
            pstmt.setString(3, text);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. fetch all active inventory from the armory
    // we use a JOIN here to grab the Character Name instead of just the ID number
    public List<FoodItem> getAllInventory() {
        List<FoodItem> inventory = new ArrayList<>();

        // tactical join: linking food_items to characters to get the 'Owner' name
        String sql = "SELECT f.*, c.name AS char_name " +
                "FROM food_items f " +
                "JOIN characters c ON f.character_id = c.character_id " +
                "WHERE f.is_active = 1"; // only show items that aren't decommissioned

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // we use the Builder to construct the item from the database row
                FoodItem item = new FoodItem.FoodItemBuilder()
                        .setId(rs.getInt("item_id"))
                        .setName(rs.getString("name"))
                        .setPrice(rs.getDouble("price"))
                        .setStock(rs.getInt("stock"))
                        .setItemType(rs.getString("item_type"))
                        .setCharCategory(rs.getString("char_name")) // this comes from the JOIN
                        .build();

                inventory.add(item);
            }
            System.out.println("✅ Inventory data retrieved: " + inventory.size() + " units.");
        } catch (SQLException e) {
            System.out.println("❌ SQL Error: Could not fetch inventory logs.");
            e.printStackTrace();
        }
        return inventory;
    }
}