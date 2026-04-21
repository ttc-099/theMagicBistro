package com.files.projBistro.models.dao;

import com.files.projBistro.models.database.DatabaseConnection;
import com.files.projBistro.models.models.FoodItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class handles all menu-related database operations
// it fetches food items for display in the customer menu
public class MenuDAO {

    // helper method to get a database connection
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // get all food items that belong to a specific character
    // example: get all items sold by "Chloe"
    // only returns items that are active (not soft-deleted)
    public List<FoodItem> getItemsByCharacter(String characterName) {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT f.*, c.name AS char_name " +
                "FROM food_items f " +
                "JOIN characters c ON f.character_id = c.character_id " +
                "WHERE c.name = ? AND f.is_active = 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, characterName);   // which character we want
            ResultSet rs = pstmt.executeQuery();

            // convert each database row into a fooditem object
            while (rs.next()) {
                FoodItem item = new FoodItem.FoodItemBuilder()
                        .setId(rs.getInt("item_id"))
                        .setName(rs.getString("name"))
                        .setPrice(rs.getDouble("price"))
                        .setStock(rs.getInt("stock"))
                        .setItemType(rs.getString("item_type"))
                        .setDescription(rs.getString("description"))
                        .setCharCategory(rs.getString("char_name"))
                        .setImagePath(rs.getString("image_path"))
                        .build();
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // get all active food items (regardless of character)
    // this could be used for admin views or special displays
    public List<FoodItem> getAllActiveItems() {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT f.*, c.name AS char_name " +
                "FROM food_items f " +
                "JOIN characters c ON f.character_id = c.character_id " +
                "WHERE f.is_active = 1";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // convert each database row into a fooditem object
            while (rs.next()) {
                FoodItem item = new FoodItem.FoodItemBuilder()
                        .setId(rs.getInt("item_id"))
                        .setName(rs.getString("name"))
                        .setPrice(rs.getDouble("price"))
                        .setStock(rs.getInt("stock"))
                        .setItemType(rs.getString("item_type"))
                        .setDescription(rs.getString("description"))
                        .setCharCategory(rs.getString("char_name"))
                        .setImagePath(rs.getString("image_path"))
                        .build();
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}