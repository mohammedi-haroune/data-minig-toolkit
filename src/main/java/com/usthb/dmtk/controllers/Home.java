package com.usthb.dmtk.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;

public class Home {
    @FXML BorderPane mainPane;

    @FXML Pane instancesPane;

    @FXML Pane attributesPane;

    public StringProperty path = new SimpleStringProperty(null);

    InstanceController instanceController;

    @FXML
    public void initialize() {
        path.addListener((observable, oldValue, newValue) -> {
            try {
                instanceController.loadInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void open() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("data"));
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Instances files", "*.arff");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showOpenDialog(null);

        if (file != null) {
            path.setValue(file.getPath());
        }
    }
}
