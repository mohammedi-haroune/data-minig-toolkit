package com.usthb.dmtk;

import com.usthb.dmtk.controllers.HomeContorller;
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
        HomeContorller homeContorller = new HomeContorller();
        homeLoader.setController(homeContorller);
        BorderPane mainPane = homeLoader.load();

        //new JMetro(JMetro.Style.LIGHT).applyTheme(mainPane);
        //new JMetro(JMetro.Style.LIGHT).applyTheme(dataset);

        primaryStage.setMaximized(true);
        primaryStage.setTitle("Data Mining Tool");
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }
}