package com.files.projBistro.controllers.admin;

import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.dao.AdminDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AdminInventoryController {

    private TableView<FoodItem> inventoryTable;
    private TableColumn<FoodItem, Integer> colId;
    private TableColumn<FoodItem, String> colName;
    private TableColumn<FoodItem, Double> colPrice;
    private TableColumn<FoodItem, Integer> colStock;
    private TableColumn<FoodItem, String> colCategory;
    private TableColumn<FoodItem, Void> colActive;
    private TextField nameInput;
    private TextField priceInput;
    private TextField stockInput;
    private ComboBox<String> categoryBox;
    private TextArea descriptionInput;
    private Label imagePathLabel;
    private ImageView imagePreview;
    private Label statusLabel;

    private AdminDAO adminDAO;
    private String selectedImagePath = null;
    private String tempImageFilePath = null;
    private BooleanSupplier isAuthorized;
    private Consumer<String> showStatus;

    // Valid characters list for validation
    private static final String[] VALID_CHARACTERS = {"Chloe", "Mimi", "Metsu", "Laniard"};

    public void init(AdminDAO adminDAO, Label statusLabel, Consumer<String> showStatus, BooleanSupplier isAuthorized) {
        this.adminDAO = adminDAO;
        this.statusLabel = statusLabel;
        this.showStatus = showStatus;
        this.isAuthorized = isAuthorized;
    }

    public void setUIElements(TableView<FoodItem> inventoryTable,
                              TableColumn<FoodItem, Integer> colId,
                              TableColumn<FoodItem, String> colName,
                              TableColumn<FoodItem, Double> colPrice,
                              TableColumn<FoodItem, Integer> colStock,
                              TableColumn<FoodItem, String> colCategory,
                              TableColumn<FoodItem, Void> colActive,
                              TextField nameInput,
                              TextField priceInput,
                              TextField stockInput,
                              ComboBox<String> categoryBox,
                              TextArea descriptionInput,
                              Label imagePathLabel,
                              ImageView imagePreview) {
        this.inventoryTable = inventoryTable;
        this.colId = colId;
        this.colName = colName;
        this.colPrice = colPrice;
        this.colStock = colStock;
        this.colCategory = colCategory;
        this.colActive = colActive;
        this.nameInput = nameInput;
        this.priceInput = priceInput;
        this.stockInput = stockInput;
        this.categoryBox = categoryBox;
        this.descriptionInput = descriptionInput;
        this.imagePathLabel = imagePathLabel;
        this.imagePreview = imagePreview;

        setupTableColumns();
        setupToggleButtonColumn();
        setupTableSelectionListener();
        setupCategoryDropdown();
    }

    private void setupCategoryDropdown() {
        categoryBox.setItems(FXCollections.observableArrayList(VALID_CHARACTERS));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colStock.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStock()));
        colCategory.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCharCategory()));
    }

    private void setupToggleButtonColumn() {
        colActive.setCellFactory(param -> new TableCell<FoodItem, Void>() {
            private final Button toggleBtn = new Button();
            {
                toggleBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");
                toggleBtn.setOnAction(event -> {
                    FoodItem item = getTableView().getItems().get(getIndex());
                    toggleItemVisibility(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FoodItem foodItem = getTableView().getItems().get(getIndex());
                    if (foodItem.isActive()) {
                        toggleBtn.setText("✓ Visible");
                        toggleBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");
                    } else {
                        toggleBtn.setText("✗ Hidden");
                        toggleBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");
                    }
                    setGraphic(toggleBtn);
                }
            }
        });
    }

    private void setupTableSelectionListener() {
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newItem) -> {
            if (newItem != null) {
                nameInput.setText(newItem.getName());
                priceInput.setText(String.valueOf(newItem.getPrice()));
                stockInput.setText(String.valueOf(newItem.getStock()));
                categoryBox.setValue(newItem.getCharCategory());
                descriptionInput.setText(newItem.getDescription() != null ? newItem.getDescription() : "");
                selectedImagePath = newItem.getImagePath();
                tempImageFilePath = null;
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    imagePathLabel.setText(new File(selectedImagePath).getName());
                } else {
                    imagePathLabel.setText("No image selected.");
                }
                updateImagePreview(selectedImagePath, null);
                showStatus.accept("Item loaded. Click 'Save Changes' to update.");
            }
        });
    }

    private void toggleItemVisibility(FoodItem item) {
        if (!isAuthorized.getAsBoolean()) return;
        boolean newStatus = !item.isActive();
        if (adminDAO.toggleItemVisibility(item.getId(), newStatus)) {
            item.setActive(newStatus);
            refreshTable();
            showStatus.accept("Item " + (newStatus ? "shown" : "hidden") + " successfully.");
        } else {
            showStatus.accept("Failed to update item visibility.");
        }
    }

    private String copyImageToProject(String sourcePath) throws IOException {
        File sourceFile = new File(sourcePath);
        String fileName = sourceFile.getName();
        String projectRoot = System.getProperty("user.dir");
        String targetDir = projectRoot + "/src/main/resources/images/";
        File targetDirFile = new File(targetDir);
        if (!targetDirFile.exists()) targetDirFile.mkdirs();
        String targetPath = targetDir + fileName;
        Files.copy(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
        return "/images/" + fileName;
    }

    // ===== FIXED: validateCharacter with proper error handling =====
    private int getCharacterId(String characterName) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return -1; // Invalid
        }
        switch (characterName) {
            case "Chloe": return 1;
            case "Mimi": return 2;
            case "Metsu": return 3;
            case "Laniard": return 4;
            default: return -1; // Unknown character
        }
    }

    private boolean isValidCharacter(String characterName) {
        return getCharacterId(characterName) != -1;
    }

    public void handleAddItemPopup() {
        if (!isAuthorized.getAsBoolean()) return;

        Dialog<FoodItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Item");
        dialog.setHeaderText("Enter item details.");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField popupName = new TextField();
        popupName.setPromptText("Item name");
        TextField popupPrice = new TextField();
        popupPrice.setPromptText("0.00");
        TextField popupStock = new TextField();
        popupStock.setPromptText("0");
        ComboBox<String> popupCategory = new ComboBox<>();
        popupCategory.setItems(FXCollections.observableArrayList(VALID_CHARACTERS));
        popupCategory.setPromptText("Select character");
        ComboBox<String> popupType = new ComboBox<>();
        popupType.setItems(FXCollections.observableArrayList("Main", "Appetizer", "Dessert", "Drink", "Special"));
        popupType.setValue("Main");

        TextArea popupDescription = new TextArea();
        popupDescription.setPromptText("Item description");
        popupDescription.setPrefRowCount(3);
        popupDescription.setWrapText(true);

        Label imageLabel = new Label("No image selected.");
        imageLabel.setStyle("-fx-text-fill: #666;");
        Button chooseImageBtn = new Button("Choose Image");

        final String[] tempImagePath = {null};

        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                tempImagePath[0] = selectedFile.getAbsolutePath();
                imageLabel.setText(selectedFile.getName());
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(popupName, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(popupPrice, 1, 1);
        grid.add(new Label("Stock:"), 0, 2);
        grid.add(popupStock, 1, 2);
        grid.add(new Label("Character:"), 0, 3);
        grid.add(popupCategory, 1, 3);
        grid.add(new Label("Type:"), 0, 4);
        grid.add(popupType, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(popupDescription, 1, 5);
        grid.add(new Label("Image:"), 0, 6);
        HBox imageBox = new HBox(10, imageLabel, chooseImageBtn);
        grid.add(imageBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == addButtonType) {
                try {
                    String name = popupName.getText().trim();
                    String priceText = popupPrice.getText().trim();
                    String stockText = popupStock.getText().trim();
                    String category = popupCategory.getValue();
                    String type = popupType.getValue();
                    String description = popupDescription.getText().trim();

                    // VALIDATION: Name
                    if (name.isEmpty()) {
                        showStatus.accept("Item name is required.");
                        return null;
                    }
                    if (name.length() > 100) {
                        showStatus.accept("Item name is too long (max 100 characters).");
                        return null;
                    }

                    // VALIDATION: Price
                    double price;
                    try {
                        price = Double.parseDouble(priceText);
                        if (price < 0) {
                            showStatus.accept("Price cannot be negative.");
                            return null;
                        }
                        if (price > 9999.99) {
                            showStatus.accept("Price is too high (max £9999.99).");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showStatus.accept("Invalid price format.");
                        return null;
                    }

                    // VALIDATION: Stock
                    int stock;
                    try {
                        stock = Integer.parseInt(stockText);
                        if (stock < 0) {
                            showStatus.accept("Stock cannot be negative.");
                            return null;
                        }
                        if (stock > 999999) {
                            showStatus.accept("Stock is too high (max 999,999).");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showStatus.accept("Invalid stock quantity.");
                        return null;
                    }

                    // VALIDATION: Character category
                    if (category == null || !isValidCharacter(category)) {
                        showStatus.accept("Please select a valid character (Chloe, Mimi, Metsu, or Laniard).");
                        return null;
                    }

                    String finalImagePath = null;
                    if (tempImagePath[0] != null) {
                        finalImagePath = copyImageToProject(tempImagePath[0]);
                    }

                    int charId = getCharacterId(category);
                    FoodItem newItem = new FoodItem.FoodItemBuilder()
                            .setName(name)
                            .setPrice(price)
                            .setStock(stock)
                            .setCharCategory(category)
                            .setItemType(type)
                            .setImagePath(finalImagePath)
                            .setDescription(description)
                            .build();

                    if (adminDAO.addItem(newItem, charId)) {
                        return newItem;
                    } else {
                        showStatus.accept("Database error: Failed to add item.");
                    }
                } catch (NumberFormatException ex) {
                    showStatus.accept("Invalid price or stock value.");
                } catch (IOException ex) {
                    showStatus.accept("Failed to save image: " + ex.getMessage());
                }
            }
            return null;
        });

        Optional<FoodItem> result = dialog.showAndWait();
        if (result.isPresent()) {
            refreshTable();
            showStatus.accept("Item added successfully!");
        }
    }

    // ===== FIXED: Save with validation =====
    public void handleSaveItem() {
        if (!isAuthorized.getAsBoolean()) return;

        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showStatus.accept("Select an item first to edit, or use the 'Add Item' button.");
            return;
        }

        if (!validateInputs()) return;

        try {
            String selectedCategory = categoryBox.getValue();

            // VALIDATION: Character category
            if (selectedCategory == null || !isValidCharacter(selectedCategory)) {
                showStatus.accept("Please select a valid character (Chloe, Mimi, Metsu, or Laniard).");
                return;
            }

            int charId = getCharacterId(selectedCategory);
            if (charId == -1) {
                showStatus.accept("Invalid character selected.");
                return;
            }

            String finalImagePath = null;
            if (tempImageFilePath != null && !tempImageFilePath.isEmpty()) {
                finalImagePath = copyImageToProject(tempImageFilePath);
            } else if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                finalImagePath = selectedImagePath;
            }

            FoodItem updatedItem = new FoodItem.FoodItemBuilder()
                    .setId(selected.getId())
                    .setName(nameInput.getText().trim())
                    .setPrice(Double.parseDouble(priceInput.getText().trim()))
                    .setStock(Integer.parseInt(stockInput.getText().trim()))
                    .setCharCategory(selectedCategory)
                    .setItemType(selected.getItemType())
                    .setImagePath(finalImagePath != null ? finalImagePath : selected.getImagePath())
                    .setIsActive(selected.isActive())
                    .setDescription(descriptionInput.getText())
                    .build();

            if (adminDAO.updateItem(updatedItem, charId)) {
                refreshTable();
                clearInputs();
                inventoryTable.getSelectionModel().clearSelection();
                showStatus.accept("Item saved successfully!");
            } else {
                showStatus.accept("Failed to save item.");
            }
        } catch (NumberFormatException e) {
            showStatus.accept("Invalid price or stock value.");
        } catch (IOException e) {
            showStatus.accept("Failed to save image: " + e.getMessage());
        }
    }

    public void handleHardDeleteItem() {
        if (!isAuthorized.getAsBoolean()) return;

        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showStatus.accept("Select an item first to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Permanent Deletion");
        confirm.setHeaderText("Permanently delete " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone. The item will be completely removed from the database.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (adminDAO.hardDeleteItem(selected.getId())) {
                refreshTable();
                clearInputs();
                showStatus.accept("Item permanently deleted.");
            } else {
                showStatus.accept("Failed to delete item.");
            }
        }
    }

    public void handleChooseImage() {
        if (!isAuthorized.getAsBoolean()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Food Item Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            tempImageFilePath = selectedFile.getAbsolutePath();
            imagePathLabel.setText(selectedFile.getName());
            updateImagePreview(tempImageFilePath, selectedFile.toURI().toString());
            showStatus.accept("Image selected. It will be saved when you click 'Save Changes'.");
        }
    }

    public void clearInputs() {
        nameInput.clear();
        priceInput.clear();
        stockInput.clear();
        categoryBox.getSelectionModel().clearSelection();
        if (descriptionInput != null) descriptionInput.clear();
        selectedImagePath = null;
        tempImageFilePath = null;
        imagePathLabel.setText("No image selected.");
        if (imagePreview != null) imagePreview.setImage(null);
        inventoryTable.getSelectionModel().clearSelection();
        showStatus.accept("Form cleared.");
    }

    public void refreshTable() {
        inventoryTable.getItems().clear();
        List<FoodItem> freshItems = adminDAO.getAllInventory();
        inventoryTable.setItems(FXCollections.observableArrayList(freshItems));
        inventoryTable.refresh();
    }

    // ===== FIXED: validateInputs with better checks =====
    private boolean validateInputs() {
        String name = nameInput.getText();
        if (name == null || name.trim().isEmpty()) {
            showStatus.accept("Please enter an item name.");
            return false;
        }
        if (name.length() > 100) {
            showStatus.accept("Item name is too long (max 100 characters).");
            return false;
        }

        String priceText = priceInput.getText();
        if (priceText == null || priceText.trim().isEmpty()) {
            showStatus.accept("Please enter a price.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceText.trim());
            if (price < 0) {
                showStatus.accept("Price cannot be negative.");
                return false;
            }
            if (price > 9999.99) {
                showStatus.accept("Price is too high (max £9999.99).");
                return false;
            }
        } catch (NumberFormatException e) {
            showStatus.accept("Invalid price format.");
            return false;
        }

        String stockText = stockInput.getText();
        if (stockText == null || stockText.trim().isEmpty()) {
            showStatus.accept("Please enter stock quantity.");
            return false;
        }
        try {
            int stock = Integer.parseInt(stockText.trim());
            if (stock < 0) {
                showStatus.accept("Stock cannot be negative.");
                return false;
            }
            if (stock > 999999) {
                showStatus.accept("Stock is too high (max 999,999).");
                return false;
            }
        } catch (NumberFormatException e) {
            showStatus.accept("Invalid stock quantity.");
            return false;
        }

        String category = categoryBox.getValue();
        if (category == null || !isValidCharacter(category)) {
            showStatus.accept("Please select a valid character (Chloe, Mimi, Metsu, or Laniard).");
            return false;
        }

        return true;
    }

    private void updateImagePreview(String imagePath, String fallbackUri) {
        if (imagePreview == null) return;
        Image image = null;
        if (fallbackUri != null && !fallbackUri.isBlank()) {
            try { image = new Image(fallbackUri, true); } catch (Exception ignored) {}
        }
        if (image == null && imagePath != null && !imagePath.isBlank()) {
            try {
                var resource = getClass().getResource(imagePath);
                if (resource != null) image = new Image(resource.toExternalForm(), true);
            } catch (Exception ignored) {}
        }
        imagePreview.setImage(image);
    }
}