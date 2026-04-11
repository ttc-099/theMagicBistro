package com.files.projBistro.controllers;

// import necessary models
import com.files.projBistro.models.FoodItem;
import com.files.projBistro.models.Order;
import com.files.projBistro.models.ThemeManager;
import com.files.projBistro.models.userModel.User;
import com.files.projBistro.models.dao.UserDAO;

// import necessary libraries
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.TilePane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.IOException;

// start
public class MenuController {

    // import FXML
    @FXML private TilePane chloeGrid;
    @FXML private ListView<FoodItem> cartListView;
    @FXML private Label totalLabel;

    // call FXML (ref: menuView.fxml)
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Button loginBtn;

    private Order currentOrder;

    // dao
    private UserDAO userDAO = new UserDAO();

    @FXML
    // throw in a sample unit
    public void initialize() {
        currentOrder = new Order(1, "Guest");
        FoodItem testItem = new FoodItem.FoodItemBuilder()
                .setName("Nitro Nachos")
                .setPrice(8.50)
                .build();
        Button foodButton = new Button(testItem.getName() + "\n£" + testItem.getPrice());
        foodButton.setPrefSize(100, 100);
        foodButton.setOnAction(e -> addItemToCart(testItem));
        chloeGrid.getChildren().add(foodButton);
    }

    // add to cart
    private void addItemToCart(FoodItem item) {
        currentOrder.addItem(item);
        cartListView.getItems().setAll(currentOrder.getItems());
        totalLabel.setText(String.format("Total: £%.2f", currentOrder.getTotalPrice()));
    }

    @FXML
    private void handleAddItem() {
        // Not needed for now since buttons directly add items
    }

    // login
    @FXML
    private void handleLogin() {
        User loggedInUser = userDAO.verifyLogin(
                userField.getText(),
                passField.getText()
        );

        if (loggedInUser != null) {
            String fxmlFile;

            if (loggedInUser.getRole().equalsIgnoreCase("Admin")) {
                fxmlFile = "/adminView.fxml";
            } else {
                fxmlFile = "/menuView.fxml";
            }

            try {
                Stage stage = (Stage) loginBtn.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(getClass().getResource(ThemeManager.getTheme()).toExternalForm());
                stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 🔧 Optional: feedback for failed login
            System.out.println("Login failed");
        }
    }

    // settings
    @FXML
    private void handleOpenSettings(ActionEvent event) throws IOException {
        // ping test
        System.out.println("Opening settings with theme: " + ThemeManager.getTheme());

        // load the fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/settingsView.fxml"));

        // get stage after button click event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // create new scene (only once!)
        Scene scene = new Scene(loader.load(), 450, 350);

        // attach the current theme from our manager
        String themePath = ThemeManager.getTheme();
        if (themePath != null) {
            scene.getStylesheets().add(getClass().getResource(themePath).toExternalForm());
        }
        stage.setScene(scene);
        stage.setTitle("Camo-Bistro | Settings");
        stage.show();
    }
}