package com.saska.stabsputnikapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class HeadApp extends Application {

    public static final String FILE = "src/main/resources/txt/SerialReceive.txt";
    public static final String FILESET = "src/main/resources/txt/ReceiveSetpoint.txt";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        String fxmlFile = "/fxml/headFormApp.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));
        stage.getIcons().add(new Image("/images/planets_Earth.jpg"));
        stage.setResizable(false);
        stage.setTitle("SUBTERMINAL");
        stage.setScene(new Scene(root));
        stage.show();

        stage.setOnCloseRequest(e -> {
            PrintWriter writerData = null;
            PrintWriter writerSet = null;
            try {
                writerData = new PrintWriter(FILE);
                writerSet = new PrintWriter(FILESET);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            assert writerData != null;
            assert writerSet != null;
            writerData.print("");
            writerSet.print("");
            writerData.close();
            writerSet.close();
            Platform.exit();
            System.exit(0);
        });
    }
}
