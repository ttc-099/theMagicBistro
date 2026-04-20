package com.files.projBistro.controllers;

import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.dao.AdminDAO;
import com.files.projBistro.models.dao.DialogueDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

// this controller shows items with low stock and lets admin restock them
// it is used by the admin dashboard's low stock alerts panel
public class LowStockController {

    // ===== screen elements =====
    @FXML private TableView<FoodItem> lowStockTable;           // table showing only low stock items
    @FXML private TableColumn<FoodItem, Integer> colId;        // column for item id
    @FXML private TableColumn<FoodItem, String> colName;       // column for item name
    @FXML private TableColumn<FoodItem, Integer> colCurrentStock; // column for current stock count
    @FXML private TableColumn<FoodItem, String> colCharacter;  // column for character name
    @FXML private TableColumn<FoodItem, String> colType;       // column for item type (main, dessert, etc)
    @FXML private TableColumn<FoodItem, Void> colAction;       // column with restock buttons
    @FXML private Label statusLabel;                           // shows messages like "all stock healthy"

    private AdminDAO adminDAO = new AdminDAO();   // helper to talk to database
    private DialogueDAO dialogueDAO = new DialogueDAO(); // for character dialogue
    private Random random = new Random();         // for random character selection
    private final int LOW_STOCK_THRESHOLD = 20;   // items with stock <= 20 are considered low

    // runs when the low stock screen first loads
    @FXML
    public void initialize() {
        setupTableColumns();   // tell each column which data to show
        refreshStock();        // load low stock items from database
    }

    // connect each table column to the right property of fooditem
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCurrentStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCharacter.setCellValueFactory(new PropertyValueFactory<>("charCategory"));
        colType.setCellValueFactory(new PropertyValueFactory<>("itemType"));

        addActionButtons();   // add the restock buttons to the action column
    }

    // add a "restock" button to each row in the table
    private void addActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            // create a button for restocking
            private final Button restockBtn = new Button("Restock");

            {
                restockBtn.getStyleClass().add("login-button");   // make it look nice
                restockBtn.setOnAction(event -> {
                    // when button is clicked, get the item for this row
                    FoodItem item = getTableView().getItems().get(getIndex());
                    handleRestockItem(item);   // open dialog to restock
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);          // empty row = no button
                } else {
                    setGraphic(restockBtn);    // show the button
                }
            }
        });
    }

    // reload the list of low stock items from the database
    @FXML
    public void refreshStock() {
        // get all items from database
        List<FoodItem> allItems = adminDAO.getAllInventory();

        // filter to only keep items with stock <= 20
        List<FoodItem> lowStockItems = allItems.stream()
                .filter(item -> item.getStock() <= LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList());

        // put the filtered list into the table
        lowStockTable.setItems(FXCollections.observableArrayList(lowStockItems));

        // update the status label based on whether there are low stock items
        if (lowStockItems.isEmpty()) {
            statusLabel.setText("All stock levels are healthy!");
            statusLabel.getStyleClass().setAll("label", "status-success");
        } else {
            statusLabel.setText( lowStockItems.size() + " items below threshold (" + LOW_STOCK_THRESHOLD + ")");
            statusLabel.getStyleClass().setAll("label", "status-warning");

            // ===== TRIGGER LOW STOCK DIALOGUE =====
            // pick a random character (1-4) to speak
            int randomCharId = random.nextInt(4) + 1;
            String lowStockMessage = dialogueDAO.getRandomDialogue(randomCharId, "LOW_STOCK");

            // show the dialogue as a popup alert
            Alert lowStockAlert = new Alert(Alert.AlertType.WARNING);
            lowStockAlert.setTitle("Low stock alert!");
            lowStockAlert.setHeaderText("Inventory is low for some items. Resolve as soon as possible.");
            lowStockAlert.setContentText(lowStockMessage + "\n\n" + lowStockItems.size() + " items need restocking.");
            lowStockAlert.showAndWait();
        }
    }

    // restock the currently selected item (from button below table)
    @FXML
    private void handleRestock() {
        FoodItem selected = lowStockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an item to restock.");
            return;
        }
        handleRestockItem(selected);
    }

    // open a dialog to ask for new stock quantity and update the item
    private void handleRestockItem(FoodItem item) {
        // popup dialog with default value 50
        TextInputDialog dialog = new TextInputDialog("50");
        dialog.setTitle("Restock Item");
        dialog.setHeaderText("Restock: " + item.getName());
        dialog.setContentText("Enter new stock quantity:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newStock = Integer.parseInt(result.get());   // convert text to number

                // create an updated fooditem with the new stock amount
                FoodItem updatedItem = new FoodItem.FoodItemBuilder()
                        .setId(item.getId())
                        .setName(item.getName())
                        .setPrice(item.getPrice())
                        .setStock(newStock)
                        .setCharCategory(item.getCharCategory())
                        .setItemType(item.getItemType())
                        .setImagePath(item.getImagePath())
                        .build();

                // get the character id (1 to 4) from the character name
                int charId = getCharacterId(item.getCharCategory());

                // save to database
                if (adminDAO.updateItem(updatedItem, charId)) {
                    refreshStock();   // reload the table
                    showAlert("Success", item.getName() + " restocked to " + newStock + " units!");
                } else {
                    showAlert("Error", "Failed to restock item.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        }
    }

    // convert character name to a number id (used by database)
    private int getCharacterId(String characterName) {
        switch (characterName) {
            case "Chloe": return 1;
            case "Mimi": return 2;
            case "Metsu": return 3;
            case "Laniard": return 4;
            default: return 1;   // default to chloe if unknown
        }
    }

    // show a simple popup message
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}