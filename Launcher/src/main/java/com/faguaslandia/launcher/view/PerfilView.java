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
import javafx.scene.layout.FlowPane;

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
        construir();
    }

    private void construir() {

        // ── BANNER ──────────────────────────────────────────
        StackPane banner = new StackPane();
        banner.getStyleClass().add("perfil-banner");
        banner.setMinHeight(200);
        banner.setMaxHeight(200);
        banner.setMaxWidth(Double.MAX_VALUE);

        // ── AVATAR centrado sobre el banner ─────────────────
        StackPane avatarWrapper = new StackPane();
        avatarWrapper.getStyleClass().add("perfil-avatar-wrapper");

        String inicial = usuario.getNombre() != null && !usuario.getNombre().isEmpty()
                ? String.valueOf(usuario.getNombre().charAt(0)).toUpperCase() : "?";
        Label avatarInitial = new Label(inicial);
        avatarInitial.getStyleClass().add("perfil-avatar-initial");

        if (usuario.getFoto() != null && !usuario.getFoto().isBlank()
                && !usuario.getFoto().equals("default_avatar.png")) {
            try {
                String fotoUrl = Config.IMG_BASE_URL + "/avatars/" + usuario.getFoto();
                ImageView avatarImg = new ImageView(new Image(fotoUrl, true));
                avatarImg.setFitWidth(100);
                avatarImg.setFitHeight(100);
                Circle clip = new Circle(50, 50, 50);
                avatarImg.setClip(clip);
                avatarWrapper.getChildren().add(avatarImg);
            } catch (Exception e) {
                avatarWrapper.getChildren().add(avatarInitial);
            }
        } else {
            avatarWrapper.getChildren().add(avatarInitial);
        }

        // Banner + avatar superpuesto centrado
        StackPane bannerConAvatar = new StackPane();
        bannerConAvatar.setMaxWidth(Double.MAX_VALUE);
        bannerConAvatar.getChildren().add(banner);
        StackPane.setAlignment(avatarWrapper, Pos.BOTTOM_CENTER);
        StackPane.setMargin(avatarWrapper, new Insets(0, 0, -50, 0));
        bannerConAvatar.getChildren().add(avatarWrapper);
        bannerConAvatar.setMinHeight(200);

        // ── NOMBRE + ESTADO ──────────────────────────────────
        String estadoRaw = usuario.getEstado() != null ? usuario.getEstado() : "online";
        lblEstadoActual = new Label(formatearEstado(estadoRaw));
        lblEstadoActual.getStyleClass().add("perfil-estado-badge");

        Label lblNombre = new Label(usuario.getNombre() != null ? usuario.getNombre() : "Usuario");
        lblNombre.getStyleClass().add("perfil-nombre");
        lblNombre.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e6f0f8;");

        Label lblTag = new Label("#" + usuario.getId());
        lblTag.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b7a99;");

        VBox nombreBox = new VBox(6, lblNombre, lblTag, lblEstadoActual);
        nombreBox.setAlignment(Pos.CENTER);
        nombreBox.setPadding(new Insets(60, 0, 20, 0));

        // ── STATS ────────────────────────────────────────────
        lblStatJuegos = new Label("—");
        lblStatHoras  = new Label("—");
        lblStatLogros = new Label("—");

        HBox statsRow = new HBox(20,
                crearStatCard("🎮", "Juegos", lblStatJuegos),
                crearStatCard("⏱️", "Horas", lblStatHoras),
                crearStatCard("🏆", "Logros", lblStatLogros)
        );
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(0, 40, 30, 40));

        // ── SEPARADOR ────────────────────────────────────────
        Region sep1 = new Region();
        sep1.getStyleClass().add("separator");
        sep1.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep1, new Insets(0, 40, 0, 40));

        // ── ESTADO DE PRESENCIA ──────────────────────────────
        VBox seccionEstado = crearSeccionEstado(estadoRaw);

        // ── SEPARADOR ────────────────────────────────────────
        Region sep2 = new Region();
        sep2.getStyleClass().add("separator");
        sep2.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep2, new Insets(0, 40, 0, 40));

        // ── INFO CUENTA ──────────────────────────────────────
        VBox seccionCuenta = crearSeccionCuenta();

        // ── SCROLL ───────────────────────────────────────────
        VBox contenido = new VBox(0,
                bannerConAvatar, nombreBox, statsRow,
                sep1, seccionEstado, sep2, seccionCuenta
        );
        contenido.setMaxWidth(Double.MAX_VALUE);
        contenido.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(scroll);

        // Cargar stats async
        cargarStats();
    }

    private VBox crearStatCard(String emoji, String titulo, Label lblValor) {
        Label ico = new Label(emoji);
        ico.setStyle("-fx-font-size: 24px;");

        Label tit = new Label(titulo);
        tit.setStyle("-fx-text-fill: #9fb3c8; -fx-font-size: 11px; -fx-font-weight: bold;");

        lblValor.setStyle("-fx-text-fill: #e6f0f8; -fx-font-size: 22px; -fx-font-weight: bold;");

        VBox card = new VBox(6, ico, lblValor, tit);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("perfil-card");
        card.setPrefWidth(160);
        card.setPadding(new Insets(20));

        return card;
    }

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
                        lblStatJuegos.setText(String.valueOf(json.get("juegos").asInt()));
                        lblStatHoras.setText(json.get("horas").asInt() + "h");
                        lblStatLogros.setText(String.valueOf(json.get("logros").asInt()));
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private VBox crearSeccionEstado(String estadoActual) {
        VBox seccion = new VBox(14);
        seccion.setPadding(new Insets(24, 40, 24, 40));

        Label titulo = new Label("Estado de presencia");
        titulo.getStyleClass().add("perfil-section-title");

        FlowPane botonesEstado = new FlowPane(10, 10);
        botonesEstado.setAlignment(Pos.CENTER_LEFT);

        String[][] opciones = {
                {"online",      "🟢 En línea"},
                {"ausente",     "🟡 Ausente"},
                {"no_molestar", "🔴 No molestar"},
                {"invisible",   "⚫ Invisible"}
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
            btn.selectedProperty().addListener((obs, was, now) -> {
                if (now) { btn.getStyleClass().add("estado-toggle-active"); cambiarEstado(valor); }
                else      btn.getStyleClass().remove("estado-toggle-active");
            });
            botonesEstado.getChildren().add(btn);
        }

        lblMensaje = new Label("");
        lblMensaje.getStyleClass().add("perfil-msg");

        seccion.getChildren().addAll(titulo, botonesEstado, lblMensaje);
        return seccion;
    }

    private VBox crearSeccionCuenta() {
        VBox seccion = new VBox(14);
        seccion.setPadding(new Insets(24, 40, 40, 40));

        Label titulo = new Label("Información de cuenta");
        titulo.getStyleClass().add("perfil-section-title");

        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("perfil-card");
        tarjeta.getChildren().addAll(
                crearFilaDato("👤  Nombre de usuario", usuario.getNombre()),
                crearSepFino(),
                crearFilaDato("✉️  Correo electrónico", usuario.getEmail()),
                crearSepFino(),
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

        HBox fila = new HBox(16, lblClave, lblValor);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private Region crearSepFino() {
        Region r = new Region();
        r.getStyleClass().add("separator");
        r.setMaxWidth(Double.MAX_VALUE);
        return r;
    }

    private String formatearEstado(String raw) {
        return switch (raw == null ? "online" : raw) {
            case "ausente"     -> "🟡 Ausente";
            case "no_molestar" -> "🔴 No molestar";
            case "invisible"   -> "⚫ Invisible";
            default            -> "🟢 En línea";
        };
    }

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