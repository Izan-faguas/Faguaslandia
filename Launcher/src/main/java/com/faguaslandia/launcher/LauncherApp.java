package com.faguaslandia.launcher;

import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.faguaslandia.launcher.service.DownloaderService;
import com.faguaslandia.launcher.view.BibliotecaView;
import com.faguaslandia.launcher.view.JuegoDetailView;
import com.faguaslandia.launcher.view.LoginView;
import com.faguaslandia.launcher.view.TiendaView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;

public class LauncherApp extends Application {

    private AuthService authService;
    private DownloaderService downloader;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        authService = new AuthService();
        downloader = new DownloaderService();

        // ===== LOGIN =====
        LoginView loginView = new LoginView();
        VBox loginRoot = new VBox(10);
        loginRoot.getChildren().add(loginView);

        Button instalarBtn = new Button("Instalar Juego");
        instalarBtn.setVisible(false);
        loginRoot.getChildren().add(instalarBtn);

        scene = new Scene(loginRoot, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/index.css").toExternalForm());

        stage.setTitle("Faguaslandia Launcher");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        // LOGIN
        loginView.getLoginBtn().setOnAction(e -> {
            String email = loginView.getUsuario().getText();
            String pass = loginView.getPassword().getText();

            if (email.isEmpty() || pass.isEmpty()) {
                loginView.getMensaje().setText("Rellena todos los campos");
                return;
            }

            loginView.getMensaje().setText("Conectando...");

            new Thread(() -> {
                try {
                    Usuario usuario = authService.login(email, pass);
                    Platform.runLater(() -> {
                        if (usuario != null) {
                            mostrarLauncher(usuario);
                        } else {
                            loginView.getMensaje().setText("Credenciales incorrectas ❌");
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> loginView.getMensaje().setText("Error conectando al servidor"));
                }
            }).start();
        });
    }

    private void mostrarLauncher(Usuario usuario) {
        BorderPane root = new BorderPane();

        BibliotecaView bibliotecaView = new BibliotecaView(usuario);
        TiendaView tiendaView = new TiendaView(usuario);
        JuegoDetailView juegoDetailView = new JuegoDetailView(usuario);

        // Actualizar biblioteca cuando se compra
        juegoDetailView.setCallbackActualizarBiblioteca(() -> bibliotecaView.actualizarBiblioteca());

        // Callback tienda → detalle
        tiendaView.setCallbackJuegoDetalle(j -> {
            juegoDetailView.setJuego(j);
            root.setCenter(juegoDetailView);
        });

        // Volver de detalle a tienda
        juegoDetailView.getVolverBtn().setOnAction(e -> root.setCenter(tiendaView));

        // Mostrar biblioteca al inicio
        root.setCenter(bibliotecaView);





        scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch();
    }
}
