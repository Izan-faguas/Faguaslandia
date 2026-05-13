package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Usuario;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PerfilView extends VBox {

    private final Usuario usuario;
    private Label lblEstadoActual;
    private ComboBox<String> comboEstado;
    private Label lblMensaje;

    // Estados disponibles con emojis
    private static final String[] ESTADOS = {
        "🟢 En línea",
        "🟡 Ausente",
        "🔴 No molestar",
        "⚫ Invisible"
    };

    public PerfilView(Usuario usuario) {
        this.usuario = usuario;
        getStyleClass().add("root");
        setAlignment(Pos.TOP_CENTER);
        setSpacing(0);

        construir();
    }

    private void construir() {
        // ── Banner superior ──
        StackPane banner = new StackPane();
        banner.getStyleClass().add("perfil-banner");
        banner.setMinHeight(160);
        banner.setMaxHeight(160);

        // ── Avatar ──
        StackPane avatarWrapper = new StackPane();
        avatarWrapper.getStyleClass().add("perfil-avatar-wrapper");

        Label avatarInitial = new Label(
            usuario.getNombre() != null && !usuario.getNombre().isEmpty()
                ? String.valueOf(usuario.getNombre().charAt(0)).toUpperCase()
                : "?"
        );
        avatarInitial.getStyleClass().add("perfil-avatar-initial");

        Circle clip = new Circle(50, 50, 50);

        if (usuario.getFoto() != null && !usuario.getFoto().isBlank()) {
            try {
                ImageView avatarImg = new ImageView(new Image(usuario.getFoto(), true));
                avatarImg.setFitWidth(100);
                avatarImg.setFitHeight(100);
                avatarImg.setClip(clip);
                avatarWrapper.getChildren().add(avatarImg);
            } catch (Exception e) {
                avatarWrapper.getChildren().add(avatarInitial);
            }
        } else {
            avatarWrapper.getChildren().add(avatarInitial);
        }

        // ── Contenedor principal centrado ──
        VBox contenido = new VBox(0);
        contenido.setMaxWidth(700);
        contenido.setMinWidth(700);
        contenido.setAlignment(Pos.TOP_CENTER);

        // Banner + avatar superpuesto
        StackPane bannerConAvatar = new StackPane();
        bannerConAvatar.getChildren().add(banner);
        StackPane.setAlignment(avatarWrapper, Pos.BOTTOM_LEFT);
        StackPane.setMargin(avatarWrapper, new Insets(0, 0, -50, 40));
        bannerConAvatar.getChildren().add(avatarWrapper);
        bannerConAvatar.setMinHeight(160);

        // ── Info del usuario ──
        VBox infoBox = new VBox(6);
        infoBox.setPadding(new Insets(60, 40, 30, 40));
        infoBox.setAlignment(Pos.TOP_LEFT);

        Label lblNombre = new Label(usuario.getNombre() != null ? usuario.getNombre() : "Usuario");
        lblNombre.getStyleClass().add("perfil-nombre");

        Label lblEmail = new Label(usuario.getEmail() != null ? usuario.getEmail() : "");
        lblEmail.getStyleClass().add("perfil-email");

        // Estado actual
        String estadoRaw = usuario.getEstado() != null ? usuario.getEstado() : "online";
        lblEstadoActual = new Label(formatearEstado(estadoRaw));
        lblEstadoActual.getStyleClass().add("perfil-estado-badge");

        HBox badgeRow = new HBox(10, lblEstadoActual);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        infoBox.getChildren().addAll(lblNombre, lblEmail, badgeRow);

        // ── Separador ──
        Region sep = new Region();
        sep.getStyleClass().add("separator");
        sep.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep, new Insets(0, 40, 0, 40));

        // ── Sección cambiar estado ──
        VBox seccionEstado = crearSeccionEstado(estadoRaw);

        // ── Sección info de cuenta ──
        VBox seccionCuenta = crearSeccionCuenta();

        contenido.getChildren().addAll(bannerConAvatar, infoBox, sep, seccionEstado, seccionCuenta);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Centrar el contenido con márgenes laterales
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.getChildren().add(scroll);
        HBox.setHgrow(scroll, Priority.ALWAYS);

        VBox.setVgrow(wrapper, Priority.ALWAYS);
        getChildren().add(wrapper);
    }

    private VBox crearSeccionEstado(String estadoActual) {
        VBox seccion = new VBox(14);
        seccion.setPadding(new Insets(24, 40, 10, 40));

        Label titulo = new Label("Estado de presencia");
        titulo.getStyleClass().add("perfil-section-title");

        // Botones de estado
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
                if (now) {
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

    private VBox crearSeccionCuenta() {
        VBox seccion = new VBox(14);
        seccion.setPadding(new Insets(24, 40, 40, 40));

        Region sep = new Region();
        sep.getStyleClass().add("separator");
        sep.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep, new Insets(0, 0, 10, 0));

        Label titulo = new Label("Información de cuenta");
        titulo.getStyleClass().add("perfil-section-title");

        // Tarjeta de datos
        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("perfil-card");

        tarjeta.getChildren().addAll(
            crearFilaDato("👤  Nombre de usuario", usuario.getNombre()),
            crearSepFino(),
            crearFilaDato("✉️  Correo electrónico", usuario.getEmail()),
            crearSepFino(),
            crearFilaDato("🆔  ID de cuenta", String.valueOf(usuario.getId()))
        );

        seccion.getChildren().addAll(sep, titulo, tarjeta);
        return seccion;
    }

    private HBox crearFilaDato(String clave, String valor) {
        HBox fila = new HBox(16);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label lblClave = new Label(clave);
        lblClave.getStyleClass().add("perfil-dato-clave");
        lblClave.setMinWidth(200);

        Label lblValor = new Label(valor != null ? valor : "—");
        lblValor.getStyleClass().add("perfil-dato-valor");

        fila.getChildren().addAll(lblClave, lblValor);
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

                HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() ->
                    lblMensaje.setText("✅ Estado actualizado")
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() ->
                    lblMensaje.setText("⚠️ No se pudo guardar el estado")
                );
            }
        }).start();
    }
}
