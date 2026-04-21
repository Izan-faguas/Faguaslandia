package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.service.GameInstallerService;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.List;

public class BibliotecaView {

    private final JuegoService juegoService = new JuegoService();
    private final Long usuarioId;
    private GameInstallerService installer = new GameInstallerService();


    private HBox root;

    private VBox bibliotecaPanel;
    private VBox detallePanel;
    private VBox amigosPanel;

    private TilePane juegosContainer;
    private StackPane selectedCard;

    public BibliotecaView(Long usuarioId) {
        this.usuarioId = usuarioId;
        crearVista();
        cargarBiblioteca();
    }

    public HBox getView() {
        return root;
    }

    private void crearVista() {

        root = new HBox();
        root.getStyleClass().add("root");

        root.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // 📚 IZQUIERDA (más ancha)
        bibliotecaPanel = new VBox(15);
        bibliotecaPanel.getStyleClass().add("panel-left");

        // 🎮 CENTRO (más grande)
        detallePanel = new VBox(15);
        detallePanel.getStyleClass().add("panel-center");

        // 👥 DERECHA
        amigosPanel = new VBox(15);
        amigosPanel.getStyleClass().add("panel-right");

        bibliotecaPanel.setPrefWidth(200);
        detallePanel.setPrefWidth(700);
        amigosPanel.setPrefWidth(190);

        Label titulo = new Label("🎮 Biblioteca");
        titulo.getStyleClass().add("title");

        bibliotecaPanel.getChildren().add(titulo);

        amigosPanel.getChildren().add(new Label("👥 Amigos"));

        root.getChildren().addAll(bibliotecaPanel, detallePanel, amigosPanel);

        HBox.setHgrow(detallePanel, Priority.ALWAYS);
    }

    private void cargarBiblioteca() {

        try {
            List<Juego> juegos = juegoService.obtenerBiblioteca(usuarioId);

            juegosContainer = new TilePane();
            juegosContainer.setPadding(new Insets(10));
            juegosContainer.setVgap(12);
            juegosContainer.setHgap(12);

            juegosContainer.setPrefColumns(1);
            juegosContainer.setMaxWidth(Double.MAX_VALUE);

            for (Juego juego : juegos) {

                String url = Config.API_BASE_URL + "/" + juego.getImagen_url();

                Image image = new Image(url, true);

                ImageView img = new ImageView(image);
                img.setFitWidth(320);
                img.setFitHeight(180);
                img.setPreserveRatio(false);
                img.setSmooth(true);

                StackPane card = new StackPane(img);
                card.getStyleClass().add("game-card");

                card.setPrefWidth(320);

                card.setOnMouseClicked(e -> {
                    mostrarJuego(juego);
                    marcarSeleccion(card);
                });

                juegosContainer.getChildren().add(card);
            }

            ScrollPane scroll = new ScrollPane(juegosContainer);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(true);
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            VBox.setVgrow(scroll, Priority.ALWAYS);

            bibliotecaPanel.getChildren().add(scroll);

            if (!juegos.isEmpty()) {
                mostrarJuego(juegos.get(0));
            }

        } catch (Exception e) {
            bibliotecaPanel.getChildren().add(new Label("Error cargando biblioteca"));
            e.printStackTrace();
        }
    }

    private void mostrarJuego(Juego juego) {

        detallePanel.getChildren().clear();

        String url = Config.API_BASE_URL + "/" + juego.getImagen_url();

        ImageView portada = new ImageView(new Image(url, true));
        portada.setFitWidth(600);
        portada.setFitHeight(320);
        portada.setPreserveRatio(true);
        portada.setSmooth(true);
        portada.getStyleClass().add("detalle-img");

        Label titulo = new Label(juego.getTitulo());
        titulo.getStyleClass().add("title");

        Label desc = new Label(
                juego.getDescripcion() != null ? juego.getDescripcion() : "Sin descripción"
        );
        desc.setWrapText(true);
        desc.getStyleClass().add("label");

        Button jugar = new Button("JUGAR");
        jugar.getStyleClass().add("btn-play");

        String gameName = juego.getTitulo().replace(" ", "_");
        String downloadUrl = Config.API_BASE_URL + "/juegos/download/" + juego.getId();

        jugar.setOnAction(e -> {

            try {
                if (installer.isInstalled(gameName)) {
                    System.out.println("Ejecutando " + gameName);
                    installer.launch(gameName);

                } else {
                    System.out.println("Instalando " + gameName);

                    installer.install(gameName, downloadUrl);

                    System.out.println("Ejecutando después de instalar...");
                    installer.launch(gameName);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox info = new VBox(10, titulo, desc, jugar);
        info.getStyleClass().add("detalle-info");

        detallePanel.getChildren().addAll(portada, info);
    }
    private void marcarSeleccion(StackPane selected) {

        if (selectedCard != null) {
            selectedCard.setStyle("");
        }

        selectedCard = selected;
        selectedCard.setStyle("""
            -fx-border-color: #00ffcc;
            -fx-border-width: 2;
            -fx-background-radius: 10;
        """);
    }

    private HBox crearAmigo(String nombre, boolean online) {

        Circle estado = new Circle(5);
        estado.setStyle(online ? "-fx-fill: #4caf50;" : "-fx-fill: #777;");

        Label label = new Label(nombre);

        return new HBox(10, estado, label);
    }

    public void actualizarBiblioteca() {
        bibliotecaPanel.getChildren().clear();

        Label titulo = new Label("🎮 Biblioteca");
        titulo.getStyleClass().add("title");

        bibliotecaPanel.getChildren().add(titulo);

        cargarBiblioteca();
    }
}