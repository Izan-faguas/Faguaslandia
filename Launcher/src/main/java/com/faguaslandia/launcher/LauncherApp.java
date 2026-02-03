package com.faguaslandia.launcher;

import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.faguaslandia.launcher.service.DownloaderService;
import com.faguaslandia.launcher.view.BibliotecaView;
import com.faguaslandia.launcher.view.LoginView;
import com.faguaslandia.launcher.view.TiendaView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.tree.DefaultTreeModel;
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
                try {
                    Usuario usuario = authService.login(email, pass); // devuelve Usuario o null
                    Platform.runLater(() -> {
                        if (usuario != null) {
                            // Creamos las vistas **después de loguearse**
                            BibliotecaView bibliotecaView = new BibliotecaView(usuario);
                            TiendaView tiendaView = new TiendaView(usuario);

                            // Configuramos los menús para cambiar de pantalla
                            bibliotecaView.setMenuActions(
                                    () -> scene.setRoot(bibliotecaView),
                                    () -> scene.setRoot(tiendaView),
                                    () -> System.out.println("Perfil (pendiente)")
                            );

                            tiendaView.setMenuActions(
                                    () -> scene.setRoot(bibliotecaView),
                                    () -> scene.setRoot(tiendaView),
                                    () -> System.out.println("Perfil (pendiente)")
                            );

                            // Mostramos la biblioteca al iniciar sesión
                            scene.setRoot(bibliotecaView);
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

        this.scene = new Scene(root);
        this.scene.getStylesheets().add(
                getClass().getResource("/styles/index.css").toExternalForm()
        );
        stage.setTitle("Faguaslandia Launcher");
        stage.setScene(this.scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
