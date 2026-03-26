package com.files.projBistro.models.DAOs;

import com.files.projBistro.models.FoodItem;
import com.files.projBistro.database.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {

    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> items = new ArrayList<>();
        String query = "SELECT * FROM FoodItems";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                // Using your Builder Pattern!
                FoodItem item = new FoodItem.FoodItemBuilder()
                        .setId(rs.getInt("item_id"))
                        .setName(rs.getString("name"))
                        .setPrice(rs.getDouble("price"))
                        .setStock(rs.getInt("stock"))
                        .setCharCategory(rs.getString("char_category"))
                        .setItemType(rs.getString("item_type"))
                        .setImagePath(rs.getString("image_path"))
                        .build();

                items.add(item);
            } // End of while

        } catch (SQLException e) {
            e.printStackTrace();
        } // End of try-catch

        return items;
    } // End of method
} // End of class