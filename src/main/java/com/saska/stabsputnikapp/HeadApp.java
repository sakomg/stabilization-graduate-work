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

    public static final String FILE = "src/main/resources/txtfiles/SerialReceive.txt";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        String fxmlFile = "/fxml/headFormApp.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));
        stage.getIcons().add(new Image("/images/planets_Earth.jpg"));

        stage.setTitle("Mini Terminal");
        stage.setScene(new Scene(root));
        stage.show();

        stage.setOnCloseRequest(e -> {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(FILE);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
            assert writer != null;
            writer.print("");
            writer.close();
            Platform.exit();
            System.exit(0);
        });
    }


}
