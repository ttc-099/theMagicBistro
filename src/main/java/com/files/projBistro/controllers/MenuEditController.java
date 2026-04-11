package com.files.projBistro.controllers;

import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.dao.AdminDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class MenuEditController {
    @FXML private TableView<FoodItem> inventoryTable;
    @FXML private TableColumn<FoodItem, String> nameCol;
    @FXML private TableColumn<FoodItem, Double> priceCol;
    @FXML private TableColumn<FoodItem, Integer> stockCol;
    @FXML private TableColumn<FoodItem, String> typeCol; // Added this!
    @FXML private TableColumn<FoodItem, String> charCol;

    private AdminDAO adminDAO = new AdminDAO();

    @FXML
    public void initialize() {
        System.out.println("Initializing Menu Management Table...");

        // Map columns to FoodItem getters (Must match getName, getPrice, etc.)
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("itemType")); // Matches getItemType()
        charCol.setCellValueFactory(new PropertyValueFactory<>("charCategory")); // Matches getCharCategory()

        refreshTable();
    }

    public void refreshTable() {
        List<FoodItem> items = adminDAO.getAllInventory();

        if (items != null && !items.isEmpty()) {
            System.out.println("✅ Data Found: " + items.size() + " items loaded.");
            inventoryTable.getItems().setAll(items);
        } else {
            System.out.println("⚠️ Data Failure: AdminDAO returned an empty or null list.");
        }
    }
}