module com.example.projBistro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    // added this (26/3) to ensure SQL can run
    requires java.sql;

    // opens controllers so JavaFX can link to the buttons/lists
    opens com.files.projBistro.controllers to javafx.fxml;

    // opens the root of the module so it can find menuView.fxml
    opens com.files.projBistro to javafx.fxml;

    exports com.files.projBistro;
}