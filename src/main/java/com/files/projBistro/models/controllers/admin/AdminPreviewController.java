package com.files.projBistro.models.controllers.admin;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.dao.AdminDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.function.Consumer;

public class AdminPreviewController {

    private TabPane previewTabPane;
    private AdminDAO adminDAO;
    private Consumer<String> showStatus;

    public void init(AdminDAO adminDAO, TabPane previewTabPane, Label statusLabel, Consumer<String> showStatus) {
        this.adminDAO = adminDAO;
        this.previewTabPane = previewTabPane;
        this.showStatus = showStatus;
    }

    public void refreshPreview() {
        System.out.println("=== Refreshing menu preview ===");
        previewTabPane.getTabs().clear();

        String[] characters = {"Chloe", "Mimi", "Metsu", "Laniard"};

        for (String character : characters) {
            // only get active items for this character
            List<FoodItem> activeItems = adminDAO.getActiveItemsByCharacter(character);

            Tab tab = new Tab(character);
            tab.setClosable(false);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #f5f5f5; -fx-background: #f5f5f5;");

            VBox contentBox = new VBox(15);
            contentBox.setPadding(new Insets(15));
            contentBox.setStyle("-fx-background-color: #f5f5f5;");

            if (activeItems.isEmpty()) {
                Label emptyLabel = new Label("No active items for " + character);
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
                contentBox.getChildren().add(emptyLabel);
            } else {
                // use TilePane to arrange cards in a grid
                javafx.scene.layout.TilePane tilePane = new javafx.scene.layout.TilePane();
                tilePane.setHgap(20);
                tilePane.setVgap(20);
                tilePane.setPrefColumns(3);
                tilePane.setStyle("-fx-padding: 10;");

                for (FoodItem item : activeItems) {
                    VBox card = createPreviewCard(item);
                    tilePane.getChildren().add(card);
                }
                contentBox.getChildren().add(tilePane);
            }

            scrollPane.setContent(contentBox);
            tab.setContent(scrollPane);
            previewTabPane.getTabs().add(tab);
        }

        showStatus.accept("Menu preview refreshed!");
    }

    // creates a card showing item image, name, price, and stock status
    private VBox createPreviewCard(FoodItem item) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(200);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        // try to load image from resource path
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                java.net.URL resource = getClass().getResource(item.getImagePath());
                if (resource != null) {
                    String imageUrl = resource.toExternalForm();
                    imageView.setImage(new Image(imageUrl));
                } else {
                    // show nothing
                    imageView.setImage(null);
                    System.out.println("Image not found: " + item.getImagePath());
                }
            } catch (Exception e) {
                imageView.setImage(null);
            }
        }

        if (imageView.getImage() == null) {
            imageView.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7;");
        }

        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setMinHeight(110);
        imageContainer.getChildren().add(imageView);

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        nameLabel.setAlignment(Pos.CENTER);

        String stockStatus = getStockStatusForPreview(item.getStock());
        Label typeLabel = new Label(item.getItemType() + " • " + stockStatus);
        typeLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        Label priceLabel = new Label(String.format("RM%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #27ae60;");

        Label statusLabelPreview = new Label("✓ active");
        statusLabelPreview.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-color: #e8f8f5; -fx-background-radius: 10;");

        card.getChildren().addAll(imageContainer, nameLabel, typeLabel, priceLabel, statusLabelPreview);
        return card;
    }

    // returns text based on stock level for display on card
    private String getStockStatusForPreview(int stock) {
        if (stock > 50) return "In stock";
        if (stock > 10) return "Low stock";
        if (stock > 0) return "Limited!";
        return "Out of stock";
    }
}