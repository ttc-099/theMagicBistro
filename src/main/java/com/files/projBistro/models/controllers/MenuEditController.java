package com.files.projBistro.models.controllers;

import com.files.projBistro.models.models.FoodItem;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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

public class MenuEditController {

    // ===== screen elements =====
    @FXML private TableView<FoodItem> inventoryTable;
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String> colName;
    @FXML private TableColumn<FoodItem, Double> colPrice;
    @FXML private TableColumn<FoodItem, Integer> colStock;
    @FXML private TableColumn<FoodItem, String> colType;
    @FXML private TableColumn<FoodItem, String> colCharacter;

    @FXML private Label statusLabel;
    @FXML private Label itemCountLabel;
    @FXML private ImageView imagePreview;
    @FXML private StackPane imageContainer;

    // Editor controls - add these fx:id declarations
    @FXML private Button chooseImageBtn;
    @FXML private Button addBtn;
    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;
    @FXML private Label selectionHint;

    @FXML private TextField nameInput;
    @FXML private TextField priceInput;
    @FXML private TextField stockInput;
    @FXML private ComboBox<String> typeBox;
    @FXML private ComboBox<String> characterBox;
    @FXML private TextArea descriptionInput;

    @FXML private ToggleButton chloeFilter;
    @FXML private ToggleButton mimiFilter;
    @FXML private ToggleButton metsuFilter;
    @FXML private ToggleButton laniardFilter;
    @FXML private ToggleButton allFilter;

    @FXML private StackPane editorStackPane;
    @FXML private Rectangle editorOverlay;

    // ===== data =====
    private AdminDAO adminDAO = new AdminDAO();
    private ObservableList<FoodItem> allItems = FXCollections.observableArrayList();
    private String currentFilter = "ALL";
    private String selectedImagePath = null;
    private final String MASTER_PIN = "1234";

    @FXML
    public void initialize() {
        typeBox.setItems(FXCollections.observableArrayList(
                "Main", "Appetizer", "Dessert", "Drink", "Special"
        ));
        characterBox.setItems(FXCollections.observableArrayList(
                "Chloe", "Mimi", "Metsu", "Laniard"
        ));

        setupTableColumns();
        loadAllItems();
        setupTableSelectionListener();
        setupToggleGroup();

        disableFormFields(true);
    }

    private void disableFormFields(boolean disable) {
        nameInput.setDisable(disable);
        priceInput.setDisable(disable);
        stockInput.setDisable(disable);
        typeBox.setDisable(disable);
        characterBox.setDisable(disable);
        descriptionInput.setDisable(disable);
        // Leave image button and clear button enabled
    }

    private void enableFormFields() {
        disableFormFields(false);
    }

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

    private void setupToggleGroup() {
        ToggleGroup filterGroup = new ToggleGroup();
        chloeFilter.setToggleGroup(filterGroup);
        mimiFilter.setToggleGroup(filterGroup);
        metsuFilter.setToggleGroup(filterGroup);
        laniardFilter.setToggleGroup(filterGroup);
        allFilter.setToggleGroup(filterGroup);
        allFilter.setSelected(true);
    }

    private void setupTableSelectionListener() {
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
                enableFormFields();  // Enable when selected
            } else {
                clearForm();
                disableFormFields(true);  // Disable when no selection
            }
        });
    }

//    private void setEditorEnabled(boolean enabled) {
//        // Disable all input fields
//        nameInput.setDisable(!enabled);
//        priceInput.setDisable(!enabled);
//        stockInput.setDisable(!enabled);
//        descriptionInput.setDisable(!enabled);
//        typeBox.setDisable(!enabled);
//        characterBox.setDisable(!enabled);
//        chooseImageBtn.setDisable(!enabled);
//        updateBtn.setDisable(!enabled);
//        deleteBtn.setDisable(!enabled);
//        clearBtn.setDisable(!enabled);
//        addBtn.setDisable(false); // Add button always enabled
//
//        // Add a prompt text when disabled
//        if (!enabled) {
//            String msg = "Select an item from the table first";
//            nameInput.setPromptText(msg);
//            priceInput.setPromptText(msg);
//            stockInput.setPromptText(msg);
//
//            // Add tooltip to explain
//            Tooltip tooltip = new Tooltip("Select an item from the table to edit");
//            nameInput.setTooltip(tooltip);
//            priceInput.setTooltip(tooltip);
//            stockInput.setTooltip(tooltip);
//            typeBox.setTooltip(tooltip);
//            characterBox.setTooltip(tooltip);
//        } else {
//            nameInput.setPromptText("Item Name");
//            priceInput.setPromptText("0.00");
//            stockInput.setPromptText("0");
//            nameInput.setTooltip(null);
//            priceInput.setTooltip(null);
//            stockInput.setTooltip(null);
//            typeBox.setTooltip(null);
//            characterBox.setTooltip(null);
//        }
//
//        // Update selection hint visibility
//        if (selectionHint != null) {
//            selectionHint.setVisible(!enabled);
//            selectionHint.setText("Select an item from the table above to edit");
//        }
//    }

    private void applyFilter(String character) {
        currentFilter = character;
        if ("ALL".equals(character)) {
            inventoryTable.setItems(allItems);
        } else {
            List<FoodItem> filtered = allItems.stream()
                    .filter(item -> character.equals(item.getCharCategory()))
                    .collect(Collectors.toList());
            inventoryTable.setItems(FXCollections.observableArrayList(filtered));
        }
        updateItemCount();
    }

    @FXML private void filterByChloe() { applyFilter("Chloe"); }
    @FXML private void filterByMimi() { applyFilter("Mimi"); }
    @FXML private void filterByMetsu() { applyFilter("Metsu"); }
    @FXML private void filterByLaniard() { applyFilter("Laniard"); }
    @FXML private void filterAll() { applyFilter("ALL"); }

    private String copyImageToProject(String sourcePath) throws IOException {
        File sourceFile = new File(sourcePath);
        String fileName = sourceFile.getName();
        String projectRoot = System.getProperty("user.dir");
        String targetDir = projectRoot + "/src/main/resources/images/";
        File targetDirFile = new File(targetDir);
        if (!targetDirFile.exists()) {
            targetDirFile.mkdirs();
        }
        String targetPath = targetDir + fileName;
        Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        return "/images/" + fileName;
    }

    @FXML
    private void handleAddItem() {
        if (!isAuthorized()) return;

        // Enable form for adding new item
        enableFormFields();
        clearForm();  // clear any existing data

        // focus on name field
        nameInput.requestFocus();
        showTemporaryStatus("Enter new item details, then click Save.", "blue");
    }

    @FXML
    private void handleEditItem() {
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryStatus("Select an item first to edit", "orange");
            return;
        }

        if (!isAuthorized()) return;
        if (!validateInputs()) return;

        try {
            int charId = getCharacterId(characterBox.getValue());

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
                showTemporaryStatus("Item updated successfully!", "green");
            } else {
                showTemporaryStatus("Failed to update item", "red");
            }
        } catch (NumberFormatException e) {
            showTemporaryStatus("Invalid price or stock value", "red");
        }
    }

    @FXML
    private void handleRemoveItem() {
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryStatus("Select an item first to remove", "orange");
            return;
        }

        if (!isAuthorized()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Remove " + selected.getName() + "?");
        confirm.setContentText("This item will be soft-deleted (hidden from menu).");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (adminDAO.removeItem(selected.getId())) {
                loadAllItems();
                applyFilter(currentFilter);
                clearForm();
                showTemporaryStatus("Item removed successfully", "green");
            } else {
                showTemporaryStatus("Failed to remove item", "red");
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showTemporaryStatus("Select an item first before adding an image", "orange");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Food Item Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            try {
                selectedImagePath = copyImageToProject(selectedFile.getAbsolutePath());
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
                showTemporaryStatus("Image copied: " + selectedFile.getName(), "blue");
            } catch (IOException e) {
                showTemporaryStatus("Failed to copy image: " + e.getMessage(), "red");
            }
        }
    }

    private void loadAllItems() {
        allItems.clear();
        allItems.addAll(adminDAO.getAllInventory());
        updateItemCount();
    }

    private void updateItemCount() {
        itemCountLabel.setText(String.valueOf(inventoryTable.getItems().size()));
    }

    private void populateForm(FoodItem item) {
        nameInput.setText(item.getName());
        priceInput.setText(String.valueOf(item.getPrice()));
        stockInput.setText(String.valueOf(item.getStock()));
        typeBox.setValue(item.getItemType());
        characterBox.setValue(item.getCharCategory());
        descriptionInput.setText(item.getDescription() != null ? item.getDescription() : "");
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

//    private FoodItem buildItemFromInput() {
//        return new FoodItem.FoodItemBuilder()
//                .setName(nameInput.getText())
//                .setPrice(Double.parseDouble(priceInput.getText()))
//                .setStock(Integer.parseInt(stockInput.getText()))
//                .setCharCategory(characterBox.getValue())
//                .setItemType(typeBox.getValue())
//                .setImagePath(selectedImagePath)
//                .setDescription(descriptionInput.getText())
//                .build();
//    }

    private int getCharacterId(String characterName) {
        switch (characterName) {
            case "Chloe": return 1;
            case "Mimi": return 2;
            case "Metsu": return 3;
            case "Laniard": return 4;
            default: return 1;
        }
    }

    private boolean validateInputs() {
        if (nameInput.getText().trim().isEmpty()) {
            showTemporaryStatus("Please enter an item name", "orange");
            return false;
        }
        if (priceInput.getText().trim().isEmpty()) {
            showTemporaryStatus("Please enter a price", "orange");
            return false;
        }
        if (stockInput.getText().trim().isEmpty()) {
            showTemporaryStatus("Please enter stock quantity", "orange");
            return false;
        }
        if (characterBox.getValue() == null) {
            showTemporaryStatus("Please select a character", "orange");
            return false;
        }
        if (typeBox.getValue() == null) {
            typeBox.setValue("Main");
        }
        return true;
    }

    // simple pin check
    private boolean isAuthorized() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Security Clearance");
        dialog.setHeaderText("Enter PIN to continue");
        dialog.setContentText("Enter 4-digit PIN:");
        Optional<String> result = dialog.showAndWait();
        return result.isPresent() && result.get().equals(MASTER_PIN);
    }

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

    @FXML
    public void refreshTable() {
        loadAllItems();
        applyFilter(currentFilter);
        showTemporaryStatus("Table refreshed", "blue");
    }

    private void showTemporaryStatus(String message, String type) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-warning", "status-info", "status-muted");

        String styleClass = "status-muted";
        if (type.equalsIgnoreCase("green")) styleClass = "status-success";
        else if (type.equalsIgnoreCase("red")) styleClass = "status-error";
        else if (type.equalsIgnoreCase("orange")) styleClass = "status-warning";
        else if (type.equalsIgnoreCase("blue")) styleClass = "status-info";

        statusLabel.getStyleClass().add(styleClass);

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
            showTemporaryStatus("Error returning to dashboard", "red");
        }
    }
}