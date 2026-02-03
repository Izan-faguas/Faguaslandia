package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;

public class TiendaView extends VBox {

    private Label lblBiblioteca;
    private Label lblTienda;
    private Label lblPerfil;

    private final JuegoService juegoService;
    private final Usuario usuario; // Usuario logueado

    public TiendaView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();

        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.getStyleClass().add("tienda"); // CSS

        // Juego destacado
        HBox hero = crearHero();
        this.getChildren().add(hero);

        // Lista de juegos
        VBox listaJuegosBox = new VBox(15);
        listaJuegosBox.getStyleClass().add("store-grid");
        try {
            List<Juego> juegos = juegoService.obtenerTodos(); // Traemos de la BBDD

            if (juegos.isEmpty()) {
                Label sinJuegos = new Label("No hay juegos disponibles");
                listaJuegosBox.getChildren().add(sinJuegos);
            } else {
                for (Juego j : juegos) {
                    VBox tarjeta = crearTarjetaJuego(j, usuario.getId());
                    listaJuegosBox.getChildren().add(tarjeta);
                }
            }

            this.getChildren().add(listaJuegosBox);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMenuActions(Runnable irBiblioteca, Runnable irTienda, Runnable irPerfil) {
        lblBiblioteca.setOnMouseClicked(e -> irBiblioteca.run());
        lblTienda.setOnMouseClicked(e -> irTienda.run());
        lblPerfil.setOnMouseClicked(e -> irPerfil.run());
    }

    private HBox crearHero() {
        HBox hero = new HBox(20);
        hero.getStyleClass().add("hero");

        ImageView img = new ImageView(new Image("file:src/main/resources/imagenes/mini_golf.png"));
        img.setFitWidth(200);
        img.setPreserveRatio(true);

        VBox info = new VBox(10);
        Label titulo = new Label("Mini Golf");
        titulo.setFont(new Font(24));
        Label descripcion = new Label("Un juego de minigolf relajante en 2D donde la paciencia y la precisión lo son todo.");
        HBox precioBox = new HBox(10);
        Label descuento = new Label("-40%");
        descuento.getStyleClass().add("discount");
        Label precio = new Label("4,99€");
        precio.getStyleClass().add("price");
        precioBox.getChildren().addAll(descuento, precio);

        info.getChildren().addAll(titulo, descripcion, precioBox);
        hero.getChildren().addAll(img, info);
        return hero;
    }

    private VBox crearTarjetaJuego(Juego j, Long usuarioId) {
        VBox tarjeta = new VBox(5);
        tarjeta.getStyleClass().add("store-card");
        tarjeta.setPadding(new Insets(10));

        ImageView img = new ImageView(new Image(
                j.getImagen_url() != null ? j.getImagen_url() :
                        "file:src/main/resources/imagenes/default.png"
        ));
        img.setFitWidth(150);
        img.setPreserveRatio(true);

        Label titulo = new Label(j.getTitulo());
        Label descripcion = new Label(j.getDescripcion() != null ? j.getDescripcion() : "Sin descripción");
        Label precio = new Label(j.getPrecio() != null ? j.getPrecio() + "€" : "Gratis");
        precio.getStyleClass().add("price");

        Button comprar = new Button("Comprar");
        comprar.setOnAction(e -> {
            try {
                juegoService.comprarJuego(usuarioId, j.getId());
                comprar.setText("Comprado");
                comprar.setDisable(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo registrar la compra");
                alert.showAndWait();
            }
        });

        tarjeta.getChildren().addAll(img, titulo, descripcion, precio, comprar);
        return tarjeta;
    }
}
