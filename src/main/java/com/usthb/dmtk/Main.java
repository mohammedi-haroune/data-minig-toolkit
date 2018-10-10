package com.usthb.dmtk;

import com.usthb.dmtk.controllers.Home;
import com.usthb.dmtk.controllers.InstanceController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("/views/home.fxml"));
        Home home = new Home();
        homeLoader.setController(home);
        BorderPane mainPane = homeLoader.load();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/table.fxml"));
        Parent instances = loader.load();
        InstanceController instanceController = loader.getController();
        instanceController.setHome(home);
        mainPane.setLeft(instances);

        primaryStage.setMaximized(true);
        primaryStage.setTitle("Data Mining Tool");
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }
}