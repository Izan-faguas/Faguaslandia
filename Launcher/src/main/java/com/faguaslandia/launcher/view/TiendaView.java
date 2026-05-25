package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TiendaView extends VBox {

    private final JuegoService juegoService;
    private final Usuario usuario;
    private CallbackJuegoDetalle callbackJuegoDetalle;

    private List<Juego> juegosGlobal;
    private Set<Long> compradosSet;
    private FlowPane juegosGrid;
    private TextField busqueda;
    private ComboBox<String> filtroCategoria;
    private ComboBox<String> filtroPrecio;

    public TiendaView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();

        getStyleClass().add("tienda-container");
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        construirUI();
        cargarJuegos();
    }

    private void construirUI() {
        Label titulo = new Label("Tienda");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e6f0f8;");

        busqueda = new TextField();
        busqueda.setPromptText("Buscar juego...");
        busqueda.getStyleClass().add("search-bar");
        busqueda.textProperty().addListener((o, old, nv) -> filtrar());

        filtroCategoria = new ComboBox<>();
        filtroCategoria.setPromptText("Todas las categorías");
        filtroCategoria.getStyleClass().add("combo-filter");
        filtroCategoria.setOnAction(e -> filtrar());

        filtroPrecio = new ComboBox<>();
        filtroPrecio.setPromptText("Cualquier precio");
        filtroPrecio.getItems().addAll("Cualquier precio", "Gratis", "0€ – 10€", "10€ – 30€", "30€+");
        filtroPrecio.getStyleClass().add("combo-filter");
        filtroPrecio.setOnAction(e -> filtrar());

        HBox barra = new HBox(12, busqueda, filtroCategoria, filtroPrecio);
        barra.setAlignment(Pos.CENTER_LEFT);

        juegosGrid = new FlowPane();
        juegosGrid.setHgap(20);
        juegosGrid.setVgap(20);
        juegosGrid.setPadding(new Insets(8, 0, 0, 0));
        juegosGrid.setStyle("-fx-background-color: transparent;");

        ScrollPane scroll = new ScrollPane(juegosGrid);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().addAll(titulo, barra, scroll);
    }

    private void cargarJuegos() {
        new Thread(() -> {
            try {
                juegosGlobal = juegoService.obtenerTodos();

                try {
                    List<Juego> compras = juegoService.obtenerBiblioteca(usuario.getId());
                    compradosSet = compras.stream().map(Juego::getId).collect(Collectors.toSet());
                } catch (Exception e) {
                    compradosSet = Set.of();
                }

                Platform.runLater(() -> {
                    List<String> cats = juegosGlobal.stream()
                            .map(Juego::getCategoria)
                            .filter(c -> c != null && !c.isEmpty())
                            .distinct().sorted().collect(Collectors.toList());
                    filtroCategoria.getItems().add("Todas las categorías");
                    filtroCategoria.getItems().addAll(cats);

                    mostrarJuegos(juegosGlobal);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> juegosGrid.getChildren().add(new Label("Error cargando la tienda")));
            }
        }).start();
    }

    private void filtrar() {
        if (juegosGlobal == null) return;
        String texto = busqueda.getText().toLowerCase();
        String cat   = filtroCategoria.getValue();
        String precio = filtroPrecio.getValue();

        List<Juego> filtrados = juegosGlobal.stream().filter(j -> {
            boolean ok = true;
            if (!texto.isEmpty())
                ok = j.getTitulo().toLowerCase().contains(texto) ||
                        (j.getDescripcion() != null && j.getDescripcion().toLowerCase().contains(texto));
            if (ok && cat != null && !cat.equals("Todas las categorías"))
                ok = cat.equals(j.getCategoria());
            if (ok && precio != null) {
                double p = j.getPrecio() != null ? j.getPrecio().doubleValue() : 0;
                switch (precio) {
                    case "Gratis"   -> ok = p == 0;
                    case "0€ – 10€" -> ok = p <= 10;
                    case "10€ – 30€"-> ok = p > 10 && p <= 30;
                    case "30€+"     -> ok = p > 30;
                }
            }
            return ok;
        }).collect(Collectors.toList());

        mostrarJuegos(filtrados);
    }

    private void mostrarJuegos(List<Juego> juegos) {
        juegosGrid.getChildren().clear();
        if (juegos.isEmpty()) {
            Label empty = new Label("No hay juegos que coincidan");
            empty.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 13px; -fx-padding: 30 0 0 0;");
            juegosGrid.getChildren().add(empty);
            return;
        }
        for (Juego j : juegos) {
            juegosGrid.getChildren().add(crearTarjeta(j));
        }
    }

    private VBox crearTarjeta(Juego j) {
        boolean comprado = compradosSet != null && compradosSet.contains(j.getId());

        ImageView img = new ImageView();
        img.setFitWidth(220);
        img.setFitHeight(124);
        img.setPreserveRatio(false);
        img.setSmooth(true);
        setImagenConFallback(img, j.getImagen_url(), "920");

        StackPane imgPane = new StackPane(img);
        imgPane.setMaxWidth(220);

        if (comprado) {
            Label badge = new Label("✔ En tu biblioteca");
            badge.getStyleClass().add("badge-comprado");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            StackPane.setMargin(badge, new Insets(8));
            imgPane.getChildren().add(badge);
        }

        Label titulo = new Label(j.getTitulo());
        titulo.getStyleClass().add("store-card-title");
        titulo.setWrapText(false);

        Label desc = new Label(j.getDescripcion() != null ? j.getDescripcion() : "Sin descripción");
        desc.getStyleClass().add("store-card-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(192);
        desc.setMaxHeight(36);

        String precioStr;
        String precioStyle;
        if (comprado) {
            precioStr = "✔ Comprado";
            precioStyle = "store-card-price-comprado";
        } else if (j.getPrecio() == null || j.getPrecio().doubleValue() == 0) {
            precioStr = "Gratis";
            precioStyle = "store-card-price-free";
        } else {
            precioStr = j.getPrecio() + " €";
            precioStyle = "store-card-price";
        }
        Label precio = new Label(precioStr);
        precio.getStyleClass().add(precioStyle);

        VBox info = new VBox(5, titulo, desc, precio);
        info.getStyleClass().add("store-card-info");

        VBox card = new VBox(0, imgPane, info);
        card.getStyleClass().add("store-card");
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setCursor(javafx.scene.Cursor.HAND);

        card.setOnMouseClicked(e -> {
            if (callbackJuegoDetalle != null) callbackJuegoDetalle.mostrarJuegoDetalle(j);
        });

        return card;
    }

    public void setCallbackJuegoDetalle(CallbackJuegoDetalle callback) {
        this.callbackJuegoDetalle = callback;
    }

    public interface CallbackJuegoDetalle {
        void mostrarJuegoDetalle(Juego juego);
    }

    private void setImagenConFallback(ImageView iv, String imagenUrl, String variante) {
        String base   = Config.IMG_BASE_URL + "/" + imagenUrl.replaceAll("(?i)\\.png$", "");
        String urlVar = base + variante + ".png";
        String urlFb  = Config.IMG_BASE_URL + "/" + imagenUrl;

        Image img = new Image(urlVar, true);
        iv.setImage(img);
        img.errorProperty().addListener((obs, o, err) -> {
            if (err) javafx.application.Platform.runLater(() -> iv.setImage(new Image(urlFb, true)));
        });
    }
}