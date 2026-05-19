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

    // DTO interno para deserializar reseñas
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

        /* ── Imagen hero ── */
        String imgUrl = Config.IMG_BASE_URL + "/" + juego.getImagen_url();
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

        /* ── Sección de reseñas ── */
        VBox resenasSection = crearSeccionResenas(juego, comprarBtn);

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
                                        // Refrescar reseñas tras comprar (activa el formulario)
                                        cargarResenas(juego.getId(), resenasSection, true);
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
                    // Cargar reseñas ahora que sabemos si está comprado
                    cargarResenas(juego.getId(), resenasSection, comprado);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comprarBtn.setText("Error");
                    comprarBtn.setDisable(true);
                    cargarResenas(juego.getId(), resenasSection, false);
                });
            }
        }).start();

        /* ── Separador ── */
        Region sep = new Region();
        sep.getStyleClass().add("separator");
        sep.setMaxWidth(Double.MAX_VALUE);

        /* ── Ensamblar en scroll ── */
        VBox contenido = new VBox(16, volver, tituloPrecio, desc, meta, sep, comprarBtn, resenasSection);
        contenido.setPadding(new Insets(28, 32, 28, 32));
        contenido.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);
    }

    // ─────────────────────────────────────────────────────
    // SECCIÓN RESEÑAS
    // ─────────────────────────────────────────────────────

    private VBox crearSeccionResenas(Juego juego, Button comprarBtn) {
        VBox section = new VBox(12);
        section.setMaxWidth(Double.MAX_VALUE);

        Region sep2 = new Region();
        sep2.getStyleClass().add("separator");
        sep2.setMaxWidth(Double.MAX_VALUE);

        Label titulo = new Label("💬  Reseñas de la comunidad");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e6f0f8;");

        // Contenedor de lista (se rellena en cargarResenas)
        VBox listaResenas = new VBox(8);
        listaResenas.setId("lista-resenas-" + juego.getId());

        // Formulario de reseña propia (oculto hasta saber si está comprado)
        VBox formulario = crearFormularioResena(juego, listaResenas);
        formulario.setId("form-resena-" + juego.getId());
        formulario.setVisible(false);
        formulario.setManaged(false);

        section.getChildren().addAll(sep2, titulo, listaResenas, formulario);
        // Guardamos referencia al formulario en properties para acceder desde cargarResenas
        section.getProperties().put("formulario", formulario);
        section.getProperties().put("listaResenas", listaResenas);
        section.getProperties().put("juegoId", juego.getId());

        return section;
    }

    private VBox crearFormularioResena(Juego juego, VBox listaResenas) {
        VBox form = new VBox(10);
        form.getStyleClass().add("resena-form");

        Label formTitulo = new Label("Tu reseña");
        formTitulo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #66c0f4;");

        // Selector de estrellas
        HBox estrellas = new HBox(4);
        estrellas.setAlignment(Pos.CENTER_LEFT);
        Label[] stars = new Label[5];
        int[] puntuacionSeleccionada = {0};

        for (int i = 0; i < 5; i++) {
            final int valor = i + 1;
            stars[i] = new Label("☆");
            stars[i].getStyleClass().add("estrella");
            stars[i].setOnMouseClicked(e -> {
                puntuacionSeleccionada[0] = valor;
                actualizarEstrellas(stars, valor);
            });
            stars[i].setOnMouseEntered(e -> actualizarEstrellas(stars, valor));
            stars[i].setOnMouseExited(e -> actualizarEstrellas(stars, puntuacionSeleccionada[0]));
            estrellas.getChildren().add(stars[i]);
        }

        // Área de texto
        TextArea comentarioField = new TextArea();
        comentarioField.setStyle("-fx-text-fill: #e6f0f8; -fx-control-inner-background: #1a2535;");
        comentarioField.setPromptText("Escribe tu opinión sobre el juego...");
        comentarioField.setWrapText(true);
        comentarioField.setPrefRowCount(3);
        comentarioField.setMaxWidth(Double.MAX_VALUE);
        comentarioField.getStyleClass().add("resena-textarea");

        // Botones
        Button guardarBtn = new Button("Publicar reseña");
        guardarBtn.getStyleClass().add("btn-buy");

        Button eliminarBtn = new Button("Eliminar reseña");
        eliminarBtn.getStyleClass().add("btn-secondary");
        eliminarBtn.setVisible(false);
        eliminarBtn.setManaged(false);

        Label mensajeForm = new Label("");
        mensajeForm.setStyle("-fx-font-size: 12px; -fx-text-fill: #66c0f4;");

        HBox botonesRow = new HBox(10, guardarBtn, eliminarBtn);
        botonesRow.setAlignment(Pos.CENTER_LEFT);

        // Cargar reseña propia si existe
        cargarMiResena(juego.getId(), stars, puntuacionSeleccionada, comentarioField, eliminarBtn);

        // Acción guardar
        guardarBtn.setOnAction(e -> {
            if (puntuacionSeleccionada[0] == 0) {
                mensajeForm.setText("⚠ Selecciona una puntuación");
                mensajeForm.setStyle("-fx-font-size: 12px; -fx-text-fill: #e57373;");
                return;
            }
            guardarBtn.setDisable(true);
            guardarBtn.setText("Guardando...");
            int pun = puntuacionSeleccionada[0];
            String com = comentarioField.getText().trim();

            new Thread(() -> {
                try {
                    String json = "{\"puntuacion\":%d,\"comentario\":\"%s\"}"
                            .formatted(pun, com.replace("\"", "\\\"").replace("\n", "\\n"));
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juego.getId()))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();
                    HttpResponse<String> resp = AuthService.getClient().send(req, HttpResponse.BodyHandlers.ofString());

                    Platform.runLater(() -> {
                        guardarBtn.setDisable(false);
                        guardarBtn.setText("Publicar reseña");
                        if (resp.statusCode() == 200) {
                            mensajeForm.setText("✔ Reseña guardada");
                            mensajeForm.setStyle("-fx-font-size: 12px; -fx-text-fill: #4caf50;");
                            eliminarBtn.setVisible(true);
                            eliminarBtn.setManaged(true);
                            // Refrescar lista
                            recargarListaResenas(juego.getId(), listaResenas);
                        } else {
                            mensajeForm.setText("❌ Error al guardar");
                            mensajeForm.setStyle("-fx-font-size: 12px; -fx-text-fill: #e57373;");
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        guardarBtn.setDisable(false);
                        guardarBtn.setText("Publicar reseña");
                        mensajeForm.setText("❌ Error de conexión");
                        mensajeForm.setStyle("-fx-font-size: 12px; -fx-text-fill: #e57373;");
                    });
                }
            }).start();
        });

        // Acción eliminar
        eliminarBtn.setOnAction(e -> {
            eliminarBtn.setDisable(true);
            new Thread(() -> {
                try {
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juego.getId()))
                            .DELETE()
                            .build();
                    HttpResponse<String> resp = AuthService.getClient().send(req, HttpResponse.BodyHandlers.ofString());

                    Platform.runLater(() -> {
                        eliminarBtn.setDisable(false);
                        if (resp.statusCode() == 200) {
                            puntuacionSeleccionada[0] = 0;
                            actualizarEstrellas(stars, 0);
                            comentarioField.clear();
                            eliminarBtn.setVisible(false);
                            eliminarBtn.setManaged(false);
                            mensajeForm.setText("Reseña eliminada");
                            mensajeForm.setStyle("-fx-font-size: 12px; -fx-text-fill: #9fb3c8;");
                            recargarListaResenas(juego.getId(), listaResenas);
                        } else {
                            mensajeForm.setText("❌ Error al eliminar");
                            mensajeForm.setStyle("-fx-font-size: 12px; -fx-text-fill: #e57373;");
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> eliminarBtn.setDisable(false));
                }
            }).start();
        });

        form.getChildren().addAll(formTitulo, estrellas, comentarioField, botonesRow, mensajeForm);
        return form;
    }

    private void cargarMiResena(Long juegoId, Label[] stars, int[] puntuacionSeleccionada,
                                TextArea comentarioField, Button eliminarBtn) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juegoId + "/mia"))
                        .GET()
                        .build();
                HttpResponse<String> resp = AuthService.getClient().send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() == 200) {
                    ResenaDTO resena = mapper.readValue(resp.body(), ResenaDTO.class);
                    Platform.runLater(() -> {
                        puntuacionSeleccionada[0] = resena.puntuacion;
                        actualizarEstrellas(stars, resena.puntuacion);
                        if (resena.comentario != null) comentarioField.setText(resena.comentario);
                        eliminarBtn.setVisible(true);
                        eliminarBtn.setManaged(true);
                    });
                }
                // 404 = no tiene reseña, no pasa nada
            } catch (Exception ex) {
                // Silencioso — es normal que no haya reseña propia
            }
        }).start();
    }

    private void cargarResenas(Long juegoId, VBox resenasSection, boolean comprado) {
        VBox listaResenas = (VBox) resenasSection.getProperties().get("listaResenas");
        VBox formulario   = (VBox) resenasSection.getProperties().get("formulario");

        // Mostrar u ocultar formulario según si tiene el juego
        if (formulario != null) {
            formulario.setVisible(comprado);
            formulario.setManaged(comprado);
        }

        recargarListaResenas(juegoId, listaResenas);
    }

    private void recargarListaResenas(Long juegoId, VBox listaResenas) {
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(Config.API_BASE_URL + "/resenas/juego/" + juegoId))
                        .GET()
                        .build();
                HttpResponse<String> resp = AuthService.getClient().send(req, HttpResponse.BodyHandlers.ofString());
                List<ResenaDTO> resenas = mapper.readValue(resp.body(), new TypeReference<>() {});

                Platform.runLater(() -> {
                    listaResenas.getChildren().clear();
                    if (resenas.isEmpty()) {
                        Label vacio = new Label("Aún no hay reseñas para este juego.");
                        vacio.setStyle("-fx-text-fill: #5b7a99; -fx-font-size: 12px;");
                        listaResenas.getChildren().add(vacio);
                        return;
                    }
                    for (ResenaDTO r : resenas) {
                        listaResenas.getChildren().add(crearTarjetaResena(r));
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    listaResenas.getChildren().clear();
                    Label err = new Label("Error cargando reseñas.");
                    err.setStyle("-fx-text-fill: #e57373; -fx-font-size: 12px;");
                    listaResenas.getChildren().add(err);
                });
            }
        }).start();
    }

    private VBox crearTarjetaResena(ResenaDTO r) {
        VBox card = new VBox(4);
        card.getStyleClass().add("resena-card");

        String nombre = (r.usuario != null && r.usuario.nombre != null) ? r.usuario.nombre : "Usuario";
        boolean esMia = r.usuario != null && usuario.getId().equals(r.usuario.id);

        Label nombreLbl = new Label((esMia ? "⭐ " : "") + nombre);
        nombreLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (esMia ? "#66c0f4" : "#c7d5e0") + "; -fx-font-size: 13px;");

        // Estrellas visuales (solo lectura)
        HBox estrellas = new HBox(2);
        for (int i = 1; i <= 5; i++) {
            Label s = new Label(i <= r.puntuacion ? "★" : "☆");
            s.setStyle("-fx-text-fill: " + (i <= r.puntuacion ? "#f4c430" : "#3d5166") + "; -fx-font-size: 14px;");
            estrellas.getChildren().add(s);
        }

        HBox cabecera = new HBox(10, nombreLbl, estrellas);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().add(cabecera);

        if (r.comentario != null && !r.comentario.isBlank()) {
            Label comentario = new Label(r.comentario);
            comentario.setWrapText(true);
            comentario.setStyle("-fx-text-fill: #dce8f0; -fx-font-size: 12px;");
            card.getChildren().add(comentario);
        }

        return card;
    }

    // ─────────────────────────────────────────────────────
    // UTILS
    // ─────────────────────────────────────────────────────

    private void actualizarEstrellas(Label[] stars, int valor) {
        for (int i = 0; i < stars.length; i++) {
            if (i < valor) {
                stars[i].setText("★");
                stars[i].setStyle("-fx-text-fill: #f4c430; -fx-font-size: 22px; -fx-cursor: hand;");
            } else {
                stars[i].setText("☆");
                stars[i].setStyle("-fx-text-fill: #3d5166; -fx-font-size: 22px; -fx-cursor: hand;");
            }
        }
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