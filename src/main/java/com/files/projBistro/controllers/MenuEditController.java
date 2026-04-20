package com.files.projBistro.controllers;

import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.dao.AdminDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// this controller lets admin edit the full menu (inventory)
// it is similar to admincontroller but with filtering by character
public class MenuEditController {

    // ===== screen elements =====
    @FXML private TableView<FoodItem> inventoryTable;           // table showing all food items
    @FXML private TableColumn<FoodItem, Integer> colId;         // item id column
    @FXML private TableColumn<FoodItem, String> colName;        // item name column
    @FXML private TableColumn<FoodItem, Double> colPrice;       // price column
    @FXML private TableColumn<FoodItem, Integer> colStock;      // stock column
    @FXML private TableColumn<FoodItem, String> colType;        // item type column (main, dessert, etc)
    @FXML private TableColumn<FoodItem, String> colCharacter;   // character category column

    @FXML private Label statusLabel;        // shows temporary messages
    @FXML private Label itemCountLabel;     // shows number of items in filtered table
    @FXML private ImageView imagePreview;   // shows preview of selected image

    // form inputs for adding/editing items
    @FXML private TextField nameInput;
    @FXML private TextField priceInput;
    @FXML private TextField stockInput;
    @FXML private ComboBox<String> typeBox;
    @FXML private ComboBox<String> characterBox;
    @FXML private TextArea descriptionInput;   // description of the item (not used everywhere)

    // filter buttons at the top
    @FXML private ToggleButton chloeFilter;
    @FXML private ToggleButton mimiFilter;
    @FXML private ToggleButton metsuFilter;
    @FXML private ToggleButton laniardFilter;
    @FXML private ToggleButton allFilter;

    // ===== data =====
    private AdminDAO adminDAO = new AdminDAO();                    // database helper
    private ObservableList<FoodItem> allItems = FXCollections.observableArrayList(); // all items from db
    private String currentFilter = "ALL";                          // which character we are filtering by
    private String selectedImagePath = null;                       // path of chosen image
    private final String MASTER_PIN = "1234";                      // admin pin

    // ===== setup - runs when screen loads =====
    @FXML
    public void initialize() {
        // fill the type dropdown with options
        typeBox.setItems(FXCollections.observableArrayList(
                "Main", "Appetizer", "Dessert", "Drink", "Special"
        ));
        // fill the character dropdown
        characterBox.setItems(FXCollections.observableArrayList(
                "Chloe", "Mimi", "Metsu", "Laniard"
        ));

        setupTableColumns();           // connect columns to data
        loadAllItems();               // get all items from database
        setupTableSelectionListener(); // when row clicked, fill the form
        setupToggleGroup();           // make filter buttons work as a group

        // style the dropdown menus so text is readable
        typeBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #2b2b2b;");
            }
        });
        typeBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        characterBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #2b2b2b;");
            }
        });
        characterBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        showTemporaryStatus("✅ Ready. Select a character to filter items.", "green");
    }

    // tell each table column which property of fooditem to show
    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        colPrice.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        colStock.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getItemType()));
        colCharacter.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCharCategory()));
    }

    // group the filter buttons so only one can be selected at a time
    private void setupToggleGroup() {
        ToggleGroup filterGroup = new ToggleGroup();
        chloeFilter.setToggleGroup(filterGroup);
        mimiFilter.setToggleGroup(filterGroup);
        metsuFilter.setToggleGroup(filterGroup);
        laniardFilter.setToggleGroup(filterGroup);
        allFilter.setToggleGroup(filterGroup);
        allFilter.setSelected(true);   // start with "all" selected
    }

    // when user clicks a row in the table, fill the form with that item's data
    private void setupTableSelectionListener() {
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
    }

    // ===== filter methods =====
    @FXML private void filterByChloe() { applyFilter("Chloe"); }
    @FXML private void filterByMimi() { applyFilter("Mimi"); }
    @FXML private void filterByMetsu() { applyFilter("Metsu"); }
    @FXML private void filterByLaniard() { applyFilter("Laniard"); }
    @FXML private void filterAll() { applyFilter("ALL"); }


    // Add this helper method to MenuEditController
    private String copyImageToProject(String sourcePath) throws IOException {
        File sourceFile = new File(sourcePath);
        String fileName = sourceFile.getName();
        // Get the project's resources/images folder
        String projectRoot = System.getProperty("user.dir");
        String targetDir = projectRoot + "/src/main/resources/images/";
        File targetDirFile = new File(targetDir);
        if (!targetDirFile.exists()) {
            targetDirFile.mkdirs();  // create folder if missing
        }
        String targetPath = targetDir + fileName;
        // Copy the file (overwrites if exists)
        Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        // Return the path relative to resources (for loading later)
        return "/images/" + fileName;
    }

    // filter the table to show only items for a specific character
    private void applyFilter(String character) {
        currentFilter = character;
        if ("ALL".equals(character)) {
            inventoryTable.setItems(allItems);   // show everything
            showTemporaryStatus("📋 Showing all " + allItems.size() + " items", "blue");
        } else {
            // filter the list to only matching character
            List<FoodItem> filtered = allItems.stream()
                    .filter(item -> character.equals(item.getCharCategory()))
                    .collect(Collectors.toList());
            inventoryTable.setItems(FXCollections.observableArrayList(filtered));
            showTemporaryStatus("👤 Showing " + filtered.size() + " items for " + character, "blue");
        }
        updateItemCount();   // update the count label
    }

    // ===== add, edit, remove items =====

    // add a new food item
    @FXML
    private void handleAddItem() {
        if (!isAuthorized()) return;       // ask for pin
        if (!validateInputs()) return;     // check form is filled

        try {
            int charId = getCharacterId(characterBox.getValue());
            FoodItem item = buildItemFromInput();

            if (adminDAO.addItem(item, charId)) {
                loadAllItems();            // reload from database
                applyFilter(currentFilter); // reapply filter
                clearForm();               // clear the form
                showTemporaryStatus("✅ Item added successfully!", "green");
            } else {
                showTemporaryStatus("❌ Failed to add item", "red");
            }
        } catch (NumberFormatException e) {
            showTemporaryStatus("❌ Invalid price or stock value", "red");
        }
    }

    // edit the selected item
    @FXML
    private void handleEditItem() {
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryStatus("⚠️ Select an item first to edit", "orange");
            return;
        }

        if (!isAuthorized()) return;
        if (!validateInputs()) return;

        try {
            int charId = getCharacterId(characterBox.getValue());

            // build updated item with data from form
            FoodItem updatedItem = new FoodItem.FoodItemBuilder()
                    .setId(selected.getId())
                    .setName(nameInput.getText())
                    .setPrice(Double.parseDouble(priceInput.getText()))
                    .setStock(Integer.parseInt(stockInput.getText()))
                    .setCharCategory(characterBox.getValue())
                    .setItemType(typeBox.getValue())
                    .setImagePath(selectedImagePath != null ? selectedImagePath : selected.getImagePath())
                    .setDescription(descriptionInput.getText())
                    .build();

            if (adminDAO.updateItem(updatedItem, charId)) {
                loadAllItems();
                applyFilter(currentFilter);
                clearForm();
                showTemporaryStatus("✅ Item updated successfully!", "green");
            } else {
                showTemporaryStatus("❌ Failed to update item", "red");
            }
        } catch (NumberFormatException e) {
            showTemporaryStatus("❌ Invalid price or stock value", "red");
        }
    }

    // remove the selected item (soft delete)
    @FXML
    private void handleRemoveItem() {
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryStatus("⚠️ Select an item first to remove", "orange");
            return;
        }

        if (!isAuthorized()) return;

        // ask for confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Remove " + selected.getName() + "?");
        confirm.setContentText("This item will be soft-deleted (hidden from menu).");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (adminDAO.removeItem(selected.getId())) {
                loadAllItems();
                applyFilter(currentFilter);
                clearForm();
                showTemporaryStatus("🗑️ Item removed successfully", "green");
            } else {
                showTemporaryStatus("❌ Failed to remove item", "red");
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Food Item Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            try {
                // Copy the file and store the relative path
                selectedImagePath = copyImageToProject(selectedFile.getAbsolutePath());
                // Show preview
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
                showTemporaryStatus("📷 Image copied to project: " + selectedFile.getName(), "blue");
            } catch (IOException e) {
                showTemporaryStatus("❌ Failed to copy image: " + e.getMessage(), "red");
            }
        }
    }


    // ===== helper methods =====

    // load all items from database into memory
    private void loadAllItems() {
        allItems.clear();
        allItems.addAll(adminDAO.getAllInventory());
        updateItemCount();
    }

    // update the label that shows how many items are in the filtered table
    private void updateItemCount() {
        itemCountLabel.setText(String.valueOf(inventoryTable.getItems().size()));
    }

    // fill the form with data from a selected item (for editing)
    private void populateForm(FoodItem item) {
        nameInput.setText(item.getName());
        priceInput.setText(String.valueOf(item.getPrice()));
        stockInput.setText(String.valueOf(item.getStock()));
        typeBox.setValue(item.getItemType());
        characterBox.setValue(item.getCharCategory());
        selectedImagePath = item.getImagePath();

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            try {
                String imageUrl = getClass().getResource(selectedImagePath).toExternalForm();
                imagePreview.setImage(new Image(imageUrl));
            } catch (Exception e) {
                imagePreview.setImage(null);
            }
        } else {
            imagePreview.setImage(null);
        }
    }

    // create a new fooditem object from the form data
    private FoodItem buildItemFromInput() {
        return new FoodItem.FoodItemBuilder()
                .setName(nameInput.getText())
                .setPrice(Double.parseDouble(priceInput.getText()))
                .setStock(Integer.parseInt(stockInput.getText()))
                .setCharCategory(characterBox.getValue())
                .setItemType(typeBox.getValue())
                .setImagePath(selectedImagePath)
                .build();
    }

    // convert character name to database id (1 to 4)
    private int getCharacterId(String characterName) {
        switch (characterName) {
            case "Chloe": return 1;
            case "Mimi": return 2;
            case "Metsu": return 3;
            case "Laniard": return 4;
            default: return 1;
        }
    }

    // check that all required form fields are filled
    private boolean validateInputs() {
        if (nameInput.getText().trim().isEmpty()) {
            showTemporaryStatus("⚠️ Please enter an item name", "orange");
            return false;
        }
        if (priceInput.getText().trim().isEmpty()) {
            showTemporaryStatus("⚠️ Please enter a price", "orange");
            return false;
        }
        if (stockInput.getText().trim().isEmpty()) {
            showTemporaryStatus("⚠️ Please enter stock quantity", "orange");
            return false;
        }
        if (characterBox.getValue() == null) {
            showTemporaryStatus("⚠️ Please select a character", "orange");
            return false;
        }
        if (typeBox.getValue() == null) {
            typeBox.setValue("Main");   // default if not set
        }
        return true;
    }

    // ask for admin pin before sensitive actions
    private boolean isAuthorized() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Security Clearance");
        dialog.setHeaderText("Mission Critical Action: Enter PIN");
        dialog.setContentText("Enter 4-digit PIN:");
        Optional<String> result = dialog.showAndWait();
        return result.isPresent() && result.get().equals(MASTER_PIN);
    }

    // clear all form fields
    @FXML
    private void clearForm() {
        nameInput.clear();
        priceInput.clear();
        stockInput.clear();
        typeBox.getSelectionModel().clearSelection();
        characterBox.getSelectionModel().clearSelection();
        descriptionInput.clear();
        selectedImagePath = null;
        imagePreview.setImage(null);
        inventoryTable.getSelectionModel().clearSelection();
        showTemporaryStatus("Form cleared", "gray");
    }

    // manually refresh the table
    @FXML
    public void refreshTable() {
        loadAllItems();
        applyFilter(currentFilter);
        showTemporaryStatus("🔄 Table refreshed", "blue");
    }

    // show a message that disappears after 3 seconds
    private void showTemporaryStatus(String message, String type) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning", "status-info", "status-muted");

        String styleClass = "status-muted";
        if (type.equalsIgnoreCase("green")) styleClass = "status-success";
        else if (type.equalsIgnoreCase("red")) styleClass = "status-error";
        else if (type.equalsIgnoreCase("orange")) styleClass = "status-warning";
        else if (type.equalsIgnoreCase("blue")) styleClass = "status-info";

        statusLabel.getStyleClass().add(styleClass);

        // run in background so it doesn't freeze the screen
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    if (statusLabel.getText().equals(message)) {
                        statusLabel.setText("Ready. Select an item to edit.");
                        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning", "status-info");
                        statusLabel.getStyleClass().add("status-muted");
                    }
                });
            } catch (InterruptedException e) {}
        }).start();
    }

    // go back to the admin dashboard
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashView.fxml"));
            Scene scene = new Scene(loader.load(), 1024, 700);
            Stage stage = (Stage) nameInput.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Camo-Gear Bistro | Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showTemporaryStatus("❌ Error returning to dashboard", "red");
        }
    }
}