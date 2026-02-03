package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.JuegoService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public class BibliotecaView extends BorderPane {

    private Label lblBiblioteca;
    private Label lblTienda;
    private Label lblPerfil;
    private JuegoService juegoService;

    public BibliotecaView(Usuario usuario) {
        this.juegoService = new JuegoService();

        HBox menu = new HBox(20);
        menu.getStyleClass().add("menu");
        menu.setPadding(new Insets(14));

        lblBiblioteca = new Label("Biblioteca");
        lblTienda = new Label("Tienda");
        lblPerfil = new Label("Perfil");

        menu.getChildren().addAll(
                lblBiblioteca,
                lblTienda,
                lblPerfil
        );


        // ===== ZONA CENTRAL =====
        HBox contenido = new HBox(30);
        contenido.setPadding(new Insets(30));

        // ----- JUEGOS -----
        VBox juegosBox = new VBox(15);
        Label tituloJuegos = new Label("Biblioteca");
        tituloJuegos.getStyleClass().add("section-title");

        TilePane juegosGrid = new TilePane();
        juegosGrid.setHgap(20);
        juegosGrid.setVgap(20);
        juegosGrid.setPrefColumns(3);

        try {
            List<Juego> juegos = juegoService.obtenerBiblioteca(usuario.getId());

            for (Juego j : juegos) {
                juegosGrid.getChildren().add(crearJuego(j.getImagen_url()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }


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

        setTop(menu);
        setCenter(contenido);


    }

    public void setMenuActions(Runnable irBiblioteca, Runnable irTienda, Runnable irPerfil) {
        lblBiblioteca.setOnMouseClicked(e -> irBiblioteca.run());
        lblTienda.setOnMouseClicked(e -> irTienda.run());
        lblPerfil.setOnMouseClicked(e -> irPerfil.run());
    }

    private ImageView crearJuego(String imagen) {
        ImageView img = new ImageView(
                new Image(getClass().getResourceAsStream("/images/" + imagen))
        );
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




}
