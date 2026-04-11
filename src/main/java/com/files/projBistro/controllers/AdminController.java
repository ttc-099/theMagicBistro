package com.files.projBistro.controllers;

import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.dao.AdminDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Optional;

public class AdminController {

    // 1. inventory tab components
    @FXML private TableView<FoodItem> inventoryTable;
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String> colName;
    @FXML private TableColumn<FoodItem, Double> colPrice;
    @FXML private TableColumn<FoodItem, Integer> colStock;
    @FXML private TableColumn<FoodItem, String> colCategory;

    @FXML private TextField nameInput;
    @FXML private TextField priceInput;
    @FXML private TextField stockInput;
    @FXML private ComboBox<String> categoryBox;

    // 2. dialogue tab components
    @FXML private ComboBox<String> dialogueCharBox;
    @FXML private TableView<String[]> dialogueTable; // Simplified for display
    @FXML private TextArea dialogueInputArea;
    @FXML private ComboBox<String> triggerInputBox;

    // 3. tactical workers
    private AdminDAO adminDAO = new AdminDAO();
    private final String MASTER_PIN = "1234";

    @FXML
    public void initialize() {
        // setup inventory table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("charCategory"));

        // sync choice boxes
        ObservableList<String> chars = FXCollections.observableArrayList("Chloe", "Mimi", "Metsu", "Laniard");
        categoryBox.setItems(chars);
        dialogueCharBox.setItems(chars);
        triggerInputBox.setItems(FXCollections.observableArrayList("ITEM_SELECTED", "ORDER_COMPLETE", "LOGIN_GREET"));

        // 4. the "Edit" listener: auto-fill inputs when a row is clicked
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameInput.setText(newSelection.getName());
                priceInput.setText(String.valueOf(newSelection.getPrice()));
                stockInput.setText(String.valueOf(newSelection.getStock()));
            }
        });

        refreshTable();
    }

    // 5. inventory logic (Add / Edit / Remove)
    @FXML
    private void handleAddItem() {
        if (!isAuthorized()) return;
        try {
            FoodItem item = buildItemFromInput();
            int charId = categoryBox.getSelectionModel().getSelectedIndex() + 1; // Maps 0-index to DB 1-index
            if (adminDAO.addItem(item, charId)) {
                refreshTable();
                clearInputs();
            }
        } catch (Exception e) { System.out.println("Tactical Error: Invalid Input"); }
    }

    @FXML
    private void handleEditItem() {
        // 1. see which unit is currently selected in the table
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected != null && isAuthorized()) {
            try {
                // 2. create a NEW FoodItem object using the Builder
                // we keep the 'id' from the selected one, but grab 'name/price/stock' from the inputs
                FoodItem updatedItem = new FoodItem.FoodItemBuilder()
                        .setId(selected.getId())
                        .setName(nameInput.getText())
                        .setPrice(Double.parseDouble(priceInput.getText()))
                        .setStock(Integer.parseInt(stockInput.getText()))
                        .setCharCategory(categoryBox.getValue())
                        .setItemType(selected.getItemType()) // keep the original type
                        .build();

                // 3. push the update to the database
                if (adminDAO.updateItem(updatedItem)) {
                    refreshTable();
                    clearInputs();
                    System.out.println("✅ Item " + selected.getId() + " updated in the armory.");
                }
            } catch (Exception e) {
                System.out.println("❌ Update failed: Check your price/stock values.");
            }
        } else if (selected == null) {
            System.out.println("⚠️ Select an item from the table first to edit it.");
        }
    }

    @FXML
    private void handleRemoveItem() {
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null && isAuthorized()) {
            if (adminDAO.removeItem(selected.getId())) {
                refreshTable();
            }
        }
    }

    // 6. dialogue logic
    @FXML
    private void handleAddDialogue() {
        int charId = dialogueCharBox.getSelectionModel().getSelectedIndex() + 1;
        String trigger = triggerInputBox.getValue();
        String text = dialogueInputArea.getText();

        if (adminDAO.addDialogue(charId, trigger, text)) {
            dialogueInputArea.clear();
            // loadDialogueTable(); // Refresh the dialogue list
        }
    }

    // 7. security & helpers
    private boolean isAuthorized() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Mission Critical Action: Enter PIN");
        Optional<String> result = dialog.showAndWait();
        return result.isPresent() && result.get().equals(MASTER_PIN);
    }

    private void refreshTable() {
        inventoryTable.setItems(FXCollections.observableArrayList(adminDAO.getAllInventory()));
    }

    private FoodItem buildItemFromInput() {
        return new FoodItem.FoodItemBuilder()
                .setName(nameInput.getText())
                .setPrice(Double.parseDouble(priceInput.getText()))
                .setStock(Integer.parseInt(stockInput.getText()))
                .setItemType("Main") // Defaulting for now
                .build();
    }

    private void clearInputs() {
        nameInput.clear(); priceInput.clear(); stockInput.clear();
        categoryBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack() {
        // Logic to switch back to MenuView.fxml
    }
}