package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class JuegoDetailView extends VBox {

    private Label titulo;
    private Label descripcion;
    private Label precio;
    private ImageView imagen;
    private Button comprarBtn;
    private Button volverBtn;

    private final JuegoService juegoService;
    private final Usuario usuario;

    // Callbacks
    private Runnable callbackActualizarBiblioteca;
    private Runnable callbackVolver;

    public JuegoDetailView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();

        setSpacing(15);
        setPadding(new Insets(20));

        // Contenido central
        imagen = new ImageView();
        imagen.setFitWidth(300);
        imagen.setPreserveRatio(true);

        titulo = new Label();
        titulo.getStyleClass().add("title");

        descripcion = new Label();
        descripcion.setWrapText(true);

        precio = new Label();
        precio.getStyleClass().add("price");

        comprarBtn = new Button("Comprar");
        volverBtn = new Button("Volver");

        // Evento de volver
        volverBtn.setOnAction(e -> {
            if (callbackVolver != null) {
                callbackVolver.run();
            }
        });

        getChildren().addAll(imagen, titulo, descripcion, precio, comprarBtn, volverBtn);
    }

    public void setJuego(Juego juego) {
        if (juego.getImagen_url() != null && !juego.getImagen_url().isEmpty()) {
            try {
                imagen.setImage(new Image(getClass().getResourceAsStream("/images/" + juego.getImagen_url())));
            } catch (Exception e) {
                imagen.setImage(new Image(getClass().getResourceAsStream("/images/algo.png")));
            }
        } else {
            imagen.setImage(new Image(getClass().getResourceAsStream("/images/algo.png")));
        }

        titulo.setText(juego.getTitulo());
        descripcion.setText(juego.getDescripcion() != null ? juego.getDescripcion() : "Sin descripción");
        precio.setText(juego.getPrecio() != null ? juego.getPrecio() + "€" : "Gratis");

        Platform.runLater(() -> {
            try {
                boolean comprado = juegoService.estaComprado(usuario.getId(), juego.getId());
                if (comprado) {
                    comprarBtn.setText("Comprado");
                    comprarBtn.setDisable(true);
                } else {
                    comprarBtn.setText("Comprar");
                    comprarBtn.setDisable(false);
                }

                comprarBtn.setOnAction(e -> {
                    try {
                        juegoService.comprarJuego(usuario.getId(), juego.getId());
                        comprarBtn.setText("Comprado");
                        comprarBtn.setDisable(true);
                        if (callbackActualizarBiblioteca != null) callbackActualizarBiblioteca.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Error al comprar").showAndWait();
                    }
                });
            } catch (Exception e) {
                comprarBtn.setText("Error");
                comprarBtn.setDisable(true);
            }
        });
    }

    public Button getVolverBtn() {
        return volverBtn;
    }

    // Callback para actualizar biblioteca
    public void setCallbackActualizarBiblioteca(Runnable callback) {
        this.callbackActualizarBiblioteca = callback;
    }

    // Nuevo callback para volver a biblioteca
    public void setCallbackVolver(Runnable callback) {
        this.callbackVolver = callback;
    }
}
