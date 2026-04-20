module com.example.projBistro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;

    opens com.files.projBistro.models to javafx.base;
    opens com.files.projBistro.controllers to javafx.fxml;

    exports com.files.projBistro;
    exports com.files.projBistro.controllers;
    exports com.files.projBistro.models;
    exports com.files.projBistro.controllers.admin;
    opens com.files.projBistro.controllers.admin to javafx.fxml;
}