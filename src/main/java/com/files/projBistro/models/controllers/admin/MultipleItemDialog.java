package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.models.FoodItem;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MultipleItemDialog {

    private static final String[] VALID_CHARACTERS = {"Chloe", "Mimi", "Metsu", "Laniard"};
    private static final String[] ITEM_TYPES = {"Main", "Appetizer", "Dessert", "Drink", "Special"};

    private List<FoodItemEntry> itemEntries = new ArrayList<>(); // stores all items being added
    private VBox itemsContainer; // holds all the item cards
    private int itemCounter = 0;
    private Stage ownerStage;

    public List<FoodItem> showAndWait(Stage owner) {
        this.ownerStage = owner;

        Dialog<List<FoodItem>> dialog = new Dialog<>();
        dialog.setTitle("Add Multiple Items");
        dialog.setHeaderText("Add multiple items at once");
        dialog.setResizable(true);

        if (owner != null) {
            dialog.initOwner(owner);
        }

        ButtonType submitButton = new ButtonType("Add All Items", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(750);
        content.setPrefHeight(650);

        // scroll area for all item cards
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        itemsContainer = new VBox(15);
        itemsContainer.setPadding(new Insets(10));
        itemsContainer.setStyle("-fx-background-color: transparent;");

        // Add 2 empty item cards by default
        addItemCard();
        addItemCard();

        scrollPane.setContent(itemsContainer);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addCardBtn = new Button("+ Add Another Item");
        addCardBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        addCardBtn.setOnAction(e -> addItemCard());

        Button clearAllBtn = new Button("Clear All");
        clearAllBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16;");
        clearAllBtn.setOnAction(e -> {
            itemsContainer.getChildren().clear();
            itemEntries.clear();
            itemCounter = 0;
            addItemCard();
            addItemCard();
        });

        buttonBox.getChildren().addAll(addCardBtn, clearAllBtn);

        content.getChildren().addAll(scrollPane, buttonBox);
        dialog.getDialogPane().setContent(content);

        // validate all cards before allowing submission
        Node submitBtnNode = dialog.getDialogPane().lookupButton(submitButton);
        submitBtnNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            List<String> errors = new ArrayList<>();
            int index = 1;

            for (FoodItemEntry entry : itemEntries) {
                if (!entry.isValid()) {
                    errors.add("Item " + index + ": Missing required fields (Name, Price, Stock)");
                } else {
                    // Validate name length
                    if (entry.getName().length() > 100) {
                        errors.add("Item " + index + ": Name is too long (max 100 characters)");
                    }
                    // Validate price range
                    if (entry.getPrice() > 9999.99) {
                        errors.add("Item " + index + ": Price is too high (max RM9999.99)");
                    }
                    // Validate stock range
                    if (entry.getStock() > 999999) {
                        errors.add("Item " + index + ": Stock is too high (max 999,999)");
                    }
                }
                index++;
            }

            if (!errors.isEmpty()) {
                event.consume(); // stop submission
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText("Please fix the following issues:");
                StringBuilder errorMsg = new StringBuilder();
                for (String error : errors) {
                    errorMsg.append("• ").append(error).append("\n");
                }
                alert.setContentText(errorMsg.toString());
                alert.showAndWait();
            }
        });

        dialog.setResultConverter(button -> {
            if (button == submitButton) {
                List<FoodItem> items = new ArrayList<>();
                for (FoodItemEntry entry : itemEntries) {
                    if (entry.isValid()) {
                        FoodItem item = entry.build();
                        if (item != null) {
                            items.add(item);
                        }
                    }
                }
                return items.isEmpty() ? null : items;
            }
            return null;
        });

        Optional<List<FoodItem>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // creates a new card for one item
    private void addItemCard() {
        itemCounter++;
        FoodItemEntry entry = new FoodItemEntry();
        itemEntries.add(entry);

        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");

        // header with card number and remove button
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label cardNumber = new Label("Item " + itemCounter);
        cardNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Error label for this card
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        errorLabel.setVisible(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button removeBtn = new Button("✖ Remove");
        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8;");
        removeBtn.setOnAction(e -> {
            itemsContainer.getChildren().remove(card);
            itemEntries.remove(entry);
        });
        headerBox.getChildren().addAll(cardNumber, spacer, removeBtn);

        // form fields for this item
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Item name");
        nameField.setPrefWidth(200);
        nameField.textProperty().addListener((obs, old, val) -> {
            entry.setName(val);
            validateField(nameField, errorLabel, val, "Name required", 100);
        });

        TextField priceField = new TextField();
        priceField.setPromptText("0.00");
        priceField.setPrefWidth(100);
        priceField.textProperty().addListener((obs, old, val) -> {
            if (validateNumericField(priceField, errorLabel, val, "Price", true, 9999.99)) {
                try {
                    entry.setPrice(Double.parseDouble(val));
                } catch (NumberFormatException e) {
                    entry.setPrice(0);
                }
            } else if (val.isEmpty()) {
                entry.setPrice(0);
            }
        });

        TextField stockField = new TextField();
        stockField.setPromptText("0");
        stockField.setPrefWidth(100);
        stockField.textProperty().addListener((obs, old, val) -> {
            if (validateNumericField(stockField, errorLabel, val, "Stock", false, 999999)) {
                try {
                    entry.setStock(Integer.parseInt(val));
                } catch (NumberFormatException e) {
                    entry.setStock(0);
                }
            } else if (val.isEmpty()) {
                entry.setStock(0);
            }
        });

        ComboBox<String> characterBox = new ComboBox<>();
        characterBox.setItems(FXCollections.observableArrayList(VALID_CHARACTERS));
        characterBox.setValue("Chloe");
        characterBox.setPrefWidth(120);
        characterBox.valueProperty().addListener((obs, old, val) -> entry.setCharacter(val));

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.setItems(FXCollections.observableArrayList(ITEM_TYPES));
        typeBox.setValue("Main");
        typeBox.setPrefWidth(100);
        typeBox.valueProperty().addListener((obs, old, val) -> entry.setType(val));

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description (optional)");
        descArea.setPrefRowCount(2);
        descArea.setWrapText(true);
        descArea.textProperty().addListener((obs, old, val) -> entry.setDescription(val));

        // image selection for this specific item
        HBox imageBox = new HBox(10);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        Label imageLabel = new Label("No image selected");
        imageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        imageLabel.setPrefWidth(200);

        Button chooseImageBtn = new Button("Browse Image");
        chooseImageBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8;");
        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome + "/Downloads");
            if (downloadsFolder.exists()) {
                fileChooser.setInitialDirectory(downloadsFolder);
            }
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(ownerStage);
            if (selectedFile != null) {
                imageLabel.setText(selectedFile.getName());
                imageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                entry.setImagePath(selectedFile.getAbsolutePath());
            }
        });

        // info icon saying image is optional
        Label infoIcon = new Label("ⓘ");
        infoIcon.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-cursor: hand;");
        Tooltip tooltip = new Tooltip("You can add an item without adding a picture.\nThe image field is optional.");
        tooltip.setShowDelay(javafx.util.Duration.millis(0));
        tooltip.setShowDuration(javafx.util.Duration.seconds(5));
        infoIcon.setTooltip(tooltip);

        Button clearImageBtn = new Button("Clear");
        clearImageBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 8;");
        clearImageBtn.setOnAction(e -> {
            imageLabel.setText("No image selected");
            imageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            entry.setImagePath(null);
        });

        imageBox.getChildren().addAll(chooseImageBtn, clearImageBtn, infoIcon, imageLabel);

        // layout the grid
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price (RM):"), 2, 0);
        grid.add(priceField, 3, 0);

        grid.add(new Label("Stock:"), 0, 1);
        grid.add(stockField, 1, 1);
        grid.add(new Label("Character:"), 2, 1);
        grid.add(characterBox, 3, 1);

        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeBox, 1, 2);

        grid.add(new Label("Description:"), 0, 3);
        grid.add(descArea, 1, 3, 3, 1);

        grid.add(new Label("Image:"), 0, 4);
        grid.add(imageBox, 1, 4, 3, 1);

        grid.add(errorLabel, 1, 5, 3, 1);

        card.getChildren().addAll(headerBox, grid);
        itemsContainer.getChildren().add(card);
    }


     // validates text fields for empty values and length
    private void validateField(TextField field, Label errorLabel, String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            errorLabel.setText( fieldName + " is required");
            errorLabel.setVisible(true);
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else if (value.length() > maxLength) {
            errorLabel.setText( fieldName + " exceeds " + maxLength + " characters");
            errorLabel.setVisible(true);
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
        } else {
            errorLabel.setVisible(false);
            field.setStyle("");
        }
    }

    // validates price and stock fields for numbers and ranges
    private boolean validateNumericField(TextField field, Label errorLabel, String value, String fieldName, boolean isPrice, double maxValue) {
        if (value == null || value.trim().isEmpty()) {
            errorLabel.setText( fieldName + " is required");
            errorLabel.setVisible(true);
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
            return false;
        }

        // check if it's a valid number
        try {
            double num = Double.parseDouble(value);

            // Check for negative
            if (num < 0) {
                errorLabel.setText( fieldName + " cannot be negative");
                errorLabel.setVisible(true);
                field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
                return false;
            }

            // Check max value
            if (num > maxValue) {
                errorLabel.setText( fieldName + " exceeds maximum (" + (isPrice ? "RM" : "") + maxValue + ")");
                errorLabel.setVisible(true);
                field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
                return false;
            }

            // For stock, check if it's a whole number
            if (!isPrice && num != Math.floor(num)) {
                errorLabel.setText("Stock must be a whole number (no decimals)");
                errorLabel.setVisible(true);
                field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
                return false;
            }

            // Valid input
            errorLabel.setVisible(false);
            field.setStyle("");
            return true;

        } catch (NumberFormatException e) {
            errorLabel.setText("Please enter a valid " + fieldName.toLowerCase() + " (numbers only)");
            errorLabel.setVisible(true);
            field.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3;");
            return false;
        }
    }

    // inner class that holds data for one item before it's added to database
    private static class FoodItemEntry {
        private String name = "";
        private double price = 0;
        private int stock = 0;
        private String character = "Chloe";
        private String type = "Main";
        private String description = "";
        private String imagePath = null;

        public void setName(String n) { this.name = n; }
        public void setPrice(double p) { this.price = p; }
        public void setStock(int s) { this.stock = s; }
        public void setCharacter(String c) { this.character = c; }
        public void setType(String t) { this.type = t; }
        public void setDescription(String d) { this.description = d; }
        public void setImagePath(String path) { this.imagePath = path; }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }

        public boolean isValid() {
            return !name.trim().isEmpty() && price >= 0 && stock >= 0;
        }

        // turns the entry into a real FoodItem object
        public FoodItem build() {
            if (!isValid()) return null;

            String finalImagePath = null;
            if (imagePath != null && !imagePath.isEmpty()) {
                finalImagePath = copyImageToProject(imagePath);
            }

            return new FoodItem.FoodItemBuilder()
                    .setName(name.trim())
                    .setPrice(price)
                    .setStock(stock)
                    .setCharCategory(character)
                    .setItemType(type)
                    .setImagePath(finalImagePath)
                    .setDescription(description)
                    .build();
        }

        // copies image to project folders so it shows up in the app
        private String copyImageToProject(String sourcePath) {
            try {
                File sourceFile = new File(sourcePath);
                String fileName = sourceFile.getName();
                String projectRoot = System.getProperty("user.dir");

                // Copy to both source and target
                String sourceDir = projectRoot + "/src/main/resources/images/";
                File sourceDirFile = new File(sourceDir);
                if (!sourceDirFile.exists()) sourceDirFile.mkdirs();
                Files.copy(Paths.get(sourcePath), Paths.get(sourceDir + fileName), StandardCopyOption.REPLACE_EXISTING);

                String targetDir = projectRoot + "/target/classes/images/";
                File targetDirFile = new File(targetDir);
                if (!targetDirFile.exists()) targetDirFile.mkdirs();
                Files.copy(Paths.get(sourcePath), Paths.get(targetDir + fileName), StandardCopyOption.REPLACE_EXISTING);

                return "/images/" + fileName;
            } catch (IOException e) {
                System.err.println("Failed to copy image: " + e.getMessage());
                return null;
            }
        }
    }
}