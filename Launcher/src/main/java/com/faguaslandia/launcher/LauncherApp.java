package com.faguaslandia.launcher;

import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.faguaslandia.launcher.view.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LauncherApp extends Application {

    private AuthService authService;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        authService = new AuthService();

        // ── LOGIN ──
        LoginView loginView = new LoginView();

        scene = new Scene(loginView, 1200, 760);
        scene.getStylesheets().add(
                getClass().getResource("/styles/index.css").toExternalForm()
        );

        stage.setTitle("Faguáslandia");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(true);
        stage.show();

        // ── Acción login ──
        loginView.getLoginBtn().setOnAction(e -> {
            String email = loginView.getUsuario().getText().trim();
            String pass  = loginView.getPassword().getText();

            if (email.isEmpty() || pass.isEmpty()) {
                loginView.getMensaje().setText("Rellena todos los campos");
                return;
            }

            loginView.getMensaje().getStyleClass().remove("login-error");
            loginView.getMensaje().getStyleClass().add("login-ok");
            loginView.getMensaje().setText("Conectando...");
            loginView.getLoginBtn().setDisable(true);

            new Thread(() -> {
                try {
                    Usuario usuario = authService.login(email, pass);
                    Platform.runLater(() -> {
                        loginView.getLoginBtn().setDisable(false);
                        if (usuario != null) {
                            mostrarLauncher(usuario);
                        } else {
                            loginView.getMensaje().getStyleClass().remove("login-ok");
                            loginView.getMensaje().getStyleClass().add("login-error");
                            loginView.getMensaje().setText("Credenciales incorrectas ❌");
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        loginView.getLoginBtn().setDisable(false);
                        loginView.getMensaje().getStyleClass().remove("login-ok");
                        loginView.getMensaje().getStyleClass().add("login-error");
                        loginView.getMensaje().setText("Error conectando al servidor");
                    });
                }
            }).start();
        });

        // Enter en el campo password también lanza login
        loginView.getPassword().setOnAction(e -> loginView.getLoginBtn().fire());
        loginView.getUsuario().setOnAction(e  -> loginView.getPassword().requestFocus());
    }

    private void mostrarLauncher(Usuario usuario) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0b1118;");

        // ── Header ──
        HeaderView header = new HeaderView();
        root.setTop(header);

        // ── Vistas ──
        BibliotecaView bibliotecaView  = new BibliotecaView(usuario.getId());
        TiendaView tiendaView          = new TiendaView(usuario);
        JuegoDetailView juegoDetailView = new JuegoDetailView(usuario);

        // Callbacks cruzados
        juegoDetailView.setCallbackActualizarBiblioteca(() ->
                bibliotecaView.actualizarBiblioteca()
        );
        juegoDetailView.setCallbackVolver(() -> {
            root.setCenter(tiendaView);
        });

        tiendaView.setCallbackJuegoDetalle(j -> {
            juegoDetailView.setJuego(j);
            root.setCenter(juegoDetailView);
        });

        // ── Navegación header ──
        header.setActions(
                () -> root.setCenter(bibliotecaView.getView()),
                () -> root.setCenter(tiendaView),
                () -> { /* perfil — futuro */ }
        );

        // Pantalla inicial: biblioteca
        root.setCenter(bibliotecaView.getView());

        scene.setRoot(root);
    }

    public static void main(String[] args) {
        launch();
    }
}