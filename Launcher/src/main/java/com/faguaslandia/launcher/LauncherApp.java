package com.faguaslandia.launcher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LauncherApp extends Application {

    @Override
    public void start(Stage stage) {
        Label titulo = new Label("Faguaslandia Launcher");
        Button boton = new Button("Hola launcher");

        VBox root = new VBox(15, titulo, boton);
        root.setStyle("-fx-padding: 20");

        stage.setTitle("Faguaslandia");
        stage.setScene(new Scene(root, 300, 150));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
