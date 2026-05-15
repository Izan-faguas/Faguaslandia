package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class PerfilView extends VBox {

    private final Usuario usuario;
    private final ObjectMapper mapper = new ObjectMapper();

    private Label lblEstadoActual;
    private Label lblMensaje;
    private Label lblStatJuegos;
    private Label lblStatHoras;
    private Label lblStatLogros;

    public PerfilView(Usuario usuario) {
        this.usuario = usuario;

        getStyleClass().add("root");
        setSpacing(0);
        setMaxWidth(Double.MAX_VALUE);

        construirVista();
    }

    private void construirVista() {

        /* ───────────────────────────────────────────────
         * BANNER SUPERIOR + AVATAR
         * ─────────────────────────────────────────────── */
        StackPane banner = new StackPane();
        banner.getStyleClass().add("perfil-banner");
        banner.setMinHeight(200);
        banner.setMaxHeight(200);

        StackPane avatarWrapper = new StackPane();
        avatarWrapper.getStyleClass().add("perfil-avatar-wrapper");

        String inicial = usuario.getNombre() != null && !usuario.getNombre().isEmpty()
                ? usuario.getNombre().substring(0, 1).toUpperCase()
                : "?";

        Label avatarInitial = new Label(inicial);
        avatarInitial.getStyleClass().add("perfil-avatar-initial");

        if (usuario.getFoto() != null && !usuario.getFoto().isBlank() && !usuario.getFoto().equals("default_avatar.png")) {
            try {
                String fotoUrl = Config.IMG_BASE_URL + "/avatars/" + usuario.getFoto();
                ImageView avatarImg = new ImageView(new Image(fotoUrl, true));
                avatarImg.setFitWidth(100);
                avatarImg.setFitHeight(100);
                avatarImg.setClip(new Circle(50, 50, 50));
                avatarWrapper.getChildren().add(avatarImg);
            } catch (Exception e) {
                avatarWrapper.getChildren().add(avatarInitial);
            }
        } else {
            avatarWrapper.getChildren().add(avatarInitial);
        }

        StackPane bannerConAvatar = new StackPane(banner);
        StackPane.setAlignment(avatarWrapper, Pos.BOTTOM_CENTER);
        StackPane.setMargin(avatarWrapper, new Insets(0, 0, -50, 0));
        bannerConAvatar.getChildren().add(avatarWrapper);

        /* ───────────────────────────────────────────────
         * NOMBRE + TAG + ESTADO
         * ─────────────────────────────────────────────── */
        String estadoRaw = usuario.getEstado() != null ? usuario.getEstado() : "online";

        lblEstadoActual = new Label(formatearEstado(estadoRaw));
        lblEstadoActual.getStyleClass().add("perfil-estado-badge");

        Label lblNombre = new Label(usuario.getNombre());
        lblNombre.getStyleClass().add("perfil-nombre");

        Label lblTag = new Label("#" + usuario.getId());
        lblTag.getStyleClass().add("perfil-tag");

        VBox nombreBox = new VBox(6, lblNombre, lblTag, lblEstadoActual);
        nombreBox.setAlignment(Pos.CENTER);
        nombreBox.setPadding(new Insets(60, 0, 20, 0));

        /* ───────────────────────────────────────────────
         * STATS (Juegos / Horas / Logros)
         * ─────────────────────────────────────────────── */
        lblStatJuegos = new Label("—");
        lblStatHoras = new Label("—");
        lblStatLogros = new Label("—");

        HBox statsRow = new HBox(20,
                crearStatCard("🎮", "Juegos", lblStatJuegos),
                crearStatCard("⏱️", "Horas", lblStatHoras),
                crearStatCard("🏆", "Logros", lblStatLogros)
        );
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(0, 40, 30, 40));

        Region sep1 = crearSeparador();

        /* ───────────────────────────────────────────────
         * SECCIÓN ESTADO DE PRESENCIA
         * ─────────────────────────────────────────────── */
        VBox seccionEstado = crearSeccionEstado(estadoRaw);

        Region sep2 = crearSeparador();

        /* ───────────────────────────────────────────────
         * SECCIÓN INFO DE CUENTA
         * ─────────────────────────────────────────────── */
        VBox seccionCuenta = crearSeccionCuenta();

        /* ───────────────────────────────────────────────
         * SCROLL GENERAL
         * ─────────────────────────────────────────────── */
        VBox contenido = new VBox(
                bannerConAvatar,
                nombreBox,
                statsRow,
                sep1,
                seccionEstado,
                sep2,
                seccionCuenta
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(scroll);

        cargarStats();
    }

    /* ───────────────────────────────────────────────
     * TARJETAS DE STATS
     * ─────────────────────────────────────────────── */
    private VBox crearStatCard(String emoji, String titulo, Label lblValor) {
        Label ico = new Label(emoji);
        ico.setStyle("-fx-font-size: 24px;");

        Label tit = new Label(titulo);
        tit.getStyleClass().add("perfil-stat-titulo");

        lblValor.getStyleClass().add("perfil-stat-valor");

        VBox card = new VBox(6, ico, lblValor, tit);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("perfil-card");
        card.setPrefWidth(160);
        card.setPadding(new Insets(20));

        return card;
    }

    /* ───────────────────────────────────────────────
     * CARGAR STATS DESDE API
     * ─────────────────────────────────────────────── */
    private void cargarStats() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/stats";

                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                );

                if (resp.statusCode() == 200) {
                    JsonNode json = mapper.readTree(resp.body());

                    Platform.runLater(() -> {
                        lblStatJuegos.setText(json.get("juegos").asText());
                        lblStatHoras.setText(json.get("horas").asInt() + "h");
                        lblStatLogros.setText(json.get("logros").asText());
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /* ───────────────────────────────────────────────
     * SECCIÓN ESTADO DE PRESENCIA
     * ─────────────────────────────────────────────── */
    private VBox crearSeccionEstado(String estadoActual) {

        VBox seccion = new VBox(14);
        seccion.setPadding(new Insets(24, 40, 24, 40));

        Label titulo = new Label("Estado de presencia");
        titulo.getStyleClass().add("perfil-section-title");

        FlowPane botonesEstado = new FlowPane(10, 10);
        botonesEstado.setAlignment(Pos.CENTER_LEFT);

        String[][] opciones = {
                {"online", "🟢 En línea"},
                {"ausente", "🟡 Ausente"},
                {"no_molestar", "🔴 No molestar"},
                {"invisible", "⚫ Invisible"}
        };

        ToggleGroup tg = new ToggleGroup();

        for (String[] op : opciones) {
            String valor = op[0];
            String etiqueta = op[1];

            ToggleButton btn = new ToggleButton(etiqueta);
            btn.setToggleGroup(tg);
            btn.getStyleClass().add("estado-toggle");

            if (valor.equals(estadoActual)) {
                btn.setSelected(true);
                btn.getStyleClass().add("estado-toggle-active");
            }

            btn.selectedProperty().addListener((obs, oldV, newV) -> {
                if (newV) {
                    btn.getStyleClass().add("estado-toggle-active");
                    cambiarEstado(valor);
                } else {
                    btn.getStyleClass().remove("estado-toggle-active");
                }
            });

            botonesEstado.getChildren().add(btn);
        }

        lblMensaje = new Label("");
        lblMensaje.getStyleClass().add("perfil-msg");

        seccion.getChildren().addAll(titulo, botonesEstado, lblMensaje);
        return seccion;
    }

    /* ───────────────────────────────────────────────
     * SECCIÓN INFO DE CUENTA
     * ─────────────────────────────────────────────── */
    private VBox crearSeccionCuenta() {

        VBox seccion = new VBox(14);
        seccion.setPadding(new Insets(24, 40, 40, 40));

        Label titulo = new Label("Información de cuenta");
        titulo.getStyleClass().add("perfil-section-title");

        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("perfil-card");

        tarjeta.getChildren().addAll(
                crearFilaDato("👤  Nombre de usuario", usuario.getNombre()),
                crearSeparador(),
                crearFilaDato("✉️  Correo electrónico", usuario.getEmail()),
                crearSeparador(),
                crearFilaDato("🆔  ID de cuenta", String.valueOf(usuario.getId()))
        );

        seccion.getChildren().addAll(titulo, tarjeta);
        return seccion;
    }

    private HBox crearFilaDato(String clave, String valor) {
        Label lblClave = new Label(clave);
        lblClave.getStyleClass().add("perfil-dato-clave");
        lblClave.setMinWidth(220);

        Label lblValor = new Label(valor != null ? valor : "—");
        lblValor.getStyleClass().add("perfil-dato-valor");

        return new HBox(16, lblClave, lblValor);
    }

    private Region crearSeparador() {
        Region r = new Region();
        r.getStyleClass().add("separator");
        r.setMaxWidth(Double.MAX_VALUE);
        return r;
    }

    /* ───────────────────────────────────────────────
     * ESTADO → TEXTO
     * ─────────────────────────────────────────────── */
    private String formatearEstado(String raw) {
        return switch (raw) {
            case "ausente" -> "🟡 Ausente";
            case "no_molestar" -> "🔴 No molestar";
            case "invisible" -> "⚫ Invisible";
            default -> "🟢 En línea";
        };
    }

    /* ───────────────────────────────────────────────
     * CAMBIAR ESTADO EN API
     * ─────────────────────────────────────────────── */
    private void cambiarEstado(String nuevoEstado) {

        usuario.setEstado(nuevoEstado);
        lblEstadoActual.setText(formatearEstado(nuevoEstado));
        lblMensaje.setText("");

        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/estado";
                String json = "{\"estado\":\"" + nuevoEstado + "\"}";

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                AuthService.getClient().send(req, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> lblMensaje.setText("✅ Estado actualizado"));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> lblMensaje.setText("⚠️ No se pudo guardar el estado"));
            }
        }).start();
    }
}
