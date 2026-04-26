package com.files.projBistro.models;

import com.files.projBistro.models.database.setUpDB;
import com.files.projBistro.models.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainView.fxml"));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/main_icon.png"))));

        Scene scene = new Scene(loader.load(), 1280, 800);

        // ✅ Get controller and pass the scene
        MainController mainController = loader.getController();
        mainController.setMainScene(scene);

        stage.setTitle("Camogear Bistro");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        setUpDB bootloader = new setUpDB();
        bootloader.initializeDatabase();
        launch(args);
    }
}