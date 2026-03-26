package com.files.projBistro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Initialise Database FIRST (The Engine)

        // 2. Load the UI (The Dashboard)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menuView.fxml"));

        // 3. Set the dimensions
        Scene scene = new Scene(loader.load(), 600, 400);

        stage.setTitle("Bistro Menu");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}