package com.faguaslandia.launcher;

import com.faguaslandia.launcher.service.AuthService;
import com.faguaslandia.launcher.service.DownloaderService;
import com.faguaslandia.launcher.view.LoginView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;

public class LauncherApp extends Application {

    private AuthService authService;
    private DownloaderService downloader;

    @Override
    public void start(Stage stage) {
        authService = new AuthService();
        downloader = new DownloaderService();

        LoginView loginView = new LoginView();

        VBox root = new VBox(10);
        root.getChildren().add(loginView);

        // Botón para instalar juego (aparece después del login correcto)
        Button instalarBtn = new Button("Instalar Juego");
        instalarBtn.setVisible(false); // invisible hasta login

        root.getChildren().add(instalarBtn);

        loginView.getLoginBtn().setOnAction(e -> {
            String email = loginView.getUsuario().getText();
            String pass = loginView.getPassword().getText();

            if (email.isEmpty() || pass.isEmpty()) {
                loginView.getMensaje().setText("Rellena todos los campos");
                return;
            }

            loginView.getMensaje().setText("Conectando...");

            new Thread(() -> {
                boolean ok = authService.login(email, pass);

                Platform.runLater(() -> {
                    if (ok) {
                        loginView.getMensaje().setText("Login correcto ✔");
                        instalarBtn.setVisible(true); // mostramos botón
                    } else {
                        loginView.getMensaje().setText("Credenciales incorrectas ❌");
                    }
                });
            }).start();
        });

        // Acción de instalar juego
        instalarBtn.setOnAction(e -> {
            new Thread(() -> {
                try {
                    Path juegosFolder = Path.of(System.getProperty("user.home"), "Escritorio", "FaguaslandiaGames");
                    if (!Files.exists(juegosFolder)) Files.createDirectories(juegosFolder);

                    Path zipDestino = juegosFolder.resolve("Smashy Road.zip");
                    String url = "http://10.116.192.57:8081/juegos_desca/Smashy_Road.zip"; // pon la URL real aquí

                    downloader.downloadFile(url, zipDestino);
                    downloader.unzip(zipDestino, juegosFolder);
                    Files.deleteIfExists(zipDestino);

                    Platform.runLater(() -> loginView.getMensaje().setText("Juego instalado en: " + juegosFolder));

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> loginView.getMensaje().setText("Error descargando el juego"));
                }
            }).start();
        });

        Scene scene = new Scene(root, 400, 350);
        stage.setTitle("Faguaslandia Launcher");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
