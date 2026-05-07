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

public class JuegoDetailView extends VBox {

    private final JuegoService juegoService;
    private final Usuario usuario;

    private Runnable callbackActualizarBiblioteca;
    private Runnable callbackVolver;

    public JuegoDetailView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();
        setStyle("-fx-background-color: linear-gradient(to bottom, #121a24, #0b1118);");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
    }

    public void setJuego(Juego juego) {
        getChildren().clear();

        /* ── Imagen hero ── */
        String imgUrl = Config.API_BASE_URL + "/" + juego.getImagen_url();
        ImageView portada = new ImageView(new Image(imgUrl, true));
        portada.setFitHeight(320);
        portada.setPreserveRatio(false);
        portada.setSmooth(true);

        StackPane hero = new StackPane(portada);
        hero.setMaxWidth(Double.MAX_VALUE);
        portada.fitWidthProperty().bind(hero.widthProperty());

        /* ── Botón volver ── */
        Button volver = new Button("← Volver a la tienda");
        volver.getStyleClass().add("btn-secondary");
        volver.setOnAction(e -> { if (callbackVolver != null) callbackVolver.run(); });

        /* ── Título + precio ── */
        Label titulo = new Label(juego.getTitulo());
        titulo.getStyleClass().add("detalle-titulo");

        String precioTxt = (juego.getPrecio() == null || juego.getPrecio().doubleValue() == 0)
                ? "Gratis" : juego.getPrecio() + " €";
        Label precio = new Label(precioTxt);
        precio.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #66c0f4;");

        HBox tituloPrecio = new HBox(20, titulo, precio);
        tituloPrecio.setAlignment(Pos.BOTTOM_LEFT);

        /* ── Descripción ── */
        Label desc = new Label(juego.getDescripcion() != null ? juego.getDescripcion() : "Sin descripción disponible.");
        desc.setWrapText(true);
        desc.getStyleClass().add("detalle-desc");

        /* ── Meta: dev, categoría, fecha ── */
        HBox meta = new HBox(16);
        meta.setAlignment(Pos.CENTER_LEFT);
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
        if (juego.getFecha_lanzamiento() != null) {
            Label fecha = new Label("📅 " + juego.getFecha_lanzamiento());
            fecha.getStyleClass().add("detalle-meta");
            meta.getChildren().add(fecha);
        }

        /* ── Botón comprar (estado dinámico) ── */
        Button comprarBtn = new Button("Cargando...");
        comprarBtn.setDisable(true);
        comprarBtn.getStyleClass().add("btn-buy");

        new Thread(() -> {
            try {
                boolean comprado = juegoService.estaComprado(usuario.getId(), juego.getId());
                Platform.runLater(() -> {
                    if (comprado) {
                        comprarBtn.setText("✔ En tu biblioteca");
                        comprarBtn.getStyleClass().remove("btn-buy");
                        comprarBtn.getStyleClass().add("btn-comprado");
                        comprarBtn.setDisable(true);
                    } else {
                        comprarBtn.setText("🛒  Comprar — " + precioTxt);
                        comprarBtn.setDisable(false);
                        comprarBtn.setOnAction(e -> {
                            comprarBtn.setDisable(true);
                            comprarBtn.setText("Procesando...");
                            new Thread(() -> {
                                try {
                                    juegoService.comprarJuego(usuario.getId(), juego.getId());
                                    Platform.runLater(() -> {
                                        comprarBtn.setText("✔ En tu biblioteca");
                                        comprarBtn.getStyleClass().remove("btn-buy");
                                        comprarBtn.getStyleClass().add("btn-comprado");
                                        if (callbackActualizarBiblioteca != null)
                                            callbackActualizarBiblioteca.run();
                                    });
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Platform.runLater(() -> {
                                        comprarBtn.setDisable(false);
                                        comprarBtn.setText("Error. Reintentar");
                                    });
                                }
                            }).start();
                        });
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comprarBtn.setText("Error");
                    comprarBtn.setDisable(true);
                });
            }
        }).start();

        /* ── Separador ── */
        Region sep = new Region();
        sep.getStyleClass().add("separator");
        sep.setMaxWidth(Double.MAX_VALUE);

        /* ── Ensamblar ── */
        VBox info = new VBox(16, volver, tituloPrecio, desc, meta, sep, comprarBtn);
        info.setStyle("-fx-padding: 28 32 28 32; -fx-background-color: transparent;");
        info.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(hero, info);
        VBox.setVgrow(info, Priority.ALWAYS);
    }

    public void setCallbackActualizarBiblioteca(Runnable r) { this.callbackActualizarBiblioteca = r; }
    public void setCallbackVolver(Runnable r)               { this.callbackVolver = r; }

    /** Compatibilidad con LauncherApp existente */
    public Button getVolverBtn() {
        Button dummy = new Button("Volver");
        dummy.setOnAction(e -> { if (callbackVolver != null) callbackVolver.run(); });
        return dummy;
    }
}