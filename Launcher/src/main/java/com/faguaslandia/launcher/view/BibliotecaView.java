package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public class BibliotecaView extends BorderPane {

    private final JuegoService juegoService;
    private final Usuario usuario;

    private TilePane juegosGrid;
    private HeaderView header;

    public BibliotecaView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();

        // ===== HEADER =====
        header = new HeaderView();
        header.setActions(
                this::irBiblioteca,
                this::irTienda,
                this::irPerfil
        );
        setTop(header);

        // ===== CONTENIDO INICIAL =====
        setCenter(crearContenidoBiblioteca());
    }

    // =====================================================
    // =============== NAVEGACIÓN ==========================
    // =====================================================

    private void irBiblioteca() {
        setCenter(crearContenidoBiblioteca());
    }

    private void irTienda() {
        TiendaView tienda = new TiendaView(usuario);

        tienda.setCallbackJuegoDetalle(this::irDetalleJuego);

        setCenter(tienda);
    }

    private void irDetalleJuego(Juego juego) {
        JuegoDetailView detalle = new JuegoDetailView(usuario);

        detalle.setJuego(juego);
        detalle.setCallbackActualizarBiblioteca(this::actualizarBiblioteca);

        detalle.setCallbackVolver(this::irBiblioteca);

        setCenter(detalle);
    }


    private void irPerfil() {
        setCenter(new Label("Perfil (pendiente)"));
    }

    // =====================================================
    // =============== CONTENIDO BIBLIOTECA ================
    // =====================================================

    private Node crearContenidoBiblioteca() {
        HBox contenido = new HBox(30);
        contenido.setPadding(new Insets(30));

        // ----- JUEGOS -----
        VBox juegosBox = new VBox(15);
        Label tituloJuegos = new Label("Biblioteca");
        tituloJuegos.getStyleClass().add("section-title");

        juegosGrid = new TilePane();
        juegosGrid.setHgap(20);
        juegosGrid.setVgap(20);
        juegosGrid.setPrefColumns(3);

        actualizarBiblioteca();

        juegosBox.getChildren().addAll(tituloJuegos, juegosGrid);
        HBox.setHgrow(juegosBox, Priority.ALWAYS);

        // ----- AMIGOS -----
        VBox amigosBox = new VBox(15);
        Label tituloAmigos = new Label("Amigos");
        tituloAmigos.getStyleClass().add("section-title");

        VBox listaAmigos = new VBox(12);
        listaAmigos.getChildren().addAll(
                crearAmigo("toni", "Activo"),
                crearAmigo("pau", "Activo")
        );

        amigosBox.getChildren().addAll(tituloAmigos, listaAmigos);
        amigosBox.setPrefWidth(200);

        contenido.getChildren().addAll(juegosBox, amigosBox);
        return contenido;
    }

    // =====================================================
    // =============== HELPERS ==============================
    // =====================================================

    private ImageView crearJuego(String imagen) {
        ImageView img;
        try {
            img = new ImageView(new Image(
                    getClass().getResourceAsStream("/images/" + imagen)
            ));
        } catch (Exception e) {
            img = new ImageView(new Image(
                    getClass().getResourceAsStream("/images/algo.png")
            ));
        }

        img.setFitWidth(140);
        img.setPreserveRatio(true);
        img.getStyleClass().add("game-cover");
        return img;
    }

    private VBox crearAmigo(String nombre, String estado) {
        VBox box = new VBox(4);
        box.getStyleClass().add("amigop");
        box.getChildren().addAll(
                new Label(nombre),
                new Label(estado)
        );
        return box;
    }

    public void actualizarBiblioteca() {
        try {
            List<Juego> juegos = juegoService.obtenerBiblioteca(usuario.getId());
            juegosGrid.getChildren().clear();
            for (Juego j : juegos) {
                juegosGrid.getChildren().add(crearJuego(j.getImagen_url()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
