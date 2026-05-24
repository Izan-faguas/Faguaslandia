package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.service.GameInstallerService;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.List;

public class BibliotecaView {

    private final JuegoService juegoService = new JuegoService();
    private final GameInstallerService installer = new GameInstallerService();
    private final Long usuarioId;

    private HBox root;
    private VBox sidebarPanel;
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

    // ════════════════════════════════════════════════════
    //  ESTRUCTURA PRINCIPAL
    // ════════════════════════════════════════════════════

    private void crearVista() {
        root = new HBox();
        root.getStyleClass().add("root");

        // ── Sidebar izquierda ─────────────────────────
        sidebarPanel = new VBox(0);
        sidebarPanel.getStyleClass().add("sidebar");

        Label sidebarLabel = new Label("MIS JUEGOS");
        sidebarLabel.getStyleClass().add("sidebar-section-label");
        sidebarPanel.getChildren().add(sidebarLabel);

        // ── Panel central ─────────────────────────────
        detallePanel = new VBox();
        detallePanel.getStyleClass().add("panel-center");
        HBox.setHgrow(detallePanel, Priority.ALWAYS);

        Label placeholder = new Label("Selecciona un juego");
        placeholder.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 14px;");
        placeholder.setPadding(new Insets(40));
        detallePanel.getChildren().add(placeholder);

        // ── Panel amigos derecha ──────────────────────
        amigosPanel = new VBox(0);
        amigosPanel.getStyleClass().add("friends-panel");

        Label friendsHeader = new Label("AMIGOS");
        friendsHeader.getStyleClass().add("friends-panel-header");
        amigosPanel.getChildren().add(friendsHeader);

        cargarAmigosPanel();

        root.getChildren().addAll(sidebarPanel, detallePanel, amigosPanel);
    }

    // ════════════════════════════════════════════════════
    //  PANEL DE AMIGOS (derecha)
    // ════════════════════════════════════════════════════

    private void cargarAmigosPanel() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuarioId + "/amigos";
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder().uri(java.net.URI.create(url)).GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200) return;

                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var relaciones = mapper.readValue(resp.body(),
                        new com.fasterxml.jackson.core.type.TypeReference<
                                java.util.List<AmigosView.AmigoRelacionDTO>>() {});

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
                            String estadoTxt = switch (otro.estado == null ? "offline" : otro.estado) {
                                case "online"      -> "En línea";
                                case "ausente"     -> "Ausente";
                                case "no_molestar" -> "No molestar";
                                default            -> "Desconectado";
                            };

                            // Avatar
                            Label letra = new Label(otro.nombre.substring(0, 1).toUpperCase());
                            letra.setStyle("-fx-text-fill: #8b9db0; -fx-font-weight: bold; -fx-font-size: 11px;");
                            StackPane av = new StackPane(letra);
                            av.setMinSize(26, 26); av.setMaxSize(26, 26);
                            av.getStyleClass().add("friend-avatar");

                            // Dot estado
                            Label dot = new Label();
                            dot.setMinSize(7, 7); dot.setMaxSize(7, 7);
                            dot.setStyle("-fx-background-color: " + dotColor + "; -fx-background-radius: 50; -fx-border-color: #131a23; -fx-border-width: 1; -fx-border-radius: 50;");
                            StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
                            av.getChildren().add(dot);

                            // Info
                            Label nombre = new Label(otro.nombre);
                            nombre.getStyleClass().add("friend-name");
                            Label estado = new Label(estadoTxt);
                            estado.getStyleClass().add("friend-status");
                            VBox info = new VBox(2, nombre, estado);

                            HBox fila = new HBox(8, av, info);
                            fila.setAlignment(Pos.CENTER_LEFT);
                            fila.getStyleClass().add("friend-row");
                            fila.setOnMouseEntered(e -> fila.setStyle("-fx-background-color: #1e2d3d;"));
                            fila.setOnMouseExited(e -> fila.setStyle(""));
                            Long amigoId = otro.id;
                            fila.setOnMouseClicked(e -> {
                                if (callbackAbrirChat != null) callbackAbrirChat.accept(amigoId);
                            });
                            return fila;
                        }).toList();

                Platform.runLater(() -> {
                    if (filas.isEmpty()) {
                        Label empty = new Label("Sin amigos aún.");
                        empty.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 12px; -fx-padding: 16 14 0 14;");
                        amigosPanel.getChildren().add(empty);
                    } else {
                        amigosPanel.getChildren().addAll(filas);
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    // ════════════════════════════════════════════════════
    //  SIDEBAR — lista de juegos
    // ════════════════════════════════════════════════════

    private void cargarBiblioteca() {
        try {
            List<Juego> juegos = juegoService.obtenerBiblioteca(usuarioId);

            juegosContainer = new VBox(2);
            juegosContainer.setPadding(new Insets(4, 6, 4, 6));

            for (Juego juego : juegos) {
                HBox card = crearSidebarCard(juego);
                juegosContainer.getChildren().add(card);
            }

            ScrollPane scroll = new ScrollPane(juegosContainer);
            scroll.getStyleClass().add("scroll-pane");
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            sidebarPanel.getChildren().add(scroll);

            if (!juegos.isEmpty()) {
                mostrarJuego(juegos.get(0));
                Platform.runLater(() -> {
                    if (!juegosContainer.getChildren().isEmpty())
                        marcarSeleccion((HBox) juegosContainer.getChildren().get(0));
                });
            } else {
                Label empty = new Label("Tu biblioteca está vacía.\nVisita la tienda.");
                empty.setWrapText(true);
                empty.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 12px; -fx-padding: 20 10 0 10; -fx-text-alignment: CENTER;");
                detallePanel.getChildren().setAll(empty);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sidebarPanel.getChildren().add(new Label("Error cargando biblioteca"));
        }
    }

    private HBox crearSidebarCard(Juego juego) {
        // Thumbnail
        ImageView thumb = new ImageView();
        thumb.setFitWidth(42); thumb.setFitHeight(26);
        thumb.setPreserveRatio(true); thumb.setSmooth(true);
        setImagenConFallback(thumb, juego.getImagen_url(), "Logo");

        // Nombre
        Label nombre = new Label(juego.getTitulo());
        nombre.getStyleClass().add("sidebar-game-name");

        HBox card = new HBox(10, thumb, nombre);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("sidebar-game-row");
        card.setMaxWidth(Double.MAX_VALUE);

        card.setOnMouseClicked(e -> {
            mostrarJuego(juego);
            marcarSeleccion(card);
        });
        card.getProperties().put("titulo", juego.getTitulo());
        card.getProperties().put("juego", juego);
        return card;
    }

    // ════════════════════════════════════════════════════
    //  PANEL CENTRAL — detalle del juego
    // ════════════════════════════════════════════════════

    private void mostrarJuego(Juego juego) {
        detallePanel.getChildren().clear();

        // ─────────────────────────────────────────────
        // HERO (PORTADA)
        // ─────────────────────────────────────────────
        ImageView portada = new ImageView();
        portada.setSmooth(true);
        portada.setPreserveRatio(false); // COVER real

        StackPane heroContainer = new StackPane();
        heroContainer.getStyleClass().add("hero-container");
        heroContainer.setPrefHeight(350);
        heroContainer.setMinHeight(350);
        heroContainer.setMaxHeight(350);

        portada.fitWidthProperty().bind(heroContainer.widthProperty());
        portada.fitHeightProperty().bind(heroContainer.heightProperty());

        // Mantener tu fallback original
        setImagenConFallback(portada, juego.getImagen_url(), "1232");

        heroContainer.getChildren().add(portada);

        // ─────────────────────────────────────────────
        // OVERLAY
        // ─────────────────────────────────────────────
        VBox heroOverlay = new VBox(4);
        heroOverlay.getStyleClass().add("hero-overlay");
        heroOverlay.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(heroOverlay, Pos.BOTTOM_LEFT);

        Label heroTitulo = new Label(juego.getTitulo());
        heroTitulo.getStyleClass().add("hero-titulo");

        Label heroMeta = new Label(
                (juego.getCategoria() != null ? juego.getCategoria() : "") +
                        (juego.getDesarrollador() != null ? " · " + juego.getDesarrollador() : "") +
                        (juego.getPrecio() != null ? " · " + juego.getPrecio() + "€" : "")
        );
        heroMeta.getStyleClass().add("hero-meta");

        Label heroHoras = new Label("⏱ Cargando horas...");
        heroHoras.getStyleClass().add("hero-horas");

        heroOverlay.getChildren().addAll(heroTitulo, heroMeta, heroHoras);
        heroContainer.getChildren().add(heroOverlay);

        // ─────────────────────────────────────────────
        // HORAS (THREAD)
        // ─────────────────────────────────────────────
        Label statHorasVal = new Label("Cargando...");
        Label statLogrosVal = new Label("Cargando...");

        new Thread(() -> {
            try {
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(
                                        Config.API_BASE_URL + "/juegos/" + juego.getId() + "/mis-horas"
                                ))
                                .GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                );

                if (resp.statusCode() == 200) {
                    var node = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(resp.body());

                    double h = node.path("horas").asDouble();

                    Platform.runLater(() -> {
                        heroHoras.setText("⏱ " + h + "h jugadas");
                        statHorasVal.setText(h + "h");
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        // ─────────────────────────────────────────────
        // BOTONES
        // ─────────────────────────────────────────────
        Button jugar = new Button("▶  JUGAR");
        jugar.getStyleClass().add("btn-play-green");

        Button actualizar = new Button("↻  ACTUALIZAR");
        actualizar.getStyleClass().add("btn-action-blue");

        Button gestionar = new Button("⚙  Gestionar");
        gestionar.getStyleClass().add("btn-action-gray");

        String gameName = juego.getTitulo().replace(" ", "_");
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
                    Platform.runLater(() -> {
                        jugar.setDisable(false);
                        jugar.setText("▶  JUGAR");
                    });
                }
            }).start();
        });

        actualizar.setOnAction(e -> {
            actualizar.setDisable(true);
            actualizar.setText("⏳  Actualizando...");

            new Thread(() -> {
                try {
                    installer.install(gameName, downloadUrl);

                    Platform.runLater(() -> {
                        actualizar.setDisable(false);
                        actualizar.setText("✅  Actualizado");
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        actualizar.setDisable(false);
                        actualizar.setText("↻  ACTUALIZAR");
                    });
                }
            }).start();
        });

        HBox actionBar = new HBox(10, jugar, actualizar, gestionar);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.getStyleClass().add("action-bar");

        // ─────────────────────────────────────────────
        // STATS (SIN VALORACIÓN)
        // ─────────────────────────────────────────────
        Label statPrecioVal = new Label(
                juego.getPrecio() != null ? juego.getPrecio() + "€" : "Gratis"
        );

        HBox statsRow = new HBox(10,
                crearStatCard(statPrecioVal, "Precio"),
                crearStatCard(statHorasVal, "Horas jugadas"),
                crearStatCard(statLogrosVal, "Logros")
        );
        statsRow.getStyleClass().add("stats-row");

        // ─────────────────────────────────────────────
        // SECCIONES
        // ─────────────────────────────────────────────
        VBox secActualizaciones = crearSeccionActualizaciones(juego.getId());
        VBox secLogros = crearSeccionLogros(juego.getId(), statLogrosVal);
        VBox secAmigos = crearSeccionAmigosJuego(juego.getId());

        VBox cuerpo = new VBox(0, secActualizaciones, secLogros, secAmigos);
        cuerpo.getStyleClass().add("detalle-body");

        VBox contenido = new VBox(0, actionBar, statsRow, cuerpo);
        VBox.setVgrow(cuerpo, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(new VBox(heroContainer, contenido));
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        detallePanel.getChildren().add(scroll);
        VBox.setVgrow(detallePanel, Priority.ALWAYS);
    }



    private VBox crearStatCard(Label valor, String etiqueta) {
        valor.getStyleClass().add("stat-valor");
        Label lbl = new Label(etiqueta.toUpperCase());
        lbl.getStyleClass().add("stat-label");
        VBox card = new VBox(3, valor, lbl);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ════════════════════════════════════════════════════
    //  SECCIONES INFERIORES
    // ════════════════════════════════════════════════════

    private VBox crearSeccionActualizaciones(Long juegoId) {
        VBox sec = new VBox(0);
        sec.getStyleClass().add("info-section");

        Label header = new Label("ACTUALIZACIONES");
        header.getStyleClass().add("info-section-header");

        VBox lista = new VBox(0);
        Label cargando = new Label("Cargando...");
        cargando.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 12px; -fx-padding: 10 16 10 16;");
        lista.getChildren().add(cargando);

        sec.getChildren().addAll(header, lista);

        new Thread(() -> {
            try {
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(Config.API_BASE_URL + "/juegos/" + juegoId + "/actualizaciones"))
                                .GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var acts = mapper.readValue(resp.body(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String,Object>>>() {});

                Platform.runLater(() -> {
                    lista.getChildren().clear();
                    if (acts.isEmpty()) {
                        Label e = new Label("Sin actualizaciones aún.");
                        e.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 12px; -fx-padding: 10 16 10 16;");
                        lista.getChildren().add(e);
                    } else {
                        for (var a : acts) {
                            HBox fila = new HBox();
                            fila.getStyleClass().add("update-row");
                            fila.setAlignment(Pos.CENTER_LEFT);

                            Label tit = new Label(a.get("titulo").toString());
                            tit.getStyleClass().add("update-titulo");
                            HBox.setHgrow(tit, Priority.ALWAYS);

                            Label fecha = new Label(a.get("fecha").toString().substring(0, 10).replace("-", "/"));
                            fecha.getStyleClass().add("update-fecha");

                            fila.getChildren().addAll(tit, fecha);

                            String desc = a.get("descripcion") != null ? a.get("descripcion").toString() : "";
                            VBox item = new VBox(2, fila);
                            if (!desc.isEmpty()) {
                                Label lDesc = new Label(desc);
                                lDesc.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 11px; -fx-padding: 0 16 6 16;");
                                lDesc.setWrapText(true);
                                item.getChildren().add(lDesc);
                            }
                            item.getStyleClass().add("update-item");
                            lista.getChildren().add(item);
                        }
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        return sec;
    }

    private VBox crearSeccionLogros(Long juegoId, Label statLogrosVal) {
        VBox sec = new VBox(0);
        sec.getStyleClass().add("info-section");

        Label header = new Label("LOGROS");
        header.getStyleClass().add("info-section-header");

        HBox iconos = new HBox(5);
        iconos.setAlignment(Pos.CENTER_LEFT);
        iconos.setPadding(new Insets(10, 16, 6, 16));

        // Barra de progreso
        Region barraBg = new Region();
        barraBg.getStyleClass().add("logros-barra-bg");

        Region barraFill = new Region();
        barraFill.getStyleClass().add("logros-barra-fill");
        barraFill.setPrefWidth(0);
        barraFill.setMaxWidth(0);

        StackPane barra = new StackPane(barraBg, barraFill);
        StackPane.setAlignment(barraFill, Pos.CENTER_LEFT);

        Label progTxt = new Label("Cargando...");
        progTxt.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 11px;");

        VBox barraBox = new VBox(5, barra, progTxt);
        barraBox.setPadding(new Insets(0, 16, 12, 16));

        sec.getChildren().addAll(header, iconos, barraBox);

        // ─────────────────────────────────────────────
        // THREAD: cargar logros
        // ─────────────────────────────────────────────
        new Thread(() -> {
            try {
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(Config.API_BASE_URL + "/juegos/" + juegoId + "/logros"))
                                .GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var logros = mapper.readValue(resp.body(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String,Object>>>() {});

                Platform.runLater(() -> {
                    iconos.getChildren().clear();

                    if (logros.isEmpty()) {
                        progTxt.setText("Sin logros para este juego.");
                        statLogrosVal.setText("0 / 0");
                        return;
                    }

                    long desbloqueados = logros.stream()
                            .filter(l -> Boolean.TRUE.equals(l.get("desbloqueado")))
                            .count();

                    // Iconos
                    for (var l : logros) {
                        boolean bloqueado = !Boolean.TRUE.equals(l.get("desbloqueado"));
                        ImageView ico = cargarIconoLogro(
                                l.get("icono") != null ? l.get("icono").toString() : null, 24
                        );
                        ico.setOpacity(bloqueado ? 0.25 : 1.0);
                        Tooltip.install(ico, new Tooltip(l.get("nombre") + ": " + l.get("descripcion")));
                        iconos.getChildren().add(ico);
                    }

                    // Barra de progreso
                    double pct = (double) desbloqueados / logros.size() * 100;
                    barraBg.setPrefWidth(300);
                    barraFill.setPrefWidth(300 * pct / 100);
                    barraFill.setMaxWidth(300 * pct / 100);

                    progTxt.setText(desbloqueados + " / " + logros.size() + " desbloqueados · " + (int) pct + "%");

                    // 🔥 Actualizar la tarjeta de stats
                    statLogrosVal.setText(desbloqueados + " / " + logros.size());
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        return sec;
    }

    private ImageView cargarIconoLogro(String ruta, double size) {
        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        String url = (ruta != null && !ruta.isBlank())
                ? Config.IMG_BASE_URL + "/" + ruta
                : Config.IMG_BASE_URL + "/logros/default.png";

        Image img = new Image(url, true);
        img.errorProperty().addListener((obs, o, err) -> {
            if (err) Platform.runLater(() ->
                    iv.setImage(new Image(Config.IMG_BASE_URL + "/logros/default.png", true))
            );
        });
        iv.setImage(img);
        return iv;
    }


    private VBox crearSeccionAmigosJuego(Long juegoId) {
        VBox sec = new VBox(0);
        sec.getStyleClass().add("info-section");

        Label header = new Label("AMIGOS CON ESTE JUEGO");
        header.getStyleClass().add("info-section-header");

        HBox tarjetas = new HBox(10);
        tarjetas.setAlignment(Pos.CENTER_LEFT);
        tarjetas.setPadding(new Insets(10, 16, 16, 16));

        Label cargando = new Label("Cargando...");
        cargando.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 12px; -fx-padding: 10 16 16 16;");

        sec.getChildren().addAll(header, cargando);

        new Thread(() -> {
            try {
                var resp = com.faguaslandia.launcher.service.AuthService.getClient().send(
                        java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(Config.API_BASE_URL + "/juegos/" + juegoId + "/amigos-con-juego"))
                                .GET().build(),
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var amigos = mapper.readValue(resp.body(),
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String,Object>>>() {});

                Platform.runLater(() -> {
                    sec.getChildren().remove(cargando);
                    if (amigos.isEmpty()) {
                        Label vacio = new Label("Ningún amigo tiene este juego.");
                        vacio.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 12px; -fx-padding: 10 16 16 16;");
                        sec.getChildren().add(vacio);
                        return;
                    }
                    for (var a : amigos) {
                        String nombre = a.get("nombre").toString();
                        String horas  = a.get("horas").toString();

                        Label letraLbl = new Label(nombre.substring(0, 1).toUpperCase());
                        letraLbl.setStyle("-fx-text-fill: #66c0f4; -fx-font-weight: bold; -fx-font-size: 13px;");
                        StackPane av = new StackPane(letraLbl);
                        av.setMinSize(28, 28); av.setMaxSize(28, 28);
                        av.getStyleClass().add("friend-avatar");

                        // Cargar foto
                        String fotoUrl = Config.IMG_BASE_URL + "/avatars/" + a.get("foto");
                        javafx.scene.image.Image img = new javafx.scene.image.Image(fotoUrl, true);
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(28); iv.setFitHeight(28);
                        iv.setClip(new Circle(14, 14, 14));
                        img.errorProperty().addListener((obs, o, err) -> {
                            if (err) Platform.runLater(() -> { av.getChildren().clear(); av.getChildren().add(letraLbl); });
                        });
                        av.getChildren().add(iv);

                        Label nombreLbl = new Label(nombre);
                        nombreLbl.getStyleClass().add("amigo-card-nombre");
                        Label horasLbl = new Label(horas + "h jugadas");
                        horasLbl.getStyleClass().add("amigo-card-horas");
                        VBox info = new VBox(2, nombreLbl, horasLbl);

                        HBox tarjeta = new HBox(8, av, info);
                        tarjeta.setAlignment(Pos.CENTER_LEFT);
                        tarjeta.getStyleClass().add("amigo-juego-card");
                        tarjetas.getChildren().add(tarjeta);
                    }
                    sec.getChildren().add(tarjetas);
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        return sec;
    }

    // ════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════

    private void marcarSeleccion(HBox card) {

        for (var nodo : juegosContainer.getChildren()) {
            if (nodo instanceof HBox fila) {
                fila.getStyleClass().remove("sidebar-game-row-selected");
            }
        }

        card.getStyleClass().add("sidebar-game-row-selected");

    }


    // Sobrecarga para compatibilidad con StackPane (no se usa en el nuevo layout)
    private void marcarSeleccion(StackPane card) {}

    private void setImagenConFallback(ImageView iv, String imagenUrl, String variante) {
        String base   = Config.IMG_BASE_URL + "/" + imagenUrl.replaceAll("(?i)\\.png$", "");
        String urlVar = base + variante + ".png";
        String urlFb  = Config.IMG_BASE_URL + "/" + imagenUrl;
        Image img = new Image(urlVar, true);
        iv.setImage(img);
        img.errorProperty().addListener((obs, o, err) -> {
            if (err) Platform.runLater(() -> iv.setImage(new Image(urlFb, true)));
        });
    }

    public void actualizarBiblioteca() {
        sidebarPanel.getChildren().clear();
        Label sidebarLabel = new Label("MIS JUEGOS");
        sidebarLabel.getStyleClass().add("sidebar-section-label");
        sidebarPanel.getChildren().add(sidebarLabel);
        detallePanel.getChildren().clear();
        selectedCard = null;
        cargarBiblioteca();
    }

    public void lanzarJuegoPorNombre(String nombre) {
        if (juegosContainer == null) return;
        juegosContainer.getChildren().forEach(node -> {
            if (node instanceof HBox card) {
                Object tituloObj = card.getProperties().get("titulo");
                if (tituloObj != null && nombre.equals(tituloObj.toString().replace(" ", "_"))) {
                    mostrarJuego((Juego) card.getProperties().get("juego"));
                    marcarSeleccion(card);
                }
            }
        });
        new Thread(() -> {
            try {
                if (!installer.isInstalled(nombre)) return;
                Platform.runLater(() -> installer.launch(nombre));
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }
}