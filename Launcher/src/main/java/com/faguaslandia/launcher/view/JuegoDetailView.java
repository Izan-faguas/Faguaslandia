package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Juego;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.faguaslandia.launcher.service.JuegoService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class JuegoDetailView extends VBox {

    private final JuegoService juegoService;
    private final Usuario usuario;
    private final ObjectMapper mapper;

    private Runnable callbackActualizarBiblioteca;
    private Runnable callbackVolver;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResenaDTO {
        public Long id;
        public UsuarioMinDTO usuario;
        public int puntuacion;
        public String comentario;
        public String fecha;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class UsuarioMinDTO {
            public Long id;
            public String nombre;
        }
    }

    public JuegoDetailView(Usuario usuario) {
        this.usuario = usuario;
        this.juegoService = new JuegoService();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        getStyleClass().add("panel-center");
    }

    public void setJuego(Juego juego) {
        getChildren().clear();

        ImageView portada = new ImageView();
        portada.setFitHeight(320);
        portada.setPreserveRatio(true);
        portada.setSmooth(true);
        setImagenConFallback(portada, juego.getImagen_url(), "1232");

        StackPane hero = new StackPane();
        hero.setMinHeight(320); hero.setMaxHeight(320);
        hero.getStyleClass().add("hero-container");
        hero.setStyle("-fx-background-color: #0a0f16;");
        portada.fitWidthProperty().bind(hero.widthProperty());
        portada.setPreserveRatio(false);
        hero.getChildren().add(portada);

        Button volver = new Button("Volver a la tienda");
        volver.getStyleClass().add("btn-action-gray");
        volver.setOnAction(e -> { if (callbackVolver != null) callbackVolver.run(); });
        StackPane.setAlignment(volver, Pos.TOP_LEFT);
        StackPane.setMargin(volver, new Insets(14, 0, 0, 18));

        VBox overlay = new VBox(6);
        overlay.getStyleClass().add("hero-overlay");
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(overlay, Pos.BOTTOM_LEFT);

        Label heroTitulo = new Label(juego.getTitulo());
        heroTitulo.getStyleClass().add("hero-titulo");

        HBox metaBox = new HBox(12);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (juego.getCategoria() != null) {
            Label tag = new Label(juego.getCategoria());
            tag.getStyleClass().add("hero-tag");
            metaBox.getChildren().add(tag);
        }
        StringBuilder metaTxt = new StringBuilder();
        if (juego.getDesarrollador() != null) metaTxt.append(juego.getDesarrollador());
        if (juego.getFecha_lanzamiento() != null) metaTxt.append(" · ").append(juego.getFecha_lanzamiento());
        if (!metaTxt.isEmpty()) {
            Label meta = new Label(metaTxt.toString());
            meta.getStyleClass().add("hero-meta");
            metaBox.getChildren().add(meta);
        }

        Label ratingLbl = new Label("★★★★★");
        ratingLbl.getStyleClass().add("hero-rating");
        ratingLbl.setText("Sin valoraciones aún");
        metaBox.getChildren().add(ratingLbl);

        overlay.getChildren().addAll(heroTitulo, metaBox);
        hero.getChildren().addAll(volver, overlay);

        String precioTxt = (juego.getPrecio() == null || juego.getPrecio().doubleValue() == 0)
                ? "Gratis" : juego.getPrecio() + "€";

        HBox statsRow = new HBox(10,
                crearStatCard(precioTxt, "Precio"),
                crearStatCard(juego.getCategoria() != null ? juego.getCategoria() : "—", "Categoría"),
                crearStatCard(juego.getDesarrollador() != null ? juego.getDesarrollador() : "—", "Desarrollador")
        );
        statsRow.getStyleClass().add("stats-row");

        Label desc = new Label(juego.getDescripcion() != null ? juego.getDescripcion() : "Sin descripción disponible.");
        desc.setWrapText(true);
        desc.getStyleClass().add("detalle-desc");
        desc.setPadding(new Insets(0, 32, 0, 32));

        Label precioBig = new Label(precioTxt);
        precioBig.getStyleClass().add("price-big");

        Button comprarBtn = new Button("Cargando...");
        comprarBtn.setDisable(true);
        comprarBtn.getStyleClass().add("btn-play-green");

        Label buyNote = new Label("Acceso instantáneo desde el Launcher");
        buyNote.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buyBlock = new HBox(16, precioBig, comprarBtn, spacer, buyNote);
        buyBlock.setAlignment(Pos.CENTER_LEFT);
        buyBlock.getStyleClass().add("buy-block");
        buyBlock.setPadding(new Insets(14, 32, 14, 32));

        VBox resenasSection = crearSeccionResenas(juego, comprarBtn, ratingLbl);

        VBox contenido = new VBox(0, statsRow, desc, buyBlock, resenasSection);
        VBox.setVgrow(resenasSection, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(new VBox(hero, contenido));
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);

        new Thread(() -> {
            try {
                boolean comprado = juegoService.estaComprado(usuario.getId(), juego.getId());
                Platform.runLater(() -> {
                    if (comprado) {
                        comprarBtn.setText("En tu biblioteca");
                        comprarBtn.getStyleClass().remove("btn-play-green");
                        comprarBtn.getStyleClass().add("btn-comprado");
                        comprarBtn.setDisable(true);
                    } else {
                        comprarBtn.setText("Comprar — " + precioTxt);
                        comprarBtn.setDisable(false);
                        comprarBtn.setOnAction(e -> {
                            comprarBtn.setDisable(true);
                            comprarBtn.setText("Procesando...");
                            new Thread(() -> {
                                try {
                                    juegoService.comprarJuego(usuario.getId(), juego.getId());
                                    Platform.runLater(() -> {
                                        comprarBtn.setText("En tu biblioteca");
                                        comprarBtn.getStyleClass().remove("btn-play-green");
                                        comprarBtn.getStyleClass().add("btn-comprado");
                                        if (callbackActualizarBiblioteca != null)
                                            callbackActualizarBiblioteca.run();
                                        cargarResenas(juego.getId(), resenasSection, true, ratingLbl);
                                    });
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Platform.runLater(() -> { comprarBtn.setDisable(false); comprarBtn.setText("Error. Reintentar"); });
                                }
                            }).start();
                        });
                    }
                    cargarResenas(juego.getId(), resenasSection, comprado, ratingLbl);
                });
            } catch (Exception e) {
                Platform.runLater(() -> { comprarBtn.setText("Error"); comprarBtn.setDisable(true); });
            }
        }).start();
    }


    private VBox crearSeccionResenas(Juego juego, Button comprarBtn, Label ratingLbl) {
        VBox section = new VBox(0);
        section.setPadding(new Insets(16, 32, 24, 32));
        section.setMaxWidth(Double.MAX_VALUE);

        VBox formulario = crearFormularioResena(juego);
        formulario.setVisible(false);
        formulario.setManaged(false);

        Label headerLista = new Label("RESEÑAS DE LA COMUNIDAD");
        headerLista.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #5b7a99; -fx-padding: 16 0 8 0;");

        VBox listaResenas = new VBox(0);
        listaResenas.setStyle("-fx-background-color: #0f1520; -fx-border-color: rgba(102,192,244,0.12); -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        section.getProperties().put("formulario", formulario);
        section.getProperties().put("listaResenas", listaResenas);
        section.getChildren().addAll(formulario, headerLista, listaResenas);
        return section;
    }

    private VBox crearFormularioResena(Juego juego) {
        VBox form = new VBox(10);
        form.setStyle("-fx-background-color: #0f1520; -fx-border-color: rgba(102,192,244,0.15); -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 16;");

        Label formTitulo = new Label("Tu reseña");
        formTitulo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #66c0f4;");

        HBox estrellas = new HBox(4);
        estrellas.setAlignment(Pos.CENTER_LEFT);
        Label[] stars = new Label[5];
        int[] puntuacion = {0};
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            stars[i] = new Label("☆");
            stars[i].getStyleClass().add("estrella");
            stars[i].setOnMouseClicked(e -> { puntuacion[0] = val; actualizarEstrellas(stars, val); });
            stars[i].setOnMouseEntered(e -> actualizarEstrellas(stars, val));
            stars[i].setOnMouseExited(e -> actualizarEstrellas(stars, puntuacion[0]));
            estrellas.getChildren().add(stars[i]);
        }

        TextArea comentario = new TextArea();
        comentario.setPromptText("Escribe tu opinión...");
        comentario.setWrapText(true);
        comentario.setPrefRowCount(3);
        comentario.setMaxWidth(Double.MAX_VALUE);
        comentario.setStyle(
                "-fx-control-inner-background: #080d13;" +
                        "-fx-background-color: #080d13;" +
                        "-fx-text-fill: #c7d5e0;" +
                        "-fx-prompt-text-fill: #4a6580;" +
                        "-fx-border-color: rgba(102,192,244,0.2);" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13px;"
        );

        Button guardar = new Button("Publicar reseña");
        guardar.getStyleClass().add("btn-play-green");
        Button eliminar = new Button("Eliminar reseña");
        eliminar.getStyleClass().add("btn-action-gray");
        eliminar.setVisible(false); eliminar.setManaged(false);
        Label msg = new Label("");
        msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #66c0f4;");

        HBox btns = new HBox(10, guardar, eliminar);
        cargarMiResena(juego.getId(), stars, puntuacion, comentario, eliminar);

        guardar.setOnAction(e -> {
            if (puntuacion[0] == 0) { msg.setText("Selecciona una puntuación"); return; }
            guardar.setDisable(true); guardar.setText("Guardando...");
            String com = comentario.getText().trim();
            int pun = puntuacion[0];
            new Thread(() -> {
                try {
                    String json = "{\"puntuacion\":%d,\"comentario\":\"%s\"}".formatted(pun, com.replace("\"", "\\\"").replace("\n", "\\n"));
                    HttpResponse<String> resp = AuthService.getClient().send(
                            HttpRequest.newBuilder().uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juego.getId()))
                                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                            HttpResponse.BodyHandlers.ofString());
                    Platform.runLater(() -> {
                        guardar.setDisable(false); guardar.setText("Publicar reseña");
                        if (resp.statusCode() == 200) {
                            msg.setText("Reseña guardada");
                            msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #4caf50;");
                            eliminar.setVisible(true); eliminar.setManaged(true);
                            VBox section = (VBox) form.getParent();
                            if (section != null) {
                                VBox lista = (VBox) section.getProperties().get("listaResenas");
                                Label rating = (Label) section.getProperties().get("ratingLbl");
                                if (lista != null) recargarListaResenas(juego.getId(), lista, rating);
                            }
                        } else { msg.setText("Error al guardar"); }
                    });
                } catch (Exception ex) { ex.printStackTrace(); Platform.runLater(() -> { guardar.setDisable(false); guardar.setText("Publicar reseña"); }); }
            }).start();
        });

        eliminar.setOnAction(e -> {
            eliminar.setDisable(true);
            new Thread(() -> {
                try {
                    HttpResponse<String> resp = AuthService.getClient().send(
                            HttpRequest.newBuilder().uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juego.getId())).DELETE().build(),
                            HttpResponse.BodyHandlers.ofString());
                    Platform.runLater(() -> {
                        eliminar.setDisable(false);
                        if (resp.statusCode() == 200) {
                            puntuacion[0] = 0; actualizarEstrellas(stars, 0); comentario.clear();
                            eliminar.setVisible(false); eliminar.setManaged(false);
                            msg.setText("Reseña eliminada");
                            msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #9fb3c8;");
                            VBox section = (VBox) form.getParent();
                            if (section != null) {
                                VBox lista = (VBox) section.getProperties().get("listaResenas");
                                Label rating = (Label) section.getProperties().get("ratingLbl");
                                if (lista != null) recargarListaResenas(juego.getId(), lista, rating);
                            }
                        }
                    });
                } catch (Exception ex) { ex.printStackTrace(); Platform.runLater(() -> eliminar.setDisable(false)); }
            }).start();
        });

        form.getChildren().addAll(formTitulo, estrellas, comentario, btns, msg);
        return form;
    }

    private void cargarMiResena(Long juegoId, Label[] stars, int[] puntuacion, TextArea comentario, Button eliminar) {
        new Thread(() -> {
            try {
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juegoId + "/mia")).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    ResenaDTO r = mapper.readValue(resp.body(), ResenaDTO.class);
                    Platform.runLater(() -> {
                        puntuacion[0] = r.puntuacion; actualizarEstrellas(stars, r.puntuacion);
                        if (r.comentario != null) comentario.setText(r.comentario);
                        eliminar.setVisible(true); eliminar.setManaged(true);
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void cargarResenas(Long juegoId, VBox section, boolean comprado, Label ratingLbl) {
        VBox lista      = (VBox) section.getProperties().get("listaResenas");
        VBox formulario = (VBox) section.getProperties().get("formulario");
        section.getProperties().put("ratingLbl", ratingLbl);
        if (formulario != null) { formulario.setVisible(comprado); formulario.setManaged(comprado); }
        recargarListaResenas(juegoId, lista, ratingLbl);
    }

    private void recargarListaResenas(Long juegoId, VBox lista, Label ratingLbl) {
        new Thread(() -> {
            try {
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juegoId)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                List<ResenaDTO> resenas = mapper.readValue(resp.body(), new TypeReference<>() {});

                double media = resenas.stream().mapToInt(r -> r.puntuacion).average().orElse(0);

                Platform.runLater(() -> {
                    if (ratingLbl != null) {
                        if (resenas.isEmpty()) {
                            ratingLbl.setText("Sin valoraciones aún");
                        } else {
                            ratingLbl.setText(estrellas((int) Math.round(media)) + String.format(" %.1f (%d reseñas)", media, resenas.size()));
                        }
                    }

                    lista.getChildren().clear();
                    if (resenas.isEmpty()) {
                        Label v = new Label("Aún no hay reseñas para este juego.");
                        v.setStyle("-fx-text-fill: #4a6580; -fx-font-size: 12px; -fx-padding: 14 18 14 18;");
                        lista.getChildren().add(v);
                        return;
                    }
                    for (ResenaDTO r : resenas) lista.getChildren().add(crearTarjetaResena(r));
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private VBox crearTarjetaResena(ResenaDTO r) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 12 18 12 18; -fx-border-color: transparent transparent rgba(102,192,244,0.08) transparent; -fx-border-width: 0 0 1 0;");

        String nombre = (r.usuario != null && r.usuario.nombre != null) ? r.usuario.nombre : "Usuario";
        boolean esMia = r.usuario != null && usuario.getId().equals(r.usuario.id);

        Label nombreLbl = new Label((esMia ? "⭐ " : "") + nombre);
        nombreLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + (esMia ? "#66c0f4" : "#c7d5e0") + ";");

        HBox starsBox = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Label s = new Label(i <= r.puntuacion ? "★" : "☆");
            s.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (i <= r.puntuacion ? "#f4c430" : "#3d5166") + ";");
            starsBox.getChildren().add(s);
        }

        HBox cabecera = new HBox(10, nombreLbl, starsBox);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(cabecera);

        if (r.comentario != null && !r.comentario.isBlank()) {
            Label txt = new Label(r.comentario);
            txt.setWrapText(true);
            txt.setStyle("-fx-text-fill: #8b9db0; -fx-font-size: 12px;");
            card.getChildren().add(txt);
        }
        return card;
    }


    private VBox crearStatCard(String valor, String etiqueta) {
        Label val = new Label(valor);
        val.getStyleClass().add("stat-valor");
        Label lbl = new Label(etiqueta.toUpperCase());
        lbl.getStyleClass().add("stat-label");
        VBox card = new VBox(3, val, lbl);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void setImagenConFallback(ImageView iv, String imagenUrl, String variante) {
        String base   = Config.IMG_BASE_URL + "/" + imagenUrl.replaceAll("(?i)\\.png$", "");
        String urlVar = base + variante + ".png";
        String urlFb  = Config.IMG_BASE_URL + "/" + imagenUrl;
        Image img = new Image(urlVar, true);
        iv.setImage(img);
        img.errorProperty().addListener((obs, o, err) -> {
            if (err) Platform.runLater(() -> iv.setImage(new Image(urlFb, true)));
        });
    }

    private void actualizarEstrellas(Label[] stars, int valor) {
        for (int i = 0; i < stars.length; i++) {
            if (i < valor) { stars[i].setText("★"); stars[i].setStyle("-fx-text-fill: #f4c430; -fx-font-size: 22px; -fx-cursor: hand;"); }
            else           { stars[i].setText("☆"); stars[i].setStyle("-fx-text-fill: #3d5166; -fx-font-size: 22px; -fx-cursor: hand;"); }
        }
    }

    private String estrellas(int n) {
        return "★".repeat(Math.max(0, n)) + "☆".repeat(Math.max(0, 5 - n));
    }

    public void setCallbackActualizarBiblioteca(Runnable r) { this.callbackActualizarBiblioteca = r; }
    public void setCallbackVolver(Runnable r)               { this.callbackVolver = r; }

    public Button getVolverBtn() {
        Button dummy = new Button("Volver");
        dummy.setOnAction(e -> { if (callbackVolver != null) callbackVolver.run(); });
        return dummy;
    }
}