package com.files.projBistro.models.dao;

import com.files.projBistro.models.database.DatabaseConnection;
import com.files.projBistro.models.models.FoodItem;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class handles all admin database operations
// it can add, edit, remove, and fetch food items
// also handles dialogue management for admins
public class AdminDAO {

    // get connection from singleton (single source of truth)
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // 1. add a new food item to the database
    // includes image path so pictures can be saved
    public boolean addItem(FoodItem item, int charId) {
        // Added description column (7th parameter)
        String sql = "INSERT INTO food_items(name, price, stock, item_type, character_id, image_path, description, is_active) VALUES(?,?,?,?,?,?,?,1)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getStock());
            pstmt.setString(4, item.getItemType());
            pstmt.setInt(5, charId);
            pstmt.setString(6, item.getImagePath());
            pstmt.setString(7, item.getDescription());  // ← new line
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. update an existing food item
    // includes updating the image path
    public boolean updateItem(FoodItem item, int characterId) {
        // Added description column in UPDATE
        String sql = "UPDATE food_items SET name=?, price=?, stock=?, item_type=?, character_id=?, image_path=?, description=? WHERE item_id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setInt(3, item.getStock());
            pstmt.setString(4, item.getItemType());
            pstmt.setInt(5, characterId);
            pstmt.setString(6, item.getImagePath());
            pstmt.setString(7, item.getDescription());  // ← new line
            pstmt.setInt(8, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. toggle item visibility (hide/show from menu)
    public boolean toggleItemVisibility(int itemId, boolean isActive) {
        String sql = "UPDATE food_items SET is_active = ? WHERE item_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setInt(2, itemId);
            int affected = pstmt.executeUpdate();
            System.out.println("toggleItemVisibility: set item " + itemId + " to active=" + isActive);
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3b. remove an item (soft delete - just hides it from menu)
    public boolean removeItem(int id) {
        return toggleItemVisibility(id, false);
    }

    // 4. add a new dialogue line for a character
    public boolean addDialogue(int charId, String trigger, String text) {
        String sql = "INSERT INTO character_dialogue(character_id, trigger_type, dialogue_text) VALUES(?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, charId);
            pstmt.setString(2, trigger);
            pstmt.setString(3, text);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // update an existing dialogue line
    public boolean updateDialogue(int dialogueId, String trigger, String text) {
        String sql = "UPDATE character_dialogue SET trigger_type=?, dialogue_text=? WHERE dialogue_id=?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trigger);
            pstmt.setString(2, text);
            pstmt.setInt(3, dialogueId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. fetch ALL inventory items (including hidden ones - for admin view)
    public List<FoodItem> getAllInventory() {
        List<FoodItem> inventory = new ArrayList<>();

        String sql = "SELECT f.*, c.name AS char_name " +
                "FROM food_items f " +
                "JOIN characters c ON f.character_id = c.character_id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through each row in the result and create a FoodItem object
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
                        .setIsActive(rs.getInt("is_active") == 1)  // load active status
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

    // 5b. get only active items for a specific character (for menu preview)
    public List<FoodItem> getActiveItemsByCharacter(String characterName) {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT f.*, c.name AS char_name " +
                "FROM food_items f " +
                "JOIN characters c ON f.character_id = c.character_id " +
                "WHERE c.name = ? AND f.is_active = 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, characterName);
            ResultSet rs = pstmt.executeQuery();

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
                        .setIsActive(true)
                        .build();
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // debug method to check where the database file is located
    public void debugDatabaseLocation() {
        try (Connection conn = getConnection()) {
            String url = conn.getMetaData().getURL();
            System.out.println("📁 Database URL: " + url);

            if (url.startsWith("jdbc:sqlite:")) {
                String path = url.substring("jdbc:sqlite:".length());
                File dbFile = new File(path);
                System.out.println("📁 Absolute path: " + dbFile.getAbsolutePath());
                System.out.println("📁 File exists: " + dbFile.exists());
                System.out.println("📁 File size: " + dbFile.length() + " bytes");
            }

            // also count how many active items are in this database
            String sql = "SELECT COUNT(*) FROM food_items WHERE is_active = 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    System.out.println("📁 ACTUAL active items in THIS database: " + rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // delete a dialogue line completely (not soft delete - actually removes it)
    public boolean deleteDialogue(int dialogueId) {
        String sql = "DELETE FROM character_dialogue WHERE dialogue_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dialogueId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // permanently delete an item from the database (hard delete)
    public boolean hardDeleteItem(int id) {
        String sql = "DELETE FROM food_items WHERE item_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            System.out.println("hardDeleteItem: deleted item " + id);
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}