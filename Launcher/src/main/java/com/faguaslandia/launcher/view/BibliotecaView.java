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
import javafx.scene.shape.Circle;

import java.util.List;

public class BibliotecaView {

    private final JuegoService juegoService   = new JuegoService();
    private final GameInstallerService installer = new GameInstallerService();
    private final Long usuarioId;

    private HBox root;
    private VBox bibliotecaPanel;
    private VBox detallePanel;
    private VBox amigosPanel;
    private VBox juegosContainer;

    private StackPane selectedCard;

    public BibliotecaView(Long usuarioId) {
        this.usuarioId = usuarioId;
        crearVista();
        cargarBiblioteca();
    }

    public HBox getView() { return root; }

    private void crearVista() {
        root = new HBox();
        root.getStyleClass().add("root");

        /* ── IZQUIERDA: lista covers ── */
        bibliotecaPanel = new VBox(12);
        bibliotecaPanel.getStyleClass().add("panel-left");

        Label tituloLib = new Label("🎮 Biblioteca");
        tituloLib.getStyleClass().add("panel-left-title");
        bibliotecaPanel.getChildren().add(tituloLib);

        /* ── CENTRO: detalle ── */
        detallePanel = new VBox();
        detallePanel.getStyleClass().add("panel-center");
        HBox.setHgrow(detallePanel, Priority.ALWAYS);

        Label placeholder = new Label("Selecciona un juego");
        placeholder.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 14px;");
        placeholder.setPadding(new Insets(40));
        detallePanel.getChildren().add(placeholder);

        /* ── DERECHA: amigos ── */
        amigosPanel = new VBox(4);
        amigosPanel.getStyleClass().add("panel-right");

        Label tituloAmigos = new Label("👥 Amigos");
        tituloAmigos.getStyleClass().add("amigos-titulo");
        tituloAmigos.setMaxWidth(Double.MAX_VALUE);
        amigosPanel.getChildren().add(tituloAmigos);

        Label amigosEmpty = new Label("Sin amigos aún.\nAgrega desde tu perfil.");
        amigosEmpty.getStyleClass().add("amigos-vacio");
        amigosEmpty.setWrapText(true);
        amigosEmpty.setPadding(new Insets(16, 4, 0, 4));
        amigosPanel.getChildren().add(amigosEmpty);

        root.getChildren().addAll(bibliotecaPanel, detallePanel, amigosPanel);
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
                // marcar el primer card
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
        image.errorProperty().addListener((obs, o, err) -> {
            if (err) System.out.println("Error cargando imagen: " + url);
        });


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

        return card;
    }

    private void mostrarJuego(Juego juego) {
        detallePanel.getChildren().clear();

        /* ── Imagen grande ── */
        String url = Config.IMG_BASE_URL + "/" + juego.getImagen_url();
        ImageView portada = new ImageView(new Image(url, true));
        portada.setFitWidth(900);
        portada.setFitHeight(320);
        portada.setPreserveRatio(false);
        portada.setSmooth(true);
        portada.getStyleClass().add("detalle-img");

        // contenedor de imagen que ocupa todo el ancho
        StackPane imgContainer = new StackPane(portada);
        imgContainer.setMaxWidth(Double.MAX_VALUE);
        portada.fitWidthProperty().bind(imgContainer.widthProperty());

        /* ── Info ── */
        Label titulo = new Label(juego.getTitulo());
        titulo.getStyleClass().add("detalle-titulo");

        Label desc = new Label(juego.getDescripcion() != null ? juego.getDescripcion() : "Sin descripción disponible");
        desc.setWrapText(true);
        desc.getStyleClass().add("detalle-desc");

        // Fila de meta: desarrollador, categoría
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

        Button jugar = new Button("▶  JUGAR");
        jugar.getStyleClass().add("btn-play");

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
                        installer.launch(gameName);
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

        VBox info = new VBox(14, titulo, desc, meta, jugar);
        info.getStyleClass().add("detalle-info");
        info.setMaxWidth(Double.MAX_VALUE);

        detallePanel.getChildren().addAll(imgContainer, info);
        VBox.setVgrow(info, Priority.ALWAYS);
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
}