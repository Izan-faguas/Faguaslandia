package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PerfilAmigoView extends VBox {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogroDTO {
        public String nombre;
        public String descripcion;
        public String icono;
        public String fechaDesbloqueo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JuegoDTO {
        public Long id;
        public String titulo;
        public String imagen_url;
    }

    private final Usuario usuarioActual;
    private final AmigosView.AmigoDTO amigo;
    private final ObjectMapper mapper;
    private Runnable onVolver;
    private Runnable onChatear;

    private Label lblNivel;
    private Label lblHoras;
    private Label lblJuegos;
    private Label lblLogros;
    private Region nivelBarraFill;
    private VBox listaLogrosBox;
    private VBox listaJuegosBox;

    public PerfilAmigoView(Usuario usuarioActual, AmigosView.AmigoDTO amigo) {
        this.usuarioActual = usuarioActual;
        this.amigo = amigo;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        getStyleClass().add("panel-center");

        construir();
        cargarDatos();
    }

    public void setOnVolver(Runnable r)   { this.onVolver = r; }
    public void setOnChatear(Runnable r)  { this.onChatear = r; }

    private void construir() {
        VBox contenido = new VBox(0);
        contenido.setMaxWidth(Double.MAX_VALUE);

        VBox hero = new VBox(16);
        hero.getStyleClass().add("perfil-hero");
        hero.setPadding(new Insets(32, 40, 28, 40));
        hero.setAlignment(Pos.TOP_LEFT);

        Button btnVolver = new Button("← Volver");
        btnVolver.getStyleClass().add("btn-secondary");
        btnVolver.setOnAction(e -> { if (onVolver != null) onVolver.run(); });

        StackPane avatar = crearAvatar(48);

        Label lblNombre = new Label(amigo.nombre);
        lblNombre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e6f0f8;");

        Label lblEstado = new Label(formatearEstado(amigo.estado));
        lblEstado.setStyle("-fx-font-size: 13px; -fx-text-fill: #9fb3c8;");

        lblNivel = new Label("Nivel —");
        lblNivel.getStyleClass().add("perfil-nivel-badge");

        StackPane barraStack = new StackPane();
        barraStack.getStyleClass().add("nivel-barra-bg");
        barraStack.setMaxWidth(180);
        barraStack.setAlignment(Pos.CENTER_LEFT);

        nivelBarraFill = new Region();
        nivelBarraFill.getStyleClass().add("nivel-barra-fill");
        nivelBarraFill.setPrefWidth(0);

        barraStack.getChildren().add(nivelBarraFill);

        HBox barraBox = new HBox(10, barraStack);
        barraBox.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(6, lblNombre, lblEstado, lblNivel, barraBox);

        HBox avatarInfo = new HBox(20, avatar, infoBox);
        avatarInfo.setAlignment(Pos.CENTER_LEFT);

        lblHoras  = new Label("—");
        lblJuegos = new Label("—");
        lblLogros = new Label("—");

        HBox statsRow = new HBox(20,
                crearStat("", lblHoras,  "Horas"),
                crearStat("", lblJuegos, "Juegos"),
                crearStat("", lblLogros, "Logros")
        );

        Button btnChatear = new Button("💬  Enviar mensaje");
        btnChatear.getStyleClass().add("btn-play");
        btnChatear.setOnAction(e -> { if (onChatear != null) onChatear.run(); });

        hero.getChildren().addAll(btnVolver, avatarInfo, statsRow, btnChatear);

        VBox secJuegos = crearSeccionJuegos();
        VBox secLogros = crearSeccionLogros();

        contenido.getChildren().addAll(hero, secJuegos, secLogros);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);
    }

    private VBox crearSeccionJuegos() {
        VBox sec = new VBox(12);
        sec.setPadding(new Insets(24, 40, 8, 40));

        Label titulo = new Label("Biblioteca");
        titulo.getStyleClass().add("perfil-section-title");

        listaJuegosBox = new VBox(8);
        Label cargando = new Label("Cargando juegos...");
        cargando.getStyleClass().add("amigos-vacio");
        listaJuegosBox.getChildren().add(cargando);

        sec.getChildren().addAll(titulo, listaJuegosBox);
        return sec;
    }

    private VBox crearSeccionLogros() {
        VBox sec = new VBox(12);
        sec.setPadding(new Insets(24, 40, 40, 40));

        Label titulo = new Label("Logros");
        titulo.getStyleClass().add("perfil-section-title");

        listaLogrosBox = new VBox(8);
        Label cargando = new Label("Cargando logros...");
        cargando.getStyleClass().add("amigos-vacio");
        listaLogrosBox.getChildren().add(cargando);

        sec.getChildren().addAll(titulo, listaLogrosBox);
        return sec;
    }


    private void cargarDatos() {
        cargarStats();
        cargarJuegos();
        cargarLogros();
    }

    private void cargarStats() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + amigo.id + "/stats";
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonNode j = mapper.readTree(resp.body());
                    Platform.runLater(() -> {
                        lblNivel.setText("Nivel " + j.get("nivel").asInt());
                        lblHoras.setText(j.get("horas").asInt() + "h");
                        lblJuegos.setText(j.get("juegos").asText());
                        lblLogros.setText(j.get("logros").asText());
                        double progreso = Math.min(j.get("progreso").asDouble(), 100);
                        nivelBarraFill.setPrefWidth(180 * progreso / 100.0);
                    });
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void cargarJuegos() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/compras/usuario/" + amigo.id;
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    List<JuegoDTO> juegos = mapper.readValue(resp.body(), new TypeReference<>() {});
                    Platform.runLater(() -> renderizarJuegos(juegos));
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    private void cargarLogros() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + amigo.id + "/logros";
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    List<LogroDTO> logros = mapper.readValue(resp.body(), new TypeReference<>() {});
                    Platform.runLater(() -> renderizarLogros(logros));
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }


    private void renderizarJuegos(List<JuegoDTO> juegos) {
        listaJuegosBox.getChildren().clear();
        if (juegos.isEmpty()) {
            Label empty = new Label("No tiene juegos en su biblioteca.");
            empty.getStyleClass().add("amigos-vacio");
            listaJuegosBox.getChildren().add(empty);
            return;
        }
        FlowPane grid = new FlowPane(10, 10);
        grid.setMaxWidth(Double.MAX_VALUE);
        for (JuegoDTO j : juegos) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.TOP_CENTER);
            card.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 8; -fx-padding: 8;");
            card.setPrefWidth(130);

            if (j.imagen_url != null) {
                String base = Config.IMG_BASE_URL + "/" + j.imagen_url.replaceAll("(?i)\\.png$", "");
                Image imgPortada = new Image(base + "920.png", true);
                ImageView img = new ImageView(imgPortada);
                img.setFitWidth(120);
                img.setFitHeight(70);
                img.setSmooth(true);
                imgPortada.errorProperty().addListener((obs, o, err) -> {
                    if (err) Platform.runLater(() ->
                            img.setImage(new Image(Config.IMG_BASE_URL + "/" + j.imagen_url, true))
                    );
                });
                card.getChildren().add(img);
            }

            Label nombre = new Label(j.titulo);
            nombre.setWrapText(true);
            nombre.setMaxWidth(120);
            nombre.setStyle("-fx-text-fill: #c7d5e0; -fx-font-size: 11px; -fx-alignment: center;");
            card.getChildren().add(nombre);

            grid.getChildren().add(card);
        }
        listaJuegosBox.getChildren().add(grid);
    }

    private void renderizarLogros(List<LogroDTO> logros) {
        listaLogrosBox.getChildren().clear();
        if (logros.isEmpty()) {
            Label empty = new Label("Aún no tiene logros desbloqueados.");
            empty.getStyleClass().add("amigos-vacio");
            listaLogrosBox.getChildren().add(empty);
            return;
        }
        for (LogroDTO l : logros) {
            HBox fila = new HBox(14);
            fila.getStyleClass().add("perfil-card");
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(12, 16, 12, 16));

            ImageView icono = new ImageView();
            icono.setFitWidth(36); icono.setFitHeight(36);
            icono.setSmooth(true); icono.setPreserveRatio(true);
            String iconoUrl = (l.icono != null && !l.icono.isBlank())
                    ? Config.IMG_BASE_URL + "/logros/" + l.icono
                    : Config.IMG_BASE_URL + "/logros/default.png";
            Image iconoImg = new Image(iconoUrl, true);
            iconoImg.errorProperty().addListener((obs, o, err) -> {
                if (err) Platform.runLater(() ->
                        icono.setImage(new Image(Config.IMG_BASE_URL + "/logros/default.png", true))
                );
            });
            icono.setImage(iconoImg);

            VBox info = new VBox(3);
            Label nombre = new Label(l.nombre);
            nombre.getStyleClass().add("amigo-nombre");
            Label desc = new Label(l.descripcion);
            desc.getStyleClass().add("amigo-estado-txt");
            desc.setWrapText(true);

            String fechaTexto = "";
            if (l.fechaDesbloqueo != null && l.fechaDesbloqueo.length() >= 10) {
                String[] p = l.fechaDesbloqueo.substring(0, 10).split("-");
                if (p.length == 3) fechaTexto = "Desbloqueado el " + p[2] + "/" + p[1] + "/" + p[0];
            }
            Label fecha = new Label(fechaTexto);
            fecha.setStyle("-fx-text-fill: #5e7a96; -fx-font-size: 11px;");

            info.getChildren().addAll(nombre, desc, fecha);
            HBox.setHgrow(info, Priority.ALWAYS);
            fila.getChildren().addAll(icono, info);
            listaLogrosBox.getChildren().add(fila);
        }
    }


    private StackPane crearAvatar(double size) {
        StackPane av = new StackPane();
        av.setMinSize(size, size);
        av.setMaxSize(size, size);
        av.getStyleClass().add("amigo-avatar");

        if (amigo.foto != null && !amigo.foto.isBlank()) {
            Image image = new Image(amigo.foto, true);
            ImageView img = new ImageView(image);
            img.setFitWidth(size);
            img.setFitHeight(size);
            Circle clip = new Circle(size / 2, size / 2, size / 2);
            img.setClip(clip);
            image.errorProperty().addListener((obs, old, err) -> {
                if (err) Platform.runLater(() -> {
                    av.getChildren().clear();
                    av.getChildren().add(letraAvatar(size));
                });
            });
            av.getChildren().add(img);
        } else {
            av.getChildren().add(letraAvatar(size));
        }

        Label dot = new Label();
        dot.getStyleClass().add("estado-dot");
        dot.getStyleClass().add("estado-dot-" + estadoDotClass(amigo.estado));
        dot.setMinSize(12, 12);
        dot.setMaxSize(12, 12);
        StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
        av.getChildren().add(dot);

        return av;
    }

    private Label letraAvatar(double size) {
        Label l = new Label(amigo.nombre != null && !amigo.nombre.isEmpty()
                ? String.valueOf(amigo.nombre.charAt(0)).toUpperCase() : "?");
        l.setStyle("-fx-text-fill: #e6f0f8; -fx-font-weight: bold; -fx-font-size: " + (size * 0.4) + "px;");
        return l;
    }

    private VBox crearStat(String icono, Label valor, String etiqueta) {
        Label ic = new Label(icono);
        ic.setStyle("-fx-font-size: 20px;");
        valor.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #66c0f4;");
        Label lbl = new Label(etiqueta);
        lbl.setStyle("-fx-text-fill: #5e7a96; -fx-font-size: 11px;");
        VBox box = new VBox(2, ic, valor, lbl);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 8; -fx-padding: 12 20 12 20;");
        return box;
    }

    private String formatearEstado(String estado) {
        return switch (estado == null ? "offline" : estado) {
            case "online"      -> "🟢 En línea";
            case "ausente"     -> "🟡 Ausente";
            case "no_molestar" -> "🔴 No molestar";
            case "invisible"   -> "⚫ Invisible";
            default            -> "⚫ Desconectado";
        };
    }

    private String estadoDotClass(String estado) {
        return switch (estado == null ? "offline" : estado) {
            case "online"      -> "online";
            case "ausente"     -> "ausente";
            case "no_molestar" -> "no-molestar";
            default            -> "offline";
        };
    }
}