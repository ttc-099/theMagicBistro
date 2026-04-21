module com.example.projBistro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;

    opens com.files.projBistro.models.models to javafx.base;
    opens com.files.projBistro.models.controllers to javafx.fxml;

    exports com.files.projBistro.models.models;
    exports com.files.projBistro.models.controllers;
    exports com.files.projBistro.models.controllers.admin;
    opens com.files.projBistro.models.controllers.admin to javafx.fxml;
    exports com.files.projBistro.models;
    opens com.files.projBistro.models to javafx.base;
}