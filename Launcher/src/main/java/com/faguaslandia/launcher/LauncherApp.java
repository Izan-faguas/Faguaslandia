package com.faguaslandia.launcher;

import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.faguaslandia.launcher.view.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class LauncherApp extends Application {

    private AuthService authService;
    private Scene scene;

    private static String juegoAutoLanzar = null;

    public static void main(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--launch".equals(args[i])) {
                juegoAutoLanzar = args[i + 1];
                break;
            }
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        authService = new AuthService();

        scene = new Scene(new StackPane(), 1200, 760);
        scene.getStylesheets().add(
                getClass().getResource("/styles/index.css").toExternalForm()
        );

        stage.setTitle("Faguáslandia");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(true);
        stage.show();

        mostrarCargando("Iniciando sesión...");

        new Thread(() -> {
            Usuario usuario = authService.loginAutomatico();
            Platform.runLater(() -> {
                if (usuario != null) {
                    arrancarHeartbeat();
                    mostrarLauncher(usuario);
                } else {
                    mostrarLogin();
                }
            });
        }).start();
    }


    private void mostrarCargando(String mensaje) {
        VBox cargando = new VBox(16);
        cargando.setAlignment(Pos.CENTER);
        cargando.setStyle("-fx-background-color: #0b1118;");

        Label logo = new Label("Faguáslandia");
        logo.getStyleClass().add("login-title");

        Label msg = new Label(mensaje);
        msg.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 13px;");

        cargando.getChildren().addAll(logo, msg);
        scene.setRoot(cargando);
    }


    private void mostrarLogin() {
        LoginView loginView = new LoginView();
        scene.setRoot(loginView);

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
                            arrancarHeartbeat();
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

        loginView.getPassword().setOnAction(e -> loginView.getLoginBtn().fire());
        loginView.getUsuario().setOnAction(e  -> loginView.getPassword().requestFocus());
    }


    private void mostrarLauncher(Usuario usuario) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0b1118;");

        HeaderView header = new HeaderView();
        root.setTop(header);

        BibliotecaView  bibliotecaView  = new BibliotecaView(usuario.getId());
        TiendaView      tiendaView      = new TiendaView(usuario);
        JuegoDetailView juegoDetailView = new JuegoDetailView(usuario);
        AmigosView      amigosView      = new AmigosView(usuario);
        PerfilView      perfilView      = new PerfilView(usuario);

        juegoDetailView.setCallbackActualizarBiblioteca(bibliotecaView::actualizarBiblioteca);

        bibliotecaView.setCallbackAbrirChat(amigoId -> {
            header.setBadgeAmigos(0);
            header.activarAmigos();
            amigosView.cargarDatos();
            root.setCenter(amigosView);
            Platform.runLater(() -> amigosView.abrirChatPorId(amigoId));
        });

        juegoDetailView.setCallbackVolver(() -> root.setCenter(tiendaView));

        tiendaView.setCallbackJuegoDetalle(j -> {
            juegoDetailView.setJuego(j);
            root.setCenter(juegoDetailView);
        });

        perfilView.setOnLogout(() -> {
            authService.logout();
            mostrarLogin();
        });

        ObjectMapper _mapper = new ObjectMapper();
        Thread badgeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30_000);
                    String urlAmigos = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/amigos";
                    HttpResponse<String> respAmigos = AuthService.getClient().send(
                            HttpRequest.newBuilder().uri(URI.create(urlAmigos)).GET().build(),
                            HttpResponse.BodyHandlers.ofString()
                    );
                    if (respAmigos.statusCode() != 200) continue;

                    JsonNode relaciones = _mapper.readTree(respAmigos.body());
                    long totalNoLeidos = 0;

                    for (JsonNode rel : relaciones) {
                        if (!"aceptado".equals(rel.path("estado").asText())) continue;
                        JsonNode u1 = rel.path("usuario1");
                        JsonNode u2 = rel.path("usuario2");
                        long amigoId = u1.path("id").asLong() == usuario.getId()
                                ? u2.path("id").asLong()
                                : u1.path("id").asLong();

                        String urlNL = Config.API_BASE_URL + "/mensajes/no-leidos/"
                                + usuario.getId() + "/de/" + amigoId;
                        HttpResponse<String> respNL = AuthService.getClient().send(
                                HttpRequest.newBuilder().uri(URI.create(urlNL)).GET().build(),
                                HttpResponse.BodyHandlers.ofString()
                        );
                        if (respNL.statusCode() == 200) {
                            totalNoLeidos += _mapper.readTree(respNL.body()).path("total").asLong();
                        }
                    }

                    final long total = totalNoLeidos;
                    Platform.runLater(() -> header.setBadgeAmigos((int) total));

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        badgeThread.setDaemon(true);
        badgeThread.start();

        header.setActions(
                () -> {
                    perfilView.detenerPollingLogros();
                    root.setCenter(bibliotecaView.getView());
                },
                () -> {
                    perfilView.detenerPollingLogros();
                    root.setCenter(tiendaView);
                },
                () -> {
                    perfilView.detenerPollingLogros();
                    header.setBadgeAmigos(0);
                    amigosView.cargarDatos();
                    root.setCenter(amigosView);
                },
                () -> {
                    perfilView.refrescar();
                    perfilView.iniciarPollingLogros();
                    root.setCenter(perfilView);
                }
        );

        root.setCenter(bibliotecaView.getView());
        scene.setRoot(root);

        if (juegoAutoLanzar != null) {
            String nombreJuego = juegoAutoLanzar;
            juegoAutoLanzar = null;
            Platform.runLater(() -> bibliotecaView.lanzarJuegoPorNombre(nombreJuego));
        }
    }

    private void arrancarHeartbeat() {
        Thread heartbeat = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000);
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(Config.API_BASE_URL + "/presencia/heartbeat"))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    AuthService.getClient().send(req, HttpResponse.BodyHandlers.discarding());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        heartbeat.setDaemon(true);
        heartbeat.start();
    }
}