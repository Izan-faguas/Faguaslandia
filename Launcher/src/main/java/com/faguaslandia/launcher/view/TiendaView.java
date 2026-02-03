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

import java.util.List;

public class TiendaView extends VBox {

    private Label lblBiblioteca;
    private Label lblTienda;
    private Label lblPerfil;

    private final JuegoService juegoService;
    private final Usuario usuario;

    public TiendaView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();

        setSpacing(20);
        setPadding(new Insets(20));
        getStyleClass().add("tienda");

        // ===== HEADER =====
        HBox menu = new HBox(20);
        lblBiblioteca = new Label("Biblioteca");
        lblTienda = new Label("Tienda");
        lblPerfil = new Label("Perfil");

        menu.getChildren().addAll(lblBiblioteca, lblTienda, lblPerfil);
        getChildren().add(menu);

        // ===== LISTA DE JUEGOS =====
        VBox listaJuegosBox = new VBox(15);
        listaJuegosBox.getStyleClass().add("store-grid");

        try {
            List<Juego> juegos = juegoService.obtenerTodos();

            if (juegos.isEmpty()) {
                listaJuegosBox.getChildren().add(
                        new Label("No hay juegos disponibles")
                );
            } else {
                for (Juego j : juegos) {
                    listaJuegosBox.getChildren().add(
                            crearTarjetaJuego(j, usuario.getId())
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            listaJuegosBox.getChildren().add(
                    new Label("Error cargando la tienda")
            );
        }

        getChildren().add(listaJuegosBox);
    }

    // ===== NAVEGACIÓN =====
    public void setMenuActions(Runnable irBiblioteca, Runnable irTienda, Runnable irPerfil) {
        lblBiblioteca.setOnMouseClicked(e -> irBiblioteca.run());
        lblTienda.setOnMouseClicked(e -> irTienda.run());
        lblPerfil.setOnMouseClicked(e -> irPerfil.run());
    }

    // ===== TARJETA DE JUEGO =====
    private VBox crearTarjetaJuego(Juego j, Long usuarioId) {
        VBox tarjeta = new VBox(5);
        tarjeta.setPadding(new Insets(10));
        tarjeta.getStyleClass().add("store-card");

        // ===== CARGA SEGURA DE IMAGEN =====
        Image imgJuego;
        try {
            if (j.getImagen_url() != null && !j.getImagen_url().isEmpty()) {
                try {
                    // Intentamos cargar como recurso
                    imgJuego = new Image(getClass().getResourceAsStream("/images/" + j.getImagen_url()));
                    if (imgJuego.isError()) throw new Exception("Error al cargar imagen recurso");
                } catch (Exception ex) {
                    // Si falla, intentamos como URL absoluta
                    imgJuego = new Image(j.getImagen_url(), true);
                }
            } else {
                imgJuego = new Image(getClass().getResourceAsStream("/images/algo.png"));
            }
        } catch (Exception e) {
            // Fallback definitivo
            imgJuego = new Image(getClass().getResourceAsStream("/images/algo.png"));
        }

        ImageView img = new ImageView(imgJuego);
        img.setFitWidth(150);
        img.setPreserveRatio(true);

        // ===== DATOS DEL JUEGO =====
        Label titulo = new Label(j.getTitulo());
        Label descripcion = new Label(j.getDescripcion() != null ? j.getDescripcion() : "Sin descripción");
        Label precio = new Label(j.getPrecio() != null ? j.getPrecio() + "€" : "Gratis");
        precio.getStyleClass().add("price");

        // ===== BOTÓN DE COMPRA SEGURA =====
        Button comprar = new Button("Comprar"); // Siempre inicializado

        try {
            boolean comprado = juegoService.estaComprado(usuarioId, j.getId());
            if (comprado) {
                comprar.setText("Comprado");
                comprar.setDisable(true);
            }

            comprar.setOnAction(e -> {
                try {
                    juegoService.comprarJuego(usuarioId, j.getId());
                    comprar.setText("Comprado");
                    comprar.setDisable(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Error al comprar").showAndWait();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            comprar.setText("Error");
            comprar.setDisable(true);
        }

        // ===== AGREGAR COMPONENTES =====
        tarjeta.getChildren().addAll(img, titulo, descripcion, precio, comprar);
        return tarjeta;
    }
}
