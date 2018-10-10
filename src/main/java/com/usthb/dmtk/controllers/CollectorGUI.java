package com.usthb.dmtk.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class CollectorGUI {
    @FXML
    private
    TextField x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, x5, y5, z5, x6, y6, z6, x7, y7, z7, x8, y8, z8, x9, y9, z9, x10, y10, z10, x11, y11, z11;

    @FXML
    TextField num;

    @FXML
    private
    TextField c;

    @FXML
    private HBox images;

    @FXML
    public ProgressIndicator fistIndicator;
    @FXML
    public ProgressIndicator stopIndicator;
    @FXML
    public ProgressIndicator point1Indicator;
    @FXML
    public ProgressIndicator point2Indicator;
    @FXML
    public ProgressIndicator grabIndicator;
    @FXML
    public Button predict;
    @FXML
    public Button send;

    @FXML
    void predict() {
    }

    @FXML
    public void updateProgressBars(double[] out) {
        fistIndicator.setProgress(out[0]);
        stopIndicator.setProgress(out[1]);
        point1Indicator.setProgress(out[2]);
        point2Indicator.setProgress(out[3]);
        grabIndicator.setProgress(out[4]);
        send.setDisable(false);
    }

    public void predictorNotFound() {
        showPopup(Alert.AlertType.WARNING, "Predictor not found", "you can start it using :",  "ai-player predictor-app <python-path>");
    }

    public void playerNotFound() {
        showPopup(Alert.AlertType.WARNING, "Player not found", "you can start it using :", "ai-player player-app <video-path>");
    }

    public void noCommandFound() {
        showPopup(Alert.AlertType.ERROR, "No action found", "No action found for the given gesture", "you can configure one using the configuration application");
    }

    public void showPopup(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showPopup(Alert.AlertType type, String title, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}