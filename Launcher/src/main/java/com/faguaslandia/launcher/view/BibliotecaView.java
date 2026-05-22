package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.service.GameInstallerService;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public class BibliotecaView {

    private final JuegoService juegoService = new JuegoService();
    private final GameInstallerService installer = new GameInstallerService();
    private final Long usuarioId;

    private HBox root;
    private VBox bibliotecaPanel;
    private VBox detallePanel;
    private VBox amigosPanel;
    private VBox juegosContainer;

    private StackPane selectedCard;

    private Runnable callbackIrAmigos;
    private java.util.function.Consumer<Long> callbackAbrirChat;

    public void setCallbackIrAmigos(Runnable r) { this.callbackIrAmigos = r; }
    public void setCallbackAbrirChat(java.util.function.Consumer<Long> c) { this.callbackAbrirChat = c; }

    public BibliotecaView(Long usuarioId) {
        this.usuarioId = usuarioId;
        crearVista();
        cargarBiblioteca();
    }

    public HBox getView() { return root; }

    private void crearVista() {
        root = new HBox();
        root.getStyleClass().add("root");

        bibliotecaPanel = new VBox(12);
        bibliotecaPanel.getStyleClass().add("panel-left");

        Label tituloLib = new Label("🎮 Biblioteca");
        tituloLib.getStyleClass().add("panel-left-title");
        bibliotecaPanel.getChildren().add(tituloLib);

        detallePanel = new VBox();
        detallePanel.getStyleClass().add("panel-center");
        HBox.setHgrow(detallePanel, Priority.ALWAYS);

        Label placeholder = new Label("Selecciona un juego");
        placeholder.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 14px;");
        placeholder.setPadding(new Insets(40));
        detallePanel.getChildren().add(placeholder);

        amigosPanel = new VBox(4);
        amigosPanel.getStyleClass().add("panel-right");

        Label tituloAmigos = new Label("👥 Amigos");
        tituloAmigos.getStyleClass().add("amigos-titulo");
        amigosPanel.getChildren().add(tituloAmigos);

        cargarAmigosPanel();

        root.getChildren().addAll(bibliotecaPanel, detallePanel, amigosPanel);
    }

    private void cargarAmigosPanel() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuarioId + "/amigos";
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder().uri(java.net.URI.create(url)).GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                );
                if (resp.statusCode() != 200) return;

                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();

                var relaciones = mapper.readValue(
                        resp.body(),
                        new com.fasterxml.jackson.core.type.TypeReference<
                                java.util.List<com.faguaslandia.launcher.view.AmigosView.AmigoRelacionDTO>
                                >() {}
                );

                var filas = relaciones.stream()
                        .filter(r -> "aceptado".equals(r.estado))
                        .map(r -> r.usuario1.id.equals(usuarioId) ? r.usuario2 : r.usuario1)
                        .map(otro -> {
                            String dotColor = switch (otro.estado == null ? "offline" : otro.estado) {
                                case "online"      -> "#4caf50";
                                case "ausente"     -> "#f9a825";
                                case "no_molestar" -> "#e53935";
                                default            -> "#5b7a99";
                            };

                            Label dot = new Label("●");
                            dot.setStyle("-fx-text-fill: " + dotColor + "; -fx-font-size: 10px;");

                            Label nombre = new Label(otro.nombre);
                            nombre.setStyle("-fx-text-fill: #e6f0f8; -fx-font-size: 13px;");

                            HBox fila = new HBox(8, dot, nombre);
                            fila.setAlignment(Pos.CENTER_LEFT);
                            fila.setPadding(new Insets(6, 10, 6, 10));
                            fila.setStyle("-fx-cursor: hand; -fx-background-radius: 6;");
                            fila.setOnMouseEntered(e -> fila.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 6;"));
                            fila.setOnMouseExited(e -> fila.setStyle("-fx-background-radius: 6;"));

                            Long amigoId = otro.id;

                            fila.setOnMouseClicked(e -> {
                                if (callbackAbrirChat != null) callbackAbrirChat.accept(amigoId);
                            });

                            return fila;
                        }).toList();

                Platform.runLater(() -> {
                    if (filas.isEmpty()) {
                        Label empty = new Label("Sin amigos aún.");
                        empty.getStyleClass().add("amigos-vacio");
                        empty.setPadding(new Insets(16, 10, 0, 10));
                        amigosPanel.getChildren().add(empty);
                    } else {
                        amigosPanel.getChildren().addAll(filas);
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void cargarBiblioteca() {
        try {
            List<Juego> juegos = juegoService.obtenerBiblioteca(usuarioId);

            juegosContainer = new VBox(10);
            juegosContainer.setPadding(new Insets(4, 4, 4, 4));

            for (Juego juego : juegos) {
                StackPane card = crearCard(juego);
                juegosContainer.getChildren().add(card);
            }

            ScrollPane scroll = new ScrollPane(juegosContainer);
            scroll.getStyleClass().add("scroll-pane");
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            VBox.setVgrow(scroll, Priority.ALWAYS);

            bibliotecaPanel.getChildren().add(scroll);

            if (!juegos.isEmpty()) {
                Juego primero = juegos.get(0);
                mostrarJuego(primero);

                Platform.runLater(() -> {
                    if (!juegosContainer.getChildren().isEmpty()) {
                        marcarSeleccion((StackPane) juegosContainer.getChildren().get(0));
                    }
                });
            } else {
                Label empty = new Label("Tu biblioteca está vacía.\nVisita la tienda.");
                empty.setWrapText(true);
                empty.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 12px; -fx-padding: 20 10 0 10; -fx-text-alignment: CENTER;");
                detallePanel.getChildren().setAll(empty);
            }

        } catch (Exception e) {
            e.printStackTrace();
            bibliotecaPanel.getChildren().add(new Label("Error cargando biblioteca"));
        }
    }

    private StackPane crearCard(Juego juego) {
        String url = Config.IMG_BASE_URL + "/" + juego.getImagen_url();
        Image image = new Image(url, true);

        ImageView img = new ImageView(image);
        img.setFitWidth(196);
        img.setFitHeight(72);
        img.setPreserveRatio(false);
        img.setSmooth(true);

        StackPane card = new StackPane(img);
        card.getStyleClass().add("game-card");
        card.setMaxWidth(Double.MAX_VALUE);

        card.setOnMouseClicked(e -> {
            mostrarJuego(juego);
            marcarSeleccion(card);
        });
        card.getProperties().put("titulo", juego.getTitulo());
        card.getProperties().put("juego", juego);
        return card;
    }

    private void mostrarJuego(Juego juego) {
        detallePanel.getChildren().clear();

        String url = Config.IMG_BASE_URL + "/" + juego.getImagen_url();
        ImageView portada = new ImageView(new Image(url, true));
        portada.setFitWidth(900);
        portada.setFitHeight(320);
        portada.setPreserveRatio(false);
        portada.setSmooth(true);
        portada.getStyleClass().add("detalle-img");

        StackPane imgContainer = new StackPane(portada);
        imgContainer.setMaxWidth(Double.MAX_VALUE);
        portada.fitWidthProperty().bind(imgContainer.widthProperty());

        Label titulo = new Label(juego.getTitulo());
        titulo.getStyleClass().add("detalle-titulo");

        Label desc = new Label(juego.getDescripcion() != null ? juego.getDescripcion() : "Sin descripción disponible");
        desc.setWrapText(true);
        desc.getStyleClass().add("detalle-desc");

        HBox meta = new HBox(16);
        if (juego.getDesarrollador() != null) {
            Label dev = new Label("👤 " + juego.getDesarrollador());
            dev.getStyleClass().add("detalle-meta");
            meta.getChildren().add(dev);
        }
        if (juego.getCategoria() != null) {
            Label cat = new Label(juego.getCategoria());
            cat.getStyleClass().add("tag");
            meta.getChildren().add(cat);
        }

        // ── Botones ──────────────────────────────────────────
        Button jugar = new Button("▶  JUGAR");
        jugar.getStyleClass().add("btn-play");

        Button actualizar = new Button("🔄  ACTUALIZAR");
        actualizar.getStyleClass().add("btn-secondary");

        String gameName   = juego.getTitulo().replace(" ", "_");
        String downloadUrl = Config.API_BASE_URL + "/juegos/download/" + juego.getId();

        jugar.setOnAction(e -> {
            jugar.setDisable(true);
            jugar.setText("⏳  Cargando...");
            new Thread(() -> {
                try {
                    if (!installer.isInstalled(gameName)) {
                        Platform.runLater(() -> jugar.setText("⬇  Instalando..."));
                        installer.install(gameName, downloadUrl);
                    }
                    Platform.runLater(() -> {
                        jugar.setDisable(false);
                        jugar.setText("▶  JUGAR");
                        installer.launch(gameName, usuarioId, juego.getId());
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> { jugar.setDisable(false); jugar.setText("▶  JUGAR"); });
                }
            }).start();
        });

        actualizar.setOnAction(e -> {
            actualizar.setDisable(true);
            actualizar.setText("⏳  Actualizando...");
            new Thread(() -> {
                try {
                    installer.install(gameName, downloadUrl);
                    Platform.runLater(() -> { actualizar.setDisable(false); actualizar.setText("✅  Actualizado"); });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> { actualizar.setDisable(false); actualizar.setText("🔄  ACTUALIZAR"); });
                }
            }).start();
        });

        HBox botonesBox = new HBox(12, jugar, actualizar);
        botonesBox.setAlignment(Pos.CENTER_LEFT);

        // ── Secciones inferiores ─────────────────────────────
        VBox secActualizaciones = crearSeccionActualizaciones(juego.getId());
        VBox secLogros          = crearSeccionLogros(juego.getId());
        VBox secAmigos          = crearSeccionAmigosJuego(juego.getId());

        HBox dosColumnas = new HBox(20, secActualizaciones, secAmigos);
        HBox.setHgrow(secActualizaciones, Priority.ALWAYS);
        HBox.setHgrow(secAmigos, Priority.ALWAYS);
        dosColumnas.setPadding(new Insets(0, 30, 20, 30));

        VBox info = new VBox(14, titulo, desc, meta, botonesBox, secLogros, dosColumnas);
        info.getStyleClass().add("detalle-info");
        info.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(new VBox(imgContainer, info));
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        detallePanel.getChildren().add(scroll);
        VBox.setVgrow(detallePanel, Priority.ALWAYS);
    }

    private void marcarSeleccion(StackPane card) {
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("game-card-selected");
        }
        selectedCard = card;
        card.getStyleClass().add("game-card-selected");
    }

    public void actualizarBiblioteca() {
        bibliotecaPanel.getChildren().clear();
        Label tituloLib = new Label("🎮 Biblioteca");
        tituloLib.getStyleClass().add("panel-left-title");
        bibliotecaPanel.getChildren().add(tituloLib);
        detallePanel.getChildren().clear();
        selectedCard = null;
        cargarBiblioteca();
    }

    /**
     * Lanza un juego por nombre. Usado desde LauncherApp cuando se arranca
     * el launcher con el argumento --launch NombreJuego desde un acceso directo.
     */
    public void lanzarJuegoPorNombre(String nombre) {
        if (juegosContainer == null) return;
        // Buscar el juego en la lista cargada
        juegosContainer.getChildren().forEach(node -> {
            if (node instanceof StackPane card) {
                // El título está guardado en las propiedades del nodo
                Object tituloObj = card.getProperties().get("titulo");
                if (tituloObj != null && nombre.equals(tituloObj.toString().replace(" ", "_"))) {
                    mostrarJuego((Juego) card.getProperties().get("juego"));
                    marcarSeleccion(card);
                }
            }
        });

        // Lanzar en background
        new Thread(() -> {
            try {
                String gameName = nombre;
                String downloadUrl = null; // se resuelve en install si hace falta
                if (!installer.isInstalled(gameName)) return; // si no está instalado no lanzar
                Platform.runLater(() -> installer.launch(gameName));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    // ── SECCIÓN ACTUALIZACIONES ───────────────────────────────
    private VBox crearSeccionActualizaciones(Long juegoId) {
        VBox sec = new VBox(8);
        sec.setPadding(new Insets(16, 0, 0, 0));

        Label titulo = new Label("📋 ACTUALIZACIONES");
        titulo.getStyleClass().add("amigos-section-title");

        VBox lista = new VBox(8);
        Label cargando = new Label("Cargando...");
        cargando.getStyleClass().add("amigos-vacio");
        lista.getChildren().add(cargando);

        sec.getChildren().addAll(titulo, lista);

        new Thread(() -> {
            try {
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(Config.API_BASE_URL + "/juegos/" + juegoId + "/actualizaciones"))
                                .GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var acts   = mapper.readValue(resp.body(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {});

                Platform.runLater(() -> {
                    lista.getChildren().clear();
                    if (acts.isEmpty()) {
                        Label empty = new Label("Sin actualizaciones aún.");
                        empty.getStyleClass().add("amigos-vacio");
                        lista.getChildren().add(empty);
                    } else {
                        for (var a : acts) {
                            Label lTitulo = new Label("🔹 " + a.get("titulo"));
                            lTitulo.setStyle("-fx-text-fill: #e6f0f8; -fx-font-size: 12px; -fx-font-weight: bold;");
                            Label lFecha = new Label(a.get("fecha").toString());
                            lFecha.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 11px;");
                            Label lDesc = new Label(a.get("descripcion").toString());
                            lDesc.setStyle("-fx-text-fill: #9fb3c8; -fx-font-size: 12px;");
                            lDesc.setWrapText(true);
                            VBox item = new VBox(3, new javafx.scene.layout.HBox(10, lTitulo, lFecha), lDesc);
                            item.getStyleClass().add("perfil-card");
                            item.setPadding(new Insets(10));
                            lista.getChildren().add(item);
                        }
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        return sec;
    }

    // ── SECCIÓN LOGROS ────────────────────────────────────────
    private VBox crearSeccionLogros(Long juegoId) {
        VBox sec = new VBox(8);
        sec.setPadding(new Insets(16, 30, 0, 30));

        Label titulo = new Label("🏆 LOGROS");
        titulo.getStyleClass().add("amigos-section-title");

        javafx.scene.layout.HBox iconos = new javafx.scene.layout.HBox(6);
        iconos.setAlignment(Pos.CENTER_LEFT);

        Label txt = new Label("Cargando...");
        txt.getStyleClass().add("amigos-vacio");

        sec.getChildren().addAll(titulo, iconos, txt);

        new Thread(() -> {
            try {
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(Config.API_BASE_URL + "/juegos/" + juegoId + "/logros"))
                                .GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var logros = mapper.readValue(resp.body(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {});

                Platform.runLater(() -> {
                    iconos.getChildren().clear();
                    txt.setText("");

                    if (logros.isEmpty()) {
                        txt.setText("Sin logros para este juego.");
                        return;
                    }

                    long desbloqueados = logros.stream().filter(l -> Boolean.TRUE.equals(l.get("desbloqueado"))).count();

                    for (var l : logros) {
                        boolean bloqueado = !Boolean.TRUE.equals(l.get("desbloqueado"));
                        Label icono = new Label(l.get("icono") != null ? l.get("icono").toString() : "🏆");
                        icono.setStyle("-fx-font-size: 22px; -fx-opacity: " + (bloqueado ? "0.3" : "1.0") + ";");
                        icono.setTooltip(new javafx.scene.control.Tooltip(l.get("nombre") + ": " + l.get("descripcion")));
                        iconos.getChildren().add(icono);
                    }

                    txt.setText(desbloqueados + " / " + logros.size() + " logros desbloqueados");
                    txt.setStyle("-fx-text-fill: #9fb3c8; -fx-font-size: 12px;");
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        return sec;
    }

    // ── SECCIÓN AMIGOS CON EL JUEGO ───────────────────────────
    private VBox crearSeccionAmigosJuego(Long juegoId) {
        VBox sec = new VBox(8);
        sec.setPadding(new Insets(16, 0, 0, 0));

        Label titulo = new Label("👥 AMIGOS CON ESTE JUEGO");
        titulo.getStyleClass().add("amigos-section-title");

        javafx.scene.layout.HBox avatares = new javafx.scene.layout.HBox(8);
        avatares.setAlignment(Pos.CENTER_LEFT);

        Label txt = new Label("Cargando...");
        txt.getStyleClass().add("amigos-vacio");

        sec.getChildren().addAll(titulo, avatares, txt);

        new Thread(() -> {
            try {
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(Config.API_BASE_URL + "/juegos/" + juegoId + "/amigos-con-juego"))
                                .GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var amigos = mapper.readValue(resp.body(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String, Object>>>() {});

                Platform.runLater(() -> {
                    avatares.getChildren().clear();
                    txt.setText("");

                    if (amigos.isEmpty()) {
                        txt.setText("Ningún amigo tiene este juego.");
                        return;
                    }

                    for (var a : amigos) {
                        String nombre = a.get("nombre").toString();
                        String horas  = a.get("horas").toString();

                        Label letra = new Label(nombre.substring(0, 1).toUpperCase());
                        letra.setStyle("-fx-text-fill: #66c0f4; -fx-font-weight: bold; -fx-font-size: 14px;");

                        StackPane av = new StackPane(letra);
                        av.setMinSize(36, 36);
                        av.setMaxSize(36, 36);
                        av.getStyleClass().add("amigo-avatar");
                        javafx.scene.control.Tooltip.install(av, new javafx.scene.control.Tooltip(nombre + " · " + horas + "h jugadas"));

                        // Intentar cargar foto
                        String fotoUrl = Config.IMG_BASE_URL + "/avatars/" + a.get("foto");
                        javafx.scene.image.Image img = new javafx.scene.image.Image(fotoUrl, true);
                        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                        iv.setFitWidth(36); iv.setFitHeight(36);
                        iv.setClip(new javafx.scene.shape.Circle(18, 18, 18));
                        img.errorProperty().addListener((obs, o, err) -> {
                            if (err) Platform.runLater(() -> { av.getChildren().clear(); av.getChildren().add(letra); });
                        });
                        av.getChildren().add(iv);
                        avatares.getChildren().add(av);
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        return sec;
    }
}
