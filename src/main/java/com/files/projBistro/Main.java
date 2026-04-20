package com.files.projBistro;

import com.files.projBistro.database.setUpDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

// done, do not edit further plz :)

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // load login UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginView.fxml"));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/main_icon.png"))));

        // set scene (400 x 500px)
        Scene scene = new Scene(loader.load(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/styles/light.css").toExternalForm());

        stage.setTitle("Camogear Bistro (Login)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // 1. initialize the setup worker
        setUpDB bootloader = new setUpDB();
        // 2. build the tables (if they don't exist)
        bootloader.initializeDatabase();

        launch(args);
    }
}