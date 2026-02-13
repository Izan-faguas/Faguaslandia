package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

public class TiendaView extends VBox {

    private final JuegoService juegoService;
    private final Usuario usuario;
    private CallbackJuegoDetalle callbackJuegoDetalle;

    public TiendaView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();

        setPadding(new Insets(20));
        setSpacing(20);
        getStyleClass().add("tienda");

        // Grid de juegos
        TilePane juegosGrid = new TilePane();
        juegosGrid.setHgap(20);
        juegosGrid.setVgap(20);
        juegosGrid.setPrefColumns(3);
        getChildren().add(juegosGrid);

        try {
            List<Juego> juegos = juegoService.obtenerTodos();
            if (juegos.isEmpty()) {
                juegosGrid.getChildren().add(new Label("No hay juegos disponibles"));
            } else {
                for (Juego j : juegos) {
                    juegosGrid.getChildren().add(crearTarjetaJuego(j));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            juegosGrid.getChildren().add(new Label("Error cargando la tienda"));
        }
    }

    public void setCallbackJuegoDetalle(CallbackJuegoDetalle callback) {
        this.callbackJuegoDetalle = callback;
    }

    private VBox crearTarjetaJuego(Juego j) {
        VBox tarjeta = new VBox(8);
        tarjeta.setPadding(new Insets(10));
        tarjeta.getStyleClass().add("store-card");

        ImageView img;
        try {
            Image imgJuego;
            if (j.getImagen_url() != null && !j.getImagen_url().isEmpty()) {
                try {
                    imgJuego = new Image(getClass().getResourceAsStream("/images/" + j.getImagen_url()));
                    if (imgJuego.isError()) throw new Exception();
                } catch (Exception ex) {
                    imgJuego = new Image(j.getImagen_url(), true);
                }
            } else {
                imgJuego = new Image(getClass().getResourceAsStream("/images/algo.png"));
            }
            img = new ImageView(imgJuego);
            img.setFitWidth(180);
            img.setFitHeight(100);
            img.setPreserveRatio(true);
        } catch (Exception e) {
            img = new ImageView(new Image(getClass().getResourceAsStream("/images/algo.png")));
        }

        Label titulo = new Label(j.getTitulo());
        titulo.getStyleClass().add("card-title");

        Label descripcion = new Label(j.getDescripcion() != null ? j.getDescripcion() : "Sin descripción");
        descripcion.getStyleClass().add("card-description");
        descripcion.setWrapText(true);

        img.setOnMouseClicked(e -> {
            if (callbackJuegoDetalle != null) {
                callbackJuegoDetalle.mostrarJuegoDetalle(j);
            }
        });

        tarjeta.getChildren().addAll(img, titulo, descripcion);
        return tarjeta;
    }

    public interface CallbackJuegoDetalle {
        void mostrarJuegoDetalle(Juego juego);
    }
}
