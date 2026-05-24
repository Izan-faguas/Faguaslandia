package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Usuario;
import com.faguaslandia.launcher.service.AuthService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
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
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PerfilView extends VBox {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogroDTO {
        public String nombre;
        public String descripcion;
        public String icono;
        public String tipo;
        public String fechaDesbloqueo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AmigoRelacionDTO {
        public Long id;
        public UsuarioMinDTO usuario1;
        public UsuarioMinDTO usuario2;
        public String estado;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsuarioMinDTO {
        public Long id;
        public String nombre;
        public String foto;
        public String estado;
    }

    private final Usuario usuario;
    private final ObjectMapper mapper = new ObjectMapper();

    private Label lblStatJuegos;
    private Label lblStatHoras;
    private Label lblStatLogros;
    private Label lblNivelBadge;
    private Label lblNivelPuntos;
    private Region nivelBarraFill;
    private StackPane avatarWrapper;
    private Label avatarInitial;

    private VBox listaLogrosBox;
    private VBox listaSolicitudesBox;
    private VBox listaAmigosBox;
    private Runnable onLogout;

    public void setOnLogout(Runnable r) {
        this.onLogout = r;
    }

    public PerfilView(Usuario usuario) {
        this.usuario = usuario;
        getStyleClass().add("root");
        setSpacing(0);
        setMaxWidth(Double.MAX_VALUE);
        construirVista();
    }

    private void construirVista() {

        StackPane banner = new StackPane();
        banner.getStyleClass().add("perfil-banner");
        banner.setMinHeight(160);
        banner.setMaxHeight(160);

        avatarWrapper = new StackPane();
        avatarWrapper.getStyleClass().add("perfil-avatar-wrapper");

        String inicial = usuario.getNombre() != null && !usuario.getNombre().isEmpty()
                ? usuario.getNombre().substring(0, 1).toUpperCase() : "?";
        avatarInitial = new Label(inicial);
        avatarInitial.getStyleClass().add("perfil-avatar-initial");

        cargarAvatarEn(avatarWrapper, avatarInitial, usuario.getFoto(), 100);

        StackPane bannerConAvatar = new StackPane(banner);
        StackPane.setAlignment(avatarWrapper, Pos.BOTTOM_CENTER);
        StackPane.setMargin(avatarWrapper, new Insets(0, 0, -50, 0));
        bannerConAvatar.getChildren().add(avatarWrapper);

        Label lblNombre = new Label(usuario.getNombre());
        lblNombre.getStyleClass().add("perfil-nombre");

        Label lblTag = new Label("#" + usuario.getId());
        lblTag.getStyleClass().add("perfil-tag");

        lblNivelBadge = new Label("Nivel 1");
        lblNivelBadge.getStyleClass().add("nivel-badge");

        nivelBarraFill = new Region();
        nivelBarraFill.getStyleClass().add("nivel-barra-fill");
        nivelBarraFill.setPrefWidth(0);
        nivelBarraFill.setMaxWidth(0);

        Region nivelBarraBg = new Region();
        nivelBarraBg.getStyleClass().add("nivel-barra-bg");
        nivelBarraBg.setPrefWidth(200);
        nivelBarraBg.setPrefHeight(8);

        StackPane barraStack = new StackPane(nivelBarraBg, nivelBarraFill);
        StackPane.setAlignment(nivelBarraFill, Pos.CENTER_LEFT);

        lblNivelPuntos = new Label("0 pts");
        lblNivelPuntos.getStyleClass().add("nivel-puntos");

        HBox barraBox = new HBox(10, barraStack, lblNivelPuntos);
        barraBox.setAlignment(Pos.CENTER);

        VBox nombreBox = new VBox(6, lblNombre, lblTag, lblNivelBadge, barraBox);
        nombreBox.setAlignment(Pos.CENTER);
        nombreBox.setPadding(new Insets(60, 0, 20, 0));

        lblStatJuegos = new Label("—");
        lblStatHoras = new Label("—");
        lblStatLogros = new Label("—");

        HBox statsRow = new HBox(20,
                crearStatCard("🎮", "Juegos", lblStatJuegos),
                crearStatCard("⏱️", "Horas", lblStatHoras),
                crearStatCard("🏆", "Logros", lblStatLogros)
        );
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(0, 40, 20, 40));

        VBox seccionCuenta = crearSeccionCuenta();
        VBox seccionLogros = crearSeccionLogros();
        VBox seccionAmigos = crearSeccionAmigos();

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.getStyleClass().add("btn-secondary");
        btnLogout.setStyle("-fx-font-size: 12px; -fx-padding: 6 16 6 16;");
        btnLogout.setOnAction(e -> {
            if (onLogout != null) onLogout.run();
        });

        HBox logoutBox = new HBox(btnLogout);
        logoutBox.setAlignment(Pos.CENTER_RIGHT);
        logoutBox.setPadding(new Insets(12, 24, 0, 0));

        VBox contenido = new VBox(
                logoutBox, bannerConAvatar, nombreBox, statsRow,
                separador(), seccionCuenta,
                separador(), seccionLogros,
                separador(), seccionAmigos
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().add(scroll);

        cargarStats();
        cargarLogros();
        cargarSolicitudes();
        cargarAmigos();
    }

    private VBox crearSeccionCuenta() {
        VBox sec = new VBox(14);
        sec.setPadding(new Insets(24, 40, 24, 40));

        Label titulo = new Label("Información de cuenta");
        titulo.getStyleClass().add("perfil-section-title");

        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("perfil-card");
        tarjeta.getChildren().addAll(
                filaDato("👤  Nombre de usuario", usuario.getNombre()),
                separador(),
                filaDato("✉️  Correo electrónico", usuario.getEmail()),
                separador(),
                filaDato("🆔  Código amigo", usuario.getNombre() + "#" + usuario.getId())
        );

        Button btnEditar = new Button("✏️  Editar perfil");
        btnEditar.getStyleClass().add("btn-play");
        btnEditar.setOnAction(e -> mostrarModalEditar());

        sec.getChildren().addAll(titulo, tarjeta, btnEditar);
        return sec;
    }

    private VBox crearSeccionLogros() {
        VBox sec = new VBox(14);
        sec.setPadding(new Insets(24, 40, 24, 40));

        Label titulo = new Label("🏆 Logros");
        titulo.getStyleClass().add("perfil-section-title");

        listaLogrosBox = new VBox(10);
        Label cargando = new Label("Cargando logros...");
        cargando.getStyleClass().add("amigos-vacio");
        listaLogrosBox.getChildren().add(cargando);

        sec.getChildren().addAll(titulo, listaLogrosBox);
        return sec;
    }

    private VBox crearSeccionAmigos() {
        VBox sec = new VBox(16);
        sec.setPadding(new Insets(24, 40, 40, 40));

        Label titulo = new Label("👥 Amigos");
        titulo.getStyleClass().add("perfil-section-title");

        TextField codigoField = new TextField();
        codigoField.setPromptText("Buscar por Nombre#id");
        codigoField.getStyleClass().add("login-field");

        Button btnAgregar = new Button("Enviar solicitud");
        btnAgregar.getStyleClass().add("btn-play");
        btnAgregar.setOnAction(e -> enviarSolicitud(codigoField.getText().trim(), codigoField));

        HBox agregarBox = new HBox(10, codigoField, btnAgregar);
        agregarBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(codigoField, Priority.ALWAYS);

        Label tituloSol = new Label("Solicitudes pendientes");
        tituloSol.getStyleClass().add("amigos-section-title");
        listaSolicitudesBox = new VBox(8);

        Label tituloAmigos = new Label("Lista de amigos");
        tituloAmigos.getStyleClass().add("amigos-section-title");
        listaAmigosBox = new VBox(8);

        sec.getChildren().addAll(titulo, agregarBox,
                tituloSol, listaSolicitudesBox,
                tituloAmigos, listaAmigosBox);
        return sec;
    }

    // ════════════════════════════════════════════════════
    //  CARGAR DATOS
    // ════════════════════════════════════════════════════

    private void cargarStats() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/stats";
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonNode j = mapper.readTree(resp.body());
                    Platform.runLater(() -> {
                        lblStatJuegos.setText(j.get("juegos").asText());
                        lblStatHoras.setText(j.get("horas").asInt() + "h");
                        lblStatLogros.setText(j.get("logros").asText());
                        lblNivelBadge.setText("Nivel " + j.get("nivel").asInt());
                        lblNivelPuntos.setText(j.get("puntos").asInt() + " pts");
                        double progreso = Math.max(0, Math.min(j.get("progreso").asDouble(), 100));
                        double anchoPx = 200 * progreso / 100.0;
                        nivelBarraFill.setPrefWidth(anchoPx);
                        nivelBarraFill.setMaxWidth(anchoPx);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void cargarLogros() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/logros";
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    List<LogroDTO> logros = mapper.readValue(resp.body(), new TypeReference<>() {
                    });
                    Platform.runLater(() -> renderizarLogros(logros));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void cargarSolicitudes() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/solicitudes";
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    List<AmigoRelacionDTO> lista = mapper.readValue(resp.body(), new TypeReference<>() {
                    });
                    Platform.runLater(() -> renderizarSolicitudes(lista));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void cargarAmigos() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/amigos";
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    List<AmigoRelacionDTO> lista = mapper.readValue(resp.body(), new TypeReference<>() {
                    });
                    Platform.runLater(() -> renderizarAmigos(lista));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // ════════════════════════════════════════════════════
    //  RENDERIZADO
    // ════════════════════════════════════════════════════

    private void renderizarLogros(List<LogroDTO> logros) {
        listaLogrosBox.getChildren().clear();
        if (logros.isEmpty()) {
            Label empty = new Label("Sin logros aún.");
            empty.getStyleClass().add("amigos-vacio");
            listaLogrosBox.getChildren().add(empty);
            return;
        }
        for (LogroDTO l : logros) {
            HBox fila = new HBox(14);
            fila.getStyleClass().add("perfil-card");
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(12, 16, 12, 16));

            ImageView icono = cargarIconoLogro(l.icono, 36);

            VBox info = new VBox(3);
            Label nombre = new Label(l.nombre);
            nombre.getStyleClass().add("amigo-nombre");
            Label desc = new Label(l.descripcion);
            desc.getStyleClass().add("amigo-estado-txt");
            desc.setWrapText(true);

            String fechaTexto = "";
            if (l.fechaDesbloqueo != null && l.fechaDesbloqueo.length() >= 10) {
                String[] partes = l.fechaDesbloqueo.substring(0, 10).split("-");
                if (partes.length == 3)
                    fechaTexto = "Desbloqueado el " + partes[2] + "/" + partes[1] + "/" + partes[0];
            }
            Label fecha = new Label(fechaTexto);
            fecha.setStyle("-fx-text-fill: #5e7a96; -fx-font-size: 11px;");

            info.getChildren().addAll(nombre, desc, fecha);
            HBox.setHgrow(info, Priority.ALWAYS);
            fila.getChildren().addAll(icono, info);
            listaLogrosBox.getChildren().add(fila);
        }
    }

    // ── Carga asíncrona (para listas) ────────────────────
    private ImageView cargarIconoLogro(String ruta, double size) {
        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        String url = (ruta != null && !ruta.isBlank())
                ? Config.IMG_BASE_URL + "/" + ruta
                : Config.IMG_BASE_URL + "/logros/default.png";

        Image img = new Image(url, true); // asíncrono está bien en listas
        img.errorProperty().addListener((obs, o, err) -> {
            if (err) Platform.runLater(() ->
                    iv.setImage(new Image(Config.IMG_BASE_URL + "/logros/default.png", true)));
        });
        iv.setImage(img);
        return iv;
    }

    // ── Carga síncrona (para el toast, que ya está en el hilo FX) ──
    private ImageView cargarIconoLogroSync(String ruta, double size) {
        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        String url = (ruta != null && !ruta.isBlank())
                ? Config.IMG_BASE_URL + "/" + ruta
                : Config.IMG_BASE_URL + "/logros/default.png";

        try {
            Image img = new Image(url); // SIN true → síncrono
            if (img.isError()) {
                img = new Image(Config.IMG_BASE_URL + "/logros/default.png");
            }
            iv.setImage(img);
        } catch (Exception e) {
            iv.setImage(new Image(Config.IMG_BASE_URL + "/logros/default.png"));
        }
        return iv;
    }

    private void renderizarSolicitudes(List<AmigoRelacionDTO> lista) {
        listaSolicitudesBox.getChildren().clear();
        List<AmigoRelacionDTO> pendientes = lista.stream()
                .filter(r -> "pendiente".equals(r.estado)).toList();
        if (pendientes.isEmpty()) {
            Label empty = new Label("Sin solicitudes pendientes");
            empty.getStyleClass().add("amigos-vacio");
            listaSolicitudesBox.getChildren().add(empty);
            return;
        }
        for (AmigoRelacionDTO sol : pendientes) {
            UsuarioMinDTO solicitante = sol.usuario1;
            HBox fila = new HBox(12);
            fila.getStyleClass().add("solicitud-card");
            fila.setAlignment(Pos.CENTER_LEFT);

            StackPane av = crearAvatarMin(solicitante, 36);
            Label nombre = new Label(solicitante.nombre + "#" + solicitante.id);
            nombre.getStyleClass().add("amigo-nombre");
            HBox.setHgrow(nombre, Priority.ALWAYS);

            Button btnAceptar = new Button("✔ Aceptar");
            btnAceptar.getStyleClass().add("btn-aceptar");
            Button btnRechazar = new Button("✖ Rechazar");
            btnRechazar.getStyleClass().add("btn-rechazar");
            btnAceptar.setOnAction(e -> responderSolicitud(sol.id, true));
            btnRechazar.setOnAction(e -> responderSolicitud(sol.id, false));

            HBox btns = new HBox(8, btnRechazar, btnAceptar);
            fila.getChildren().addAll(av, nombre, btns);
            listaSolicitudesBox.getChildren().add(fila);
        }
    }

    private void renderizarAmigos(List<AmigoRelacionDTO> lista) {
        listaAmigosBox.getChildren().clear();
        List<AmigoRelacionDTO> aceptados = lista.stream()
                .filter(r -> "aceptado".equals(r.estado)).toList();
        if (aceptados.isEmpty()) {
            Label empty = new Label("Sin amigos aún. ¡Envía una solicitud!");
            empty.getStyleClass().add("amigos-vacio");
            listaAmigosBox.getChildren().add(empty);
            return;
        }
        for (AmigoRelacionDTO rel : aceptados) {
            UsuarioMinDTO otro = rel.usuario1.id.equals(usuario.getId())
                    ? rel.usuario2 : rel.usuario1;

            HBox fila = new HBox(12);
            fila.getStyleClass().add("amigo-row");
            fila.setAlignment(Pos.CENTER_LEFT);

            StackPane av = crearAvatarMin(otro, 36);
            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label nombre = new Label(otro.nombre);
            nombre.getStyleClass().add("amigo-nombre");
            Label estadoLbl = new Label(formatearEstado(otro.estado != null ? otro.estado : "offline"));
            estadoLbl.getStyleClass().add("amigo-estado-txt");
            info.getChildren().addAll(nombre, estadoLbl);

            Button btnEliminar = new Button("Eliminar");
            btnEliminar.getStyleClass().add("btn-rechazar");
            btnEliminar.setOnAction(e -> eliminarAmigo(rel.id, otro.id, otro.nombre));

            fila.getChildren().addAll(av, info, btnEliminar);
            listaAmigosBox.getChildren().add(fila);
        }
    }

    // ════════════════════════════════════════════════════
    //  ACCIONES AMIGOS
    // ════════════════════════════════════════════════════

    private void enviarSolicitud(String codigo, TextField campo) {
        if (!codigo.contains("#")) {
            mostrarAlerta("Formato inválido. Usa Nombre#id");
            return;
        }
        long id2;
        try {
            id2 = Long.parseLong(codigo.split("#")[1]);
        } catch (NumberFormatException e) {
            mostrarAlerta("ID inválido");
            return;
        }
        if (id2 == usuario.getId()) {
            mostrarAlerta("No puedes agregarte a ti mismo");
            return;
        }

        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/agregar/" + id2;
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url))
                                .POST(HttpRequest.BodyPublishers.noBody()).build(),
                        HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    if (resp.statusCode() == 200) {
                        campo.clear();
                        mostrarAlerta("✅ Solicitud enviada");
                    } else mostrarAlerta("Error: " + resp.body());
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> mostrarAlerta("Error de conexión"));
            }
        }).start();
    }

    private void responderSolicitud(Long solicitudId, boolean aceptar) {
        String endpoint = aceptar ? "aceptar" : "rechazar";
        String method = aceptar ? "PUT" : "DELETE";
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/solicitud/" + solicitudId + "/" + endpoint;
                HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
                HttpRequest req = method.equals("PUT")
                        ? builder.PUT(HttpRequest.BodyPublishers.noBody()).build()
                        : builder.DELETE().build();
                AuthService.getClient().send(req, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    cargarSolicitudes();
                    if (aceptar) cargarAmigos();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void eliminarAmigo(Long relacionId, Long amigoId, String nombre) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + nombre + " de tus amigos?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/amigos/" + amigoId;
                        AuthService.getClient().send(
                                HttpRequest.newBuilder().uri(URI.create(url)).DELETE().build(),
                                HttpResponse.BodyHandlers.ofString());
                        Platform.runLater(this::cargarAmigos);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }

    // ════════════════════════════════════════════════════
    //  MODAL EDITAR PERFIL
    // ════════════════════════════════════════════════════

    private void mostrarModalEditar() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Editar perfil");
        dialog.setHeaderText(null);

        DialogPane dp = dialog.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/styles/index.css").toExternalForm());
        dp.getStyleClass().add("login-box");
        dp.setStyle("-fx-background-color: #121a24; -fx-border-color: rgba(102,192,244,0.2);"
                + "-fx-border-radius: 12; -fx-background-radius: 12;");

        Label titulo = new Label("Editar Perfil");
        titulo.getStyleClass().add("perfil-section-title");

        TextField nombreField = new TextField(usuario.getNombre());
        nombreField.getStyleClass().add("login-field");

        TextField correoField = new TextField(usuario.getEmail());
        correoField.getStyleClass().add("login-field");

        Label lblFotoElegida = new Label("Sin archivo elegido");
        lblFotoElegida.setStyle("-fx-text-fill: #9fb3c8; -fx-font-size: 12px;");
        final File[] fotoElegida = {null};

        Button btnFoto = new Button("📁 Elegir foto");
        btnFoto.getStyleClass().add("btn-secondary");
        btnFoto.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar imagen");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File f = fc.showOpenDialog(getScene().getWindow());
            if (f != null) {
                fotoElegida[0] = f;
                lblFotoElegida.setText(f.getName());
            }
        });

        HBox fotoBox = new HBox(10, btnFoto, lblFotoElegida);
        fotoBox.setAlignment(Pos.CENTER_LEFT);

        Label msgEditar = new Label("");
        msgEditar.getStyleClass().add("perfil-msg");

        VBox contenido = new VBox(12,
                titulo,
                etiqueta("Nombre"), nombreField,
                etiqueta("Correo"), correoField,
                etiqueta("Foto de perfil"), fotoBox,
                msgEditar);
        contenido.setPadding(new Insets(10, 0, 10, 0));
        dp.setContent(contenido);

        ButtonType guardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dp.getButtonTypes().addAll(guardarType, cancelType);

        Button btnGuardar = (Button) dp.lookupButton(guardarType);
        Button btnCancelar = (Button) dp.lookupButton(cancelType);
        btnGuardar.getStyleClass().add("btn-play");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnGuardar.setOnAction(e -> {
            String nombre = nombreField.getText().trim();
            String correo = correoField.getText().trim();
            if (nombre.isEmpty() || correo.isEmpty()) {
                msgEditar.setText("⚠️ Nombre y correo no pueden estar vacíos");
                e.consume();
                return;
            }
            guardarPerfil(nombre, correo, fotoElegida[0], msgEditar, dialog);
            e.consume();
        });

        dialog.setResultConverter(bt -> null);
        dialog.show();
    }

    private void guardarPerfil(String nombre, String correo, File foto,
                               Label msgLabel, Dialog<?> dialog) {
        new Thread(() -> {
            try {
                String boundary = "----FormBoundary" + System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();
                sb.append("--").append(boundary).append("\r\n");
                sb.append("Content-Disposition: form-data; name=\"nombre\"\r\n\r\n");
                sb.append(nombre).append("\r\n");
                sb.append("--").append(boundary).append("\r\n");
                sb.append("Content-Disposition: form-data; name=\"email\"\r\n\r\n");
                sb.append(correo).append("\r\n");

                byte[] prefixBytes = sb.toString().getBytes();
                byte[] suffix = ("\r\n--" + boundary + "--\r\n").getBytes();
                byte[] body;
                String contentType;

                if (foto != null) {
                    byte[] fileBytes = java.nio.file.Files.readAllBytes(foto.toPath());
                    String fileHeader = "--" + boundary + "\r\n"
                            + "Content-Disposition: form-data; name=\"foto\"; filename=\"" + foto.getName() + "\"\r\n"
                            + "Content-Type: image/jpeg\r\n\r\n";
                    byte[] fileHeaderBytes = fileHeader.getBytes();
                    body = new byte[prefixBytes.length + fileHeaderBytes.length + fileBytes.length + suffix.length];
                    System.arraycopy(prefixBytes, 0, body, 0, prefixBytes.length);
                    System.arraycopy(fileHeaderBytes, 0, body, prefixBytes.length, fileHeaderBytes.length);
                    System.arraycopy(fileBytes, 0, body, prefixBytes.length + fileHeaderBytes.length, fileBytes.length);
                    System.arraycopy(suffix, 0, body, prefixBytes.length + fileHeaderBytes.length + fileBytes.length, suffix.length);
                    contentType = "multipart/form-data; boundary=" + boundary;
                } else {
                    body = (sb + suffix.toString()).getBytes();
                    contentType = "multipart/form-data; boundary=" + boundary;
                }

                String url = Config.API_BASE_URL + "/usuarios/actualizar/" + usuario.getId();
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url))
                                .header("Content-Type", contentType)
                                .PUT(HttpRequest.BodyPublishers.ofByteArray(body)).build(),
                        HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (resp.statusCode() == 200) {
                        usuario.setNombre(nombre);
                        usuario.setEmail(correo);
                        if (foto != null) {
                            String filename = "user_" + usuario.getId() + ".jpg";
                            usuario.setFoto(filename);
                            avatarWrapper.getChildren().clear();
                            cargarAvatarEn(avatarWrapper, avatarInitial, filename, 100);
                        }
                        msgLabel.setText("✅ Perfil actualizado");
                        msgLabel.setStyle("-fx-text-fill: #4caf50;");
                    } else {
                        msgLabel.setText("⚠️ Error al guardar: " + resp.statusCode());
                        msgLabel.setStyle("-fx-text-fill: #f47f7f;");
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    msgLabel.setText("⚠️ Error de conexión");
                    msgLabel.setStyle("-fx-text-fill: #f47f7f;");
                });
            }
        }).start();
    }

    // ════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════

    private void cargarAvatarEn(StackPane wrapper, Label fallback, String nombreFoto, double size) {
        try {
            String fn = (nombreFoto != null && !nombreFoto.isBlank()) ? nombreFoto : "default_avatar.png";
            String url = Config.IMG_BASE_URL + "/avatars/" + fn;
            Image img = new Image(url, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setClip(new Circle(size / 2, size / 2, size / 2));
            img.errorProperty().addListener((obs, o, err) -> {
                if (err) Platform.runLater(() -> {
                    wrapper.getChildren().clear();
                    wrapper.getChildren().add(fallback);
                });
            });
            wrapper.getChildren().add(iv);
        } catch (Exception e) {
            wrapper.getChildren().add(fallback);
        }
    }

    private StackPane crearAvatarMin(UsuarioMinDTO u, double size) {
        StackPane av = new StackPane();
        av.setMinSize(size, size);
        av.setMaxSize(size, size);
        av.getStyleClass().add("amigo-avatar");

        String fn = (u.foto != null && !u.foto.isBlank()) ? u.foto : "default_avatar.png";
        String url = Config.IMG_BASE_URL + "/avatars/" + fn;
        Image img = new Image(url, true);
        ImageView iv = new ImageView(img);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setClip(new Circle(size / 2, size / 2, size / 2));

        Label letra = new Label(!u.nombre.isEmpty()
                ? String.valueOf(u.nombre.charAt(0)).toUpperCase() : "?");
        letra.setStyle("-fx-text-fill:#e6f0f8;-fx-font-weight:bold;-fx-font-size:" + (size * 0.4) + "px;");

        img.errorProperty().addListener((obs, o, err) -> {
            if (err) Platform.runLater(() -> {
                av.getChildren().clear();
                av.getChildren().add(letra);
            });
        });
        av.getChildren().add(iv);

        Label dot = new Label();
        dot.getStyleClass().add("estado-dot");
        dot.getStyleClass().add("estado-dot-" + dotClass(u.estado));
        dot.setMinSize(10, 10);
        dot.setMaxSize(10, 10);
        StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
        av.getChildren().add(dot);

        return av;
    }

    private String dotClass(String estado) {
        return switch (estado == null ? "offline" : estado) {
            case "ausente" -> "ausente";
            case "no_molestar" -> "no-molestar";
            case "online" -> "online";
            default -> "offline";
        };
    }

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

    private HBox filaDato(String clave, String valor) {
        Label k = new Label(clave);
        k.getStyleClass().add("perfil-dato-clave");
        k.setMinWidth(220);
        Label v = new Label(valor != null ? valor : "—");
        v.getStyleClass().add("perfil-dato-valor");
        return new HBox(16, k, v);
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: #9fb3c8; -fx-font-size: 13px;");
        return l;
    }

    private Region separador() {
        Region r = new Region();
        r.getStyleClass().add("separator");
        r.setMaxWidth(Double.MAX_VALUE);
        return r;
    }

    private String formatearEstado(String raw) {
        return switch (raw == null ? "offline" : raw) {
            case "ausente" -> "🟡 Ausente";
            case "no_molestar" -> "🔴 No molestar";
            case "invisible" -> "⚫ Invisible";
            case "online" -> "🟢 En línea";
            default -> "⚫ Desconectado";
        };
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ════════════════════════════════════════════════════
    //  REFRESCO PÚBLICO
    // ════════════════════════════════════════════════════

    public void refrescar() {
        cargarStats();
        cargarLogros();
        cargarSolicitudes();
        cargarAmigos();
    }

    // ════════════════════════════════════════════════════
    //  POLLING DE LOGROS PENDIENTES
    // ════════════════════════════════════════════════════

    private javafx.animation.Timeline logroPolling;

    public void iniciarPollingLogros() {
        if (logroPolling != null) logroPolling.stop();
        logroPolling = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(15),
                        e -> comprobarLogrosPendientes()
                )
        );
        logroPolling.setCycleCount(javafx.animation.Animation.INDEFINITE);
        logroPolling.play();
        comprobarLogrosPendientes();
    }

    public void detenerPollingLogros() {
        if (logroPolling != null) logroPolling.stop();
    }

    private void comprobarLogrosPendientes() {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/" + usuario.getId() + "/logros-pendientes";
                HttpResponse<String> resp = AuthService.getClient().send(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() == 200) {
                    List<LogroDTO> pendientes = mapper.readValue(resp.body(), new TypeReference<>() {
                    });
                    for (int i = 0; i < pendientes.size(); i++) {
                        final LogroDTO logro = pendientes.get(i);
                        final long delay = i * 1200L;
                        new Thread(() -> {
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException ignored) {
                            }
                            Platform.runLater(() -> mostrarToastLogro(logro));
                        }).start();
                    }
                    if (!pendientes.isEmpty()) Platform.runLater(this::refrescar);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void mostrarToastLogro(LogroDTO logro) {
        System.out.println("[Toast] Icono ruta BBDD: " + logro.icono);

        javafx.scene.Scene scene = null;
        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
            if (w.isShowing() && w instanceof javafx.stage.Stage) {
                scene = ((javafx.stage.Stage) w).getScene();
                break;
            }
        }
        if (scene == null) return;

        final javafx.scene.Scene sceneFinal = scene;

        new Thread(() -> {
            String urlIcono = (logro.icono != null && !logro.icono.isBlank())
                    ? Config.IMG_BASE_URL + "/" + logro.icono
                    : Config.IMG_BASE_URL + "/logros/default.png";

            System.out.println("[Toast] URL completa icono: " + urlIcono);

            Image img;
            try {
                img = new Image(urlIcono);
                System.out.println("[Toast] isError: " + img.isError());
                if (img.isError()) {
                    img = new Image(Config.IMG_BASE_URL + "/logros/default.png");
                    System.out.println("[Toast] Default isError: " + img.isError());
                }
            } catch (Exception e) {
                System.out.println("[Toast] Excepción: " + e.getMessage());
                img = new Image(Config.IMG_BASE_URL + "/logros/default.png");
            }

            final Image imgFinal = img;

            Platform.runLater(() -> {
                ImageView icono = new ImageView(imgFinal);
                icono.setFitWidth(40);
                icono.setFitHeight(40);
                icono.setPreserveRatio(true);
                icono.setSmooth(true);

                Label titulo = new Label("🏆 Logro desbloqueado");
                titulo.setStyle("-fx-text-fill: #66c0f4; -fx-font-size: 11px; -fx-font-weight: bold;");

                Label nombre = new Label(logro.nombre);
                nombre.setStyle("-fx-text-fill: #e6f0f8; -fx-font-size: 13px; -fx-font-weight: bold;");

                Label desc = new Label(logro.descripcion);
                desc.setStyle("-fx-text-fill: #9fb3c8; -fx-font-size: 11px;");
                desc.setWrapText(true);
                desc.setMaxWidth(220);

                VBox texto = new VBox(3, titulo, nombre, desc);
                HBox toast = new HBox(14, icono, texto);
                toast.setMaxWidth(320);
                toast.setMaxHeight(Region.USE_PREF_SIZE);
                toast.setAlignment(Pos.CENTER_LEFT);
                toast.setPadding(new Insets(14, 18, 14, 18));
                toast.setStyle("""
                    -fx-background-color: #16202d;
                    -fx-border-color: rgba(102,192,244,0.4);
                    -fx-border-width: 1;
                    -fx-border-radius: 10;
                    -fx-background-radius: 10;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 16, 0, 0, 4);
                    """);
                toast.setOpacity(0);

                javafx.scene.layout.Pane overlay = (javafx.scene.layout.Pane) sceneFinal.getRoot();
                javafx.scene.layout.StackPane.setAlignment(toast, javafx.geometry.Pos.BOTTOM_RIGHT);
                javafx.scene.layout.StackPane.setMargin(toast, new Insets(0, 24, 24, 0));

                if (overlay instanceof javafx.scene.layout.StackPane sp) {
                    sp.getChildren().add(toast);
                } else {
                    javafx.scene.layout.StackPane wrapper = new javafx.scene.layout.StackPane(overlay);
                    wrapper.getChildren().add(toast);
                    javafx.scene.layout.StackPane.setAlignment(toast, javafx.geometry.Pos.BOTTOM_RIGHT);
                    javafx.scene.layout.StackPane.setMargin(toast, new Insets(0, 24, 24, 0));
                    sceneFinal.setRoot(wrapper);
                }

                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(300), toast);
                fadeIn.setFromValue(0); fadeIn.setToValue(1);

                javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(400), toast);
                fadeOut.setFromValue(1); fadeOut.setToValue(0);
                fadeOut.setDelay(javafx.util.Duration.seconds(4));
                fadeOut.setOnFinished(e -> {
                    javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) toast.getParent();
                    if (parent != null) parent.getChildren().remove(toast);
                });

                fadeIn.play();
                fadeOut.play();
            });
        }).start();
    }
}