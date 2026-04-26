package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.dao.AdminDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Tooltip;
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


    @FXML private TableView<FoodItem> inventoryTable;
    @FXML private TableColumn<FoodItem, Integer> colId;
    @FXML private TableColumn<FoodItem, String> colName;
    @FXML private TableColumn<FoodItem, Double> colPrice;
    @FXML private TableColumn<FoodItem, Integer> colStock;
    @FXML private TableColumn<FoodItem, String> colCategory;
    @FXML private TableColumn<FoodItem, Void> colActive;
    @FXML private TextField nameInput;
    @FXML private TextField priceInput;
    @FXML private TextField stockInput;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextArea descriptionInput;
    @FXML private Label imagePathLabel;
    @FXML private ImageView imagePreview;
    @FXML private Label statusLabel;
    private VBox formOverlay;

    private AdminDAO adminDAO;
    private String selectedImagePath = null;
    private String tempImageFilePath = null;
    private BooleanSupplier isAuthorized;
    private Consumer<String> showStatus;

    private static final String[] VALID_CHARACTERS = {"Chloe", "Mimi", "Metsu", "Laniard"};

    @FXML
    public void initialize() {
        // This runs after FXML is loaded
        setupTableColumns();
        setupToggleButtonColumn();
        setupTableSelectionListener();
        setupCategoryDropdown();

        // Initially show overlay (no item selected)
        showOverlay(true);
    }

    public void init(AdminDAO adminDAO, Label statusLabel, Consumer<String> showStatus, BooleanSupplier isAuthorized) {
        this.adminDAO = adminDAO;
        this.statusLabel = statusLabel;
        this.showStatus = showStatus;
        this.isAuthorized = isAuthorized;
    }

    // connects ui elements from parent controller
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
        // For backward compatibility, but prefer @FXML injection
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
        showOverlay(true);
    }

    private void setupCategoryDropdown() {
        categoryBox.setItems(FXCollections.observableArrayList(VALID_CHARACTERS));
    }

    // tells each table column what data to show from FoodItem objects
    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        colStock.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStock()));
        colCategory.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCharCategory()));
    }

    // adds the visible/hidden toggle button column
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

    // when admin clicks a row in the table, load that item into the form
    private void setupTableSelectionListener() {
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newItem) -> {
            System.out.println("Selection changed - newItem: " + (newItem != null ? newItem.getName() : "null")); // Debug
            if (newItem != null) {
                Platform.runLater(() -> showOverlay(false));  // Hide overlay when item selected
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
                if (showStatus != null) {
                    showStatus.accept("Item loaded. Click 'Save Changes' to update.");
                }
            } else {
                Platform.runLater(() -> showOverlay(true));   // show gray overlay
                clearInputs();
            }
        });
    }

    // toggles item between visible and hidden on menu
    private void toggleItemVisibility(FoodItem item) {
        if (!isAuthorized.getAsBoolean()) return;
        boolean newStatus = !item.isActive();
        if (adminDAO.toggleItemVisibility(item.getId(), newStatus)) {
            item.setActive(newStatus);
            inventoryTable.refresh();
            if (showStatus != null) {
                showStatus.accept("Item " + (newStatus ? "shown" : "hidden") + " successfully.");
            }
        } else {
            if (showStatus != null) {
                showStatus.accept("Failed to update item visibility.");
            }
        }
    }

    // copies selected image to both src and target folders so it works in dev and runtime
    private String copyImageToProject(String sourcePath) throws IOException {
        File sourceFile = new File(sourcePath);
        String fileName = sourceFile.getName();
        String projectRoot = System.getProperty("user.dir");

        // copy to source folder (for development)
        String sourceDir = projectRoot + "/src/main/resources/images/";
        File sourceDirFile = new File(sourceDir);
        if (!sourceDirFile.exists()) sourceDirFile.mkdirs();
        String sourceTargetPath = sourceDir + fileName;
        Files.copy(Paths.get(sourcePath), Paths.get(sourceTargetPath), StandardCopyOption.REPLACE_EXISTING);

        // copy to target folder (for runtime when running from jar)
        String targetDir = projectRoot + "/target/classes/images/";
        File targetDirFile = new File(targetDir);
        if (!targetDirFile.exists()) targetDirFile.mkdirs();
        String targetTargetPath = targetDir + fileName;
        Files.copy(Paths.get(sourcePath), Paths.get(targetTargetPath), StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Image copied to: " + sourceTargetPath);
        System.out.println("Image copied to: " + targetTargetPath);

        return "/images/" + fileName;
    }

    private int getCharacterId(String characterName) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return -1;
        }
        switch (characterName) {
            case "Chloe": return 1;
            case "Mimi": return 2;
            case "Metsu": return 3;
            case "Laniard": return 4;
            default: return -1;
        }
    }

    private boolean isValidCharacter(String characterName) {
        return getCharacterId(characterName) != -1;
    }

    @FXML
    public void handleAddItemPopup() {
        if (!isAuthorized.getAsBoolean()) return;

        Dialog<FoodItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Item");
        dialog.setHeaderText("Enter item details.");
        dialog.setResizable(true);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        // Error label at the top
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(400);
        errorLabel.setVisible(false);

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

        Label infoIcon = new Label("ⓘ");
        infoIcon.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-cursor: hand;");

        Tooltip tooltip = new Tooltip("You can add an item without adding a picture.\nThe image field is optional.");
        tooltip.setShowDelay(javafx.util.Duration.millis(0));
        tooltip.setShowDuration(javafx.util.Duration.seconds(5));

        infoIcon.setTooltip(tooltip);

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

        // Row 0: Error label (spans both columns)
        grid.add(errorLabel, 0, 0, 2, 1);

        grid.add(new Label("Name:"), 0, 1);
        grid.add(popupName, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(popupPrice, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(popupStock, 1, 3);
        grid.add(new Label("Character:"), 0, 4);
        grid.add(popupCategory, 1, 4);
        grid.add(new Label("Type:"), 0, 5);
        grid.add(popupType, 1, 5);
        grid.add(new Label("Description:"), 0, 6);
        grid.add(popupDescription, 1, 6);
        grid.add(new Label("Image:"), 0, 7);
        HBox imageBox = new HBox(10, imageLabel, chooseImageBtn, infoIcon);
        grid.add(imageBox, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // disable Add button initially until all required fields are filled
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // real-time validation to enable/disable Add button
        popupName.textProperty().addListener((obs, old, val) -> validateForm(addButton, errorLabel, popupName, popupPrice, popupStock, popupCategory));
        popupPrice.textProperty().addListener((obs, old, val) -> validateForm(addButton, errorLabel, popupName, popupPrice, popupStock, popupCategory));
        popupStock.textProperty().addListener((obs, old, val) -> validateForm(addButton, errorLabel, popupName, popupPrice, popupStock, popupCategory));
        popupCategory.valueProperty().addListener((obs, old, val) -> validateForm(addButton, errorLabel, popupName, popupPrice, popupStock, popupCategory));

        dialog.setResultConverter(button -> {
            if (button == addButtonType) {
                try {
                    String name = popupName.getText().trim();
                    String priceText = popupPrice.getText().trim();
                    String stockText = popupStock.getText().trim();
                    String category = popupCategory.getValue();
                    String type = popupType.getValue();
                    String description = popupDescription.getText().trim();

                    if (name.isEmpty()) {
                        showError(errorLabel, "Item name is required.");
                        return null;
                    }
                    if (name.length() > 100) {
                        showError(errorLabel, "Item name is too long (max 100 characters).");
                        return null;
                    }

                    double price;
                    try {
                        price = Double.parseDouble(priceText);
                        if (price < 0) {
                            showError(errorLabel, "Price cannot be negative.");
                            return null;
                        }
                        if (price > 9999.99) {
                            showError(errorLabel, "Price is too high (max RM9999.99).");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showError(errorLabel, "Invalid price format.");
                        return null;
                    }

                    int stock;
                    try {
                        stock = Integer.parseInt(stockText);
                        if (stock < 0) {
                            showError(errorLabel, "Stock cannot be negative.");
                            return null;
                        }
                        if (stock > 999999) {
                            showError(errorLabel, "Stock is too high (max 999,999).");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showError(errorLabel, "Invalid stock quantity.");
                        return null;
                    }

                    if (category == null || !isValidCharacter(category)) {
                        showError(errorLabel, "Please select a valid character (Chloe, Mimi, Metsu, or Laniard).");
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
                        showError(errorLabel, "Database error: Failed to add item.");
                        return null;
                    }
                } catch (Exception ex) {
                    showError(errorLabel, "Error: " + ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<FoodItem> result = dialog.showAndWait();
        if (result.isPresent() && showStatus != null) {
            showStatus.accept("Item added successfully! Click Refresh to see changes.");
        }
    }

    private void validateForm(Node addButton, Label errorLabel, TextField name, TextField price, TextField stock, ComboBox<String> category) {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        String nameText = name.getText().trim();
        if (nameText.isEmpty()) {
            isValid = false;
            errors.append("• Item name is required\n");
        } else if (nameText.length() > 100) {
            isValid = false;
            errors.append("• Name too long (max 100 characters)\n");
        }

        String priceText = price.getText().trim();
        if (priceText.isEmpty()) {
            isValid = false;
            errors.append("• Price is required\n");
        } else {
            try {
                double p = Double.parseDouble(priceText);
                if (p < 0) {
                    isValid = false;
                    errors.append("• Price cannot be negative\n");
                } else if (p > 9999.99) {
                    isValid = false;
                    errors.append("• Price too high (max RM9999.99)\n");
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errors.append("• Invalid price format\n");
            }
        }

        String stockText = stock.getText().trim();
        if (stockText.isEmpty()) {
            isValid = false;
            errors.append("• Stock quantity is required\n");
        } else {
            try {
                int s = Integer.parseInt(stockText);
                if (s < 0) {
                    isValid = false;
                    errors.append("• Stock cannot be negative\n");
                } else if (s > 999999) {
                    isValid = false;
                    errors.append("• Stock too high (max 999,999)\n");
                }
            } catch (NumberFormatException e) {
                isValid = false;
                errors.append("• Invalid stock quantity\n");
            }
        }

        if (category.getValue() == null || !isValidCharacter(category.getValue())) {
            isValid = false;
            errors.append("• Please select a valid character\n");
        }

        addButton.setDisable(!isValid);
        if (!isValid) {
            errorLabel.setText(errors.toString());
            errorLabel.setVisible(true);
        } else {
            errorLabel.setVisible(false);
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    // saves changes to currently selected item
    @FXML
    public void handleSaveItem() {
        if (!isAuthorized.getAsBoolean()) return;

        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            if (showStatus != null) {
                showStatus.accept("Select an item first to edit, or use the 'Add Item' button.");
            }
            return;
        }

        if (!validateInputs()) return;

        try {
            String selectedCategory = categoryBox.getValue();

            if (selectedCategory == null || !isValidCharacter(selectedCategory)) {
                if (showStatus != null) {
                    showStatus.accept("Please select a valid character (Chloe, Mimi, Metsu, or Laniard).");
                }
                return;
            }

            int charId = getCharacterId(selectedCategory);
            if (charId == -1) {
                if (showStatus != null) {
                    showStatus.accept("Invalid character selected.");
                }
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
                clearInputs();
                inventoryTable.getSelectionModel().clearSelection();
                if (showStatus != null) {
                    showStatus.accept("Item saved successfully! Click Refresh to see changes.");
                }
            } else {
                if (showStatus != null) {
                    showStatus.accept("Failed to save item.");
                }
            }
        } catch (NumberFormatException e) {
            if (showStatus != null) {
                showStatus.accept("Invalid price or stock value.");
            }
        } catch (IOException e) {
            if (showStatus != null) {
                showStatus.accept("Failed to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleHardDeleteItem() {
        if (!isAuthorized.getAsBoolean()) return;

        FoodItem selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            if (showStatus != null) {
                showStatus.accept("Select an item first to delete.");
            }
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Permanent Deletion");
        confirm.setHeaderText("Permanently delete " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone. The item will be completely removed from the database.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (adminDAO.hardDeleteItem(selected.getId())) {

                // Delete the associated image file
                String imagePath = selected.getImagePath();
                if (imagePath != null && !imagePath.isEmpty()) {
                    try {
                        String projectRoot = System.getProperty("user.dir");
                        String fullPath = projectRoot + "/src/main/resources" + imagePath;
                        File imageFile = new File(fullPath);
                        if (imageFile.exists()) {
                            imageFile.delete();
                        }
                    } catch (Exception e) {
                        System.out.println("Could not delete image: " + e.getMessage());
                    }
                }
                clearInputs();
                if (showStatus != null) {
                    showStatus.accept("Item permanently deleted. Click Refresh to see changes.");
                }
            } else {
                if (showStatus != null) {
                    showStatus.accept("Failed to delete item.");
                }
            }
        }
    }

    @FXML
    public void handleChooseImage() {
        if (!isAuthorized.getAsBoolean()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Food Item Image");

        String userHome = System.getProperty("user.home");
        File downloadsFolder = new File(userHome + "/Downloads");
        if (downloadsFolder.exists()) {
            fileChooser.setInitialDirectory(downloadsFolder);
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            tempImageFilePath = selectedFile.getAbsolutePath();
            imagePathLabel.setText(selectedFile.getName());
            updateImagePreview(tempImageFilePath, selectedFile.toURI().toString());
            if (showStatus != null) {
                showStatus.accept("Image selected. It will be saved when you click 'Save Changes'.");
            }
        }
    }

    @FXML
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
        showOverlay(true);  // Show overlay when cleared
        if (showStatus != null) {
            showStatus.accept("Form cleared.");
        }
    }

    @FXML
    public void refreshInventoryTable() {
        if (inventoryTable != null && adminDAO != null) {
            inventoryTable.getItems().clear();
            List<FoodItem> freshItems = adminDAO.getAllInventory();
            inventoryTable.setItems(FXCollections.observableArrayList(freshItems));
            inventoryTable.refresh();
            if (showStatus != null) {
                showStatus.accept("Inventory table refreshed.");
            }
        }
    }

    private boolean validateInputs() {
        String name = nameInput.getText();
        if (name == null || name.trim().isEmpty()) {
            if (showStatus != null) showStatus.accept("Please enter an item name.");
            return false;
        }
        if (name.length() > 100) {
            if (showStatus != null) showStatus.accept("Item name is too long (max 100 characters).");
            return false;
        }

        String priceText = priceInput.getText();
        if (priceText == null || priceText.trim().isEmpty()) {
            if (showStatus != null) showStatus.accept("Please enter a price.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceText.trim());
            if (price < 0) {
                if (showStatus != null) showStatus.accept("Price cannot be negative.");
                return false;
            }
            if (price > 9999.99) {
                if (showStatus != null) showStatus.accept("Price is too high (max RM9999.99).");
                return false;
            }
        } catch (NumberFormatException e) {
            if (showStatus != null) showStatus.accept("Invalid price format.");
            return false;
        }

        String stockText = stockInput.getText();
        if (stockText == null || stockText.trim().isEmpty()) {
            if (showStatus != null) showStatus.accept("Please enter stock quantity.");
            return false;
        }
        try {
            int stock = Integer.parseInt(stockText.trim());
            if (stock < 0) {
                if (showStatus != null) showStatus.accept("Stock cannot be negative.");
                return false;
            }
            if (stock > 999999) {
                if (showStatus != null) showStatus.accept("Stock is too high (max 999,999).");
                return false;
            }
        } catch (NumberFormatException e) {
            if (showStatus != null) showStatus.accept("Invalid stock quantity.");
            return false;
        }

        String category = categoryBox.getValue();
        if (category == null || !isValidCharacter(category)) {
            if (showStatus != null) showStatus.accept("Please select a valid character (Chloe, Mimi, Metsu, or Laniard).");
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

    @FXML
    public void handleAddMultipleItemsPopup() {
        if (!isAuthorized.getAsBoolean()) return;

        Stage ownerStage = (Stage) inventoryTable.getScene().getWindow();

        MultipleItemDialog dialog = new MultipleItemDialog();
        List<FoodItem> items = dialog.showAndWait(ownerStage);

        if (items == null || items.isEmpty()) {
            if (showStatus != null) showStatus.accept("No items added.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (FoodItem item : items) {
            int charId = getCharacterId(item.getCharCategory());
            if (adminDAO.addItem(item, charId)) {
                successCount++;
            } else {
                failCount++;
            }
        }

        refreshInventoryTable();
        if (showStatus != null) {
            showStatus.accept("Added " + successCount + " items successfully." +
                    (failCount > 0 ? " " + failCount + " failed." : ""));
        }
    }

    private void showOverlay(boolean show) {
        System.out.println("showOverlay called with: " + show);
        if (formOverlay != null) {
            formOverlay.setVisible(show);
            formOverlay.setManaged(show);
            formOverlay.setDisable(show); // Also disable the overlay so it blocks input
            System.out.println("Overlay visibility set to: " + show);
            System.out.println("Overlay visible property: " + formOverlay.isVisible());
        } else {
            System.err.println("formOverlay is NULL! Make sure fx:id='formOverlay' exists in FXML");
        }
    }

    public void setFormOverlay(VBox overlay) {
        this.formOverlay = overlay;
        showOverlay(true);
        System.out.println("formOverlay set from AdminController");
    }
}