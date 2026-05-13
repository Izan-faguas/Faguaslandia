package com.faguaslandia.launcher.view;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AmigosView extends HBox {

    // ── Modelo interno ──────────────────────────────────
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AmigoDTO {
        public Long id;
        public String nombre;
        public String foto;
        public String estado;   // "online" | "ausente" | "no_molestar" | "invisible" | "offline"
        public String juegoActual; // puede ser null

        // Para solicitudes pendientes
        public Long solicitudId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MensajeDTO {
        public Long id;
        public Long remitenteId;
        public String contenido;
        public String fecha;
    }

    // ── Campos ──────────────────────────────────────────
    private final Usuario usuarioActual;
    private final ObjectMapper mapper = new ObjectMapper();

    // Panel izquierdo
    private VBox listaPanel;
    private VBox listaContainer;
    private VBox solicitudesContainer;
    private TextField buscadorField;

    // Panel derecho (chat)
    private StackPane chatArea;
    private VBox chatPanel;
    private VBox mensajesBox;
    private TextField msgField;
    private Button enviarBtn;
    private AmigoDTO amigoSeleccionado;
    private Label chatTitulo;
    private Label chatEstado;

    // Polling
    private Thread pollingThread;
    private volatile boolean pollingActivo = false;

    // Datos
    private List<AmigoDTO> amigos = new ArrayList<>();

    public AmigosView(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        getStyleClass().add("root");
        construir();
        cargarDatos();
    }

    // ════════════════════════════════════════════════════
    //  CONSTRUCCIÓN UI
    // ════════════════════════════════════════════════════

    private void construir() {
        setSpacing(0);

        // ── Panel IZQUIERDO ──────────────────────────────
        VBox left = new VBox(0);
        left.getStyleClass().add("amigos-left-panel");

        // Cabecera
        HBox cabeceraLeft = new HBox(10);
        cabeceraLeft.getStyleClass().add("amigos-left-header");
        cabeceraLeft.setAlignment(Pos.CENTER_LEFT);

        Label tituloLeft = new Label("👥  Amigos");
        tituloLeft.getStyleClass().add("amigos-panel-title");
        HBox.setHgrow(tituloLeft, Priority.ALWAYS);

        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.getStyleClass().add("btn-agregar-amigo");
        btnAgregar.setOnAction(e -> mostrarDialogoAgregar());

        cabeceraLeft.getChildren().addAll(tituloLeft, btnAgregar);

        // Buscador
        buscadorField = new TextField();
        buscadorField.setPromptText("🔍  Buscar amigo...");
        buscadorField.getStyleClass().add("amigos-search");
        buscadorField.textProperty().addListener((obs, o, n) -> filtrarAmigos(n));
        HBox buscadorBox = new HBox(buscadorField);
        buscadorBox.setPadding(new Insets(12, 16, 8, 16));
        HBox.setHgrow(buscadorField, Priority.ALWAYS);

        // Tabs solicitudes / amigos
        HBox tabs = new HBox(0);
        tabs.getStyleClass().add("amigos-tabs");

        ToggleButton tabAmigos      = makeTab("Amigos");
        ToggleButton tabSolicitudes = makeTab("Solicitudes");

        ToggleGroup tg = new ToggleGroup();
        tabAmigos.setToggleGroup(tg);
        tabSolicitudes.setToggleGroup(tg);
        tabAmigos.setSelected(true);
        tabAmigos.getStyleClass().add("amigos-tab-active");

        tabAmigos.selectedProperty().addListener((obs, o, n) -> {
            if (n) tabAmigos.getStyleClass().add("amigos-tab-active");
            else   tabAmigos.getStyleClass().remove("amigos-tab-active");
        });
        tabSolicitudes.selectedProperty().addListener((obs, o, n) -> {
            if (n) tabSolicitudes.getStyleClass().add("amigos-tab-active");
            else   tabSolicitudes.getStyleClass().remove("amigos-tab-active");
        });

        HBox.setHgrow(tabAmigos, Priority.ALWAYS);
        HBox.setHgrow(tabSolicitudes, Priority.ALWAYS);
        tabs.getChildren().addAll(tabAmigos, tabSolicitudes);
        tabs.setPadding(new Insets(0, 16, 0, 16));

        // Contenedor scrollable
        listaContainer     = new VBox(4);
        solicitudesContainer = new VBox(4);
        listaContainer.setPadding(new Insets(8, 8, 8, 8));
        solicitudesContainer.setPadding(new Insets(8, 8, 8, 8));

        StackPane listaStack = new StackPane(listaContainer);

        tabAmigos.setOnAction(e -> {
            listaStack.getChildren().setAll(listaContainer);
        });
        tabSolicitudes.setOnAction(e -> {
            listaStack.getChildren().setAll(solicitudesContainer);
        });

        ScrollPane scrollLista = new ScrollPane(listaStack);
        scrollLista.getStyleClass().add("scroll-pane");
        scrollLista.setFitToWidth(true);
        scrollLista.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollLista, Priority.ALWAYS);

        left.getChildren().addAll(cabeceraLeft, buscadorBox, tabs, scrollLista);

        // ── Panel DERECHO (chat) ─────────────────────────
        chatArea = new StackPane();
        chatArea.getStyleClass().add("chat-placeholder-area");
        HBox.setHgrow(chatArea, Priority.ALWAYS);

        // Placeholder inicial
        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        Label iconChat = new Label("💬");
        iconChat.setStyle("-fx-font-size: 48px;");
        Label txtPlaceholder = new Label("Selecciona un amigo para chatear");
        txtPlaceholder.getStyleClass().add("chat-placeholder-txt");
        placeholder.getChildren().addAll(iconChat, txtPlaceholder);
        chatArea.getChildren().add(placeholder);

        getChildren().addAll(left, chatArea);
    }

    private ToggleButton makeTab(String texto) {
        ToggleButton btn = new ToggleButton(texto);
        btn.getStyleClass().add("amigos-tab");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    // ════════════════════════════════════════════════════
    //  CARGAR DATOS DESDE BACKEND
    // ════════════════════════════════════════════════════

    public void cargarDatos() {
        new Thread(() -> {
            try {
                List<AmigoDTO> lista = fetchAmigos();
                List<AmigoDTO> solicitudes = fetchSolicitudes();

                Platform.runLater(() -> {
                    amigos = lista;
                    renderizarAmigos(lista);
                    renderizarSolicitudes(solicitudes);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Label err = new Label("Error cargando amigos");
                    err.setStyle("-fx-text-fill: #f47f7f;");
                    listaContainer.getChildren().setAll(err);
                });
            }
        }).start();
    }

    private List<AmigoDTO> fetchAmigos() throws Exception {
        String url = Config.API_BASE_URL + "/usuarios/" + usuarioActual.getId() + "/amigos";
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        if (resp.statusCode() == 200) {
            return mapper.readValue(resp.body(), new TypeReference<>() {});
        }
        return new ArrayList<>();
    }

    private List<AmigoDTO> fetchSolicitudes() throws Exception {
        String url = Config.API_BASE_URL + "/usuarios/" + usuarioActual.getId() + "/solicitudes-pendientes";
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        if (resp.statusCode() == 200) {
            return mapper.readValue(resp.body(), new TypeReference<>() {});
        }
        return new ArrayList<>();
    }

    // ════════════════════════════════════════════════════
    //  RENDERIZAR LISTA AMIGOS
    // ════════════════════════════════════════════════════

    private void renderizarAmigos(List<AmigoDTO> lista) {
        listaContainer.getChildren().clear();

        List<AmigoDTO> online  = lista.stream().filter(a -> !"offline".equals(a.estado) && !"invisible".equals(a.estado)).toList();
        List<AmigoDTO> offline = lista.stream().filter(a ->  "offline".equals(a.estado) || "invisible".equals(a.estado)).toList();

        if (!online.isEmpty()) {
            Label secOnline = new Label("EN LÍNEA — " + online.size());
            secOnline.getStyleClass().add("amigos-section-title");
            listaContainer.getChildren().add(secOnline);
            online.forEach(a -> listaContainer.getChildren().add(crearFilaAmigo(a)));
        }

        if (!offline.isEmpty()) {
            Label secOffline = new Label("DESCONECTADO — " + offline.size());
            secOffline.getStyleClass().add("amigos-section-title");
            listaContainer.getChildren().add(secOffline);
            offline.forEach(a -> listaContainer.getChildren().add(crearFilaAmigo(a)));
        }

        if (lista.isEmpty()) {
            Label empty = new Label("Sin amigos todavía.\nUsa \"+ Agregar\" para buscar.");
            empty.getStyleClass().add("amigos-vacio");
            empty.setWrapText(true);
            empty.setPadding(new Insets(20, 10, 0, 10));
            listaContainer.getChildren().add(empty);
        }
    }

    private HBox crearFilaAmigo(AmigoDTO amigo) {
        HBox fila = new HBox(12);
        fila.getStyleClass().add("amigo-row");
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setMaxWidth(Double.MAX_VALUE);

        // Avatar
        StackPane av = crearAvatar(amigo, 36);

        // Info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nombre = new Label(amigo.nombre);
        nombre.getStyleClass().add("amigo-nombre");

        Label estadoTxt = new Label(estadoTexto(amigo));
        estadoTxt.getStyleClass().add("amigo-estado-txt");

        info.getChildren().addAll(nombre, estadoTxt);

        // Botón chat
        Button btnChat = new Button("💬");
        btnChat.getStyleClass().add("amigo-btn-chat");
        btnChat.setTooltip(new Tooltip("Abrir chat"));
        btnChat.setOnAction(e -> abrirChat(amigo));

        fila.getChildren().addAll(av, info, btnChat);
        fila.setOnMouseClicked(e -> abrirChat(amigo));

        // Resaltar si es el seleccionado
        if (amigoSeleccionado != null && amigoSeleccionado.id.equals(amigo.id)) {
            fila.getStyleClass().add("amigo-row-selected");
        }

        return fila;
    }

    private StackPane crearAvatar(AmigoDTO amigo, double size) {
        StackPane av = new StackPane();
        av.setMinSize(size, size);
        av.setMaxSize(size, size);
        av.getStyleClass().add("amigo-avatar");

        if (amigo.foto != null && !amigo.foto.isBlank()) {
            try {
                ImageView img = new ImageView(new Image(amigo.foto, true));
                img.setFitWidth(size);
                img.setFitHeight(size);
                Circle clip = new Circle(size / 2, size / 2, size / 2);
                img.setClip(clip);
                av.getChildren().add(img);
            } catch (Exception e) {
                av.getChildren().add(letraAvatar(amigo, size));
            }
        } else {
            av.getChildren().add(letraAvatar(amigo, size));
        }

        // Indicador de estado
        Label dot = new Label();
        dot.getStyleClass().add("estado-dot");
        dot.getStyleClass().add("estado-dot-" + estadoDotClass(amigo.estado));
        dot.setMinSize(10, 10);
        dot.setMaxSize(10, 10);
        StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
        av.getChildren().add(dot);

        return av;
    }

    private Label letraAvatar(AmigoDTO amigo, double size) {
        Label l = new Label(amigo.nombre != null && !amigo.nombre.isEmpty()
            ? String.valueOf(amigo.nombre.charAt(0)).toUpperCase() : "?");
        l.setStyle("-fx-text-fill: #e6f0f8; -fx-font-weight: bold; -fx-font-size: " + (size * 0.4) + "px;");
        return l;
    }

    private String estadoDotClass(String estado) {
        return switch (estado == null ? "offline" : estado) {
            case "ausente"     -> "ausente";
            case "no_molestar" -> "no-molestar";
            case "invisible"   -> "offline";
            case "online"      -> "online";
            default            -> "offline";
        };
    }

    private String estadoTexto(AmigoDTO amigo) {
        String base = switch (amigo.estado == null ? "offline" : amigo.estado) {
            case "ausente"     -> "🟡 Ausente";
            case "no_molestar" -> "🔴 No molestar";
            case "invisible"   -> "⚫ Invisible";
            case "online"      -> "🟢 En línea";
            default            -> "⚫ Desconectado";
        };
        if (amigo.juegoActual != null && !amigo.juegoActual.isBlank()) {
            base += "  •  Jugando a " + amigo.juegoActual;
        }
        return base;
    }

    // ════════════════════════════════════════════════════
    //  RENDERIZAR SOLICITUDES
    // ════════════════════════════════════════════════════

    private void renderizarSolicitudes(List<AmigoDTO> lista) {
        solicitudesContainer.getChildren().clear();

        if (lista.isEmpty()) {
            Label empty = new Label("No tienes solicitudes pendientes.");
            empty.getStyleClass().add("amigos-vacio");
            empty.setPadding(new Insets(20, 10, 0, 10));
            solicitudesContainer.getChildren().add(empty);
            return;
        }

        Label sec = new Label("SOLICITUDES — " + lista.size());
        sec.getStyleClass().add("amigos-section-title");
        solicitudesContainer.getChildren().add(sec);

        for (AmigoDTO sol : lista) {
            solicitudesContainer.getChildren().add(crearFilaSolicitud(sol));
        }
    }

    private VBox crearFilaSolicitud(AmigoDTO sol) {
        VBox card = new VBox(8);
        card.getStyleClass().add("solicitud-card");

        HBox info = new HBox(12);
        info.setAlignment(Pos.CENTER_LEFT);

        StackPane av = crearAvatar(sol, 40);

        VBox datos = new VBox(3);
        HBox.setHgrow(datos, Priority.ALWAYS);

        Label nombre = new Label(sol.nombre);
        nombre.getStyleClass().add("amigo-nombre");
        Label sub = new Label("Quiere ser tu amigo");
        sub.getStyleClass().add("amigo-estado-txt");

        datos.getChildren().addAll(nombre, sub);
        info.getChildren().addAll(av, datos);

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER_RIGHT);

        Button btnAceptar  = new Button("✔ Aceptar");
        btnAceptar.getStyleClass().add("btn-aceptar");
        Button btnRechazar = new Button("✖ Rechazar");
        btnRechazar.getStyleClass().add("btn-rechazar");

        btnAceptar.setOnAction(e -> responderSolicitud(sol, true, card));
        btnRechazar.setOnAction(e -> responderSolicitud(sol, false, card));

        botones.getChildren().addAll(btnRechazar, btnAceptar);
        card.getChildren().addAll(info, botones);
        return card;
    }

    private void responderSolicitud(AmigoDTO sol, boolean aceptar, VBox card) {
        String endpoint = aceptar ? "aceptar" : "rechazar";
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/solicitudes/" + sol.solicitudId + "/" + endpoint;
                HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                        .POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString()
                );
                Platform.runLater(() -> {
                    solicitudesContainer.getChildren().remove(card);
                    if (aceptar) cargarDatos(); // recargar lista amigos
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // ════════════════════════════════════════════════════
    //  FILTRO DE BÚSQUEDA
    // ════════════════════════════════════════════════════

    private void filtrarAmigos(String texto) {
        if (texto == null || texto.isBlank()) {
            renderizarAmigos(amigos);
            return;
        }
        String lower = texto.toLowerCase();
        List<AmigoDTO> filtrados = amigos.stream()
            .filter(a -> a.nombre != null && a.nombre.toLowerCase().contains(lower))
            .toList();
        renderizarAmigos(filtrados);
    }

    // ════════════════════════════════════════════════════
    //  CHAT
    // ════════════════════════════════════════════════════

    private void abrirChat(AmigoDTO amigo) {
        amigoSeleccionado = amigo;
        detenerPolling();

        chatArea.getChildren().clear();

        chatPanel = new VBox(0);
        chatPanel.getStyleClass().add("chat-panel");
        chatPanel.setMaxWidth(Double.MAX_VALUE);
        chatPanel.setMaxHeight(Double.MAX_VALUE);
        chatArea.getChildren().add(chatPanel);

        // ── Cabecera chat ──
        HBox cabecera = new HBox(12);
        cabecera.getStyleClass().add("chat-header");
        cabecera.setAlignment(Pos.CENTER_LEFT);

        StackPane avChat = crearAvatar(amigo, 36);

        VBox infoChat = new VBox(2);
        HBox.setHgrow(infoChat, Priority.ALWAYS);

        chatTitulo = new Label(amigo.nombre);
        chatTitulo.getStyleClass().add("chat-nombre");

        chatEstado = new Label(estadoTexto(amigo));
        chatEstado.getStyleClass().add("amigo-estado-txt");

        infoChat.getChildren().addAll(chatTitulo, chatEstado);

        Button btnCerrar = new Button("✕");
        btnCerrar.getStyleClass().add("chat-btn-cerrar");
        btnCerrar.setOnAction(e -> cerrarChat());

        cabecera.getChildren().addAll(avChat, infoChat, btnCerrar);

        // ── Mensajes ──
        mensajesBox = new VBox(10);
        mensajesBox.setPadding(new Insets(16));
        mensajesBox.setFillWidth(true);

        ScrollPane scrollMsgs = new ScrollPane(mensajesBox);
        scrollMsgs.getStyleClass().add("scroll-pane");
        scrollMsgs.setFitToWidth(true);
        scrollMsgs.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollMsgs, Priority.ALWAYS);

        // Auto-scroll al final
        mensajesBox.heightProperty().addListener((obs, o, n) ->
            scrollMsgs.setVvalue(1.0)
        );

        // ── Input ──
        HBox inputBox = new HBox(10);
        inputBox.getStyleClass().add("chat-input-box");
        inputBox.setAlignment(Pos.CENTER);

        msgField  = new TextField();
        msgField.setPromptText("Escribe un mensaje...");
        msgField.getStyleClass().add("chat-input");
        HBox.setHgrow(msgField, Priority.ALWAYS);

        enviarBtn = new Button("Enviar ▶");
        enviarBtn.getStyleClass().add("btn-play");
        enviarBtn.setStyle("-fx-padding: 10 20 10 20; -fx-font-size: 13px;");

        enviarBtn.setOnAction(e -> enviarMensaje());
        msgField.setOnAction(e -> enviarMensaje());

        inputBox.getChildren().addAll(msgField, enviarBtn);

        chatPanel.getChildren().addAll(cabecera, scrollMsgs, inputBox);

        // Cargar mensajes y arrancar polling
        cargarMensajes(amigo.id);
        iniciarPolling(amigo.id);

        // Resaltar en lista
        renderizarAmigos(amigos);
    }

    private void cerrarChat() {
        detenerPolling();
        amigoSeleccionado = null;
        chatArea.getChildren().clear();

        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        Label icon = new Label("💬");
        icon.setStyle("-fx-font-size: 48px;");
        Label txt = new Label("Selecciona un amigo para chatear");
        txt.getStyleClass().add("chat-placeholder-txt");
        placeholder.getChildren().addAll(icon, txt);
        chatArea.getChildren().add(placeholder);

        renderizarAmigos(amigos);
    }

    private void cargarMensajes(Long amigoId) {
        new Thread(() -> {
            try {
                List<MensajeDTO> mensajes = fetchMensajes(amigoId);
                Platform.runLater(() -> {
                    mensajesBox.getChildren().clear();
                    mensajes.forEach(m -> mensajesBox.getChildren().add(crearBurbuja(m)));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private List<MensajeDTO> fetchMensajes(Long amigoId) throws Exception {
        String url = Config.API_BASE_URL + "/mensajes/" + usuarioActual.getId() + "/" + amigoId;
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        if (resp.statusCode() == 200) {
            return mapper.readValue(resp.body(), new TypeReference<>() {});
        }
        return new ArrayList<>();
    }

    private void enviarMensaje() {
        String texto = msgField.getText().trim();
        if (texto.isEmpty() || amigoSeleccionado == null) return;

        msgField.clear();
        enviarBtn.setDisable(true);

        // Mostrar optimísticamente
        MensajeDTO local = new MensajeDTO();
        local.remitenteId = usuarioActual.getId();
        local.contenido   = texto;
        local.fecha       = "ahora";
        mensajesBox.getChildren().add(crearBurbuja(local));

        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/mensajes";
                String json = String.format(
                    "{\"remitenteId\":%d,\"destinatarioId\":%d,\"contenido\":\"%s\"}",
                    usuarioActual.getId(), amigoSeleccionado.id,
                    texto.replace("\"", "\\\"")
                );
                HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                    HttpResponse.BodyHandlers.ofString()
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                Platform.runLater(() -> enviarBtn.setDisable(false));
            }
        }).start();
    }

    private HBox crearBurbuja(MensajeDTO m) {
        boolean mio = m.remitenteId != null && m.remitenteId.equals(usuarioActual.getId());

        Label bubble = new Label(m.contenido);
        bubble.setWrapText(true);
        bubble.setMaxWidth(360);
        bubble.getStyleClass().add(mio ? "chat-burbuja-mia" : "chat-burbuja-otro");

        Label hora = new Label(m.fecha != null ? m.fecha : "");
        hora.getStyleClass().add("chat-hora");

        VBox stack = new VBox(3, bubble, hora);
        stack.setAlignment(mio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        hora.setAlignment(mio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox row = new HBox(stack);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setAlignment(mio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return row;
    }

    // ════════════════════════════════════════════════════
    //  POLLING (actualizar mensajes cada 5s)
    // ════════════════════════════════════════════════════

    private void iniciarPolling(Long amigoId) {
        pollingActivo = true;
        pollingThread = new Thread(() -> {
            while (pollingActivo) {
                try {
                    Thread.sleep(5000);
                    if (!pollingActivo) break;
                    List<MensajeDTO> nuevos = fetchMensajes(amigoId);
                    Platform.runLater(() -> {
                        int actual = mensajesBox.getChildren().size();
                        if (nuevos.size() > actual) {
                            // añadir solo los nuevos
                            for (int i = actual; i < nuevos.size(); i++) {
                                mensajesBox.getChildren().add(crearBurbuja(nuevos.get(i)));
                            }
                        }
                    });
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void detenerPolling() {
        pollingActivo = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    // ════════════════════════════════════════════════════
    //  DIÁLOGO AGREGAR AMIGO
    // ════════════════════════════════════════════════════

    private void mostrarDialogoAgregar() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Agregar amigo");
        dialog.setHeaderText(null);

        // Estilo oscuro al dialog
        DialogPane dp = dialog.getDialogPane();
        dp.getStylesheets().add(
            getClass().getResource("/styles/index.css").toExternalForm()
        );
        dp.getStyleClass().add("login-box");
        dp.setStyle("-fx-background-color: #121a24; -fx-border-color: rgba(102,192,244,0.2); -fx-border-radius: 12; -fx-background-radius: 12;");

        Label titulo = new Label("Agregar amigo");
        titulo.getStyleClass().add("perfil-section-title");

        TextField emailField = new TextField();
        emailField.setPromptText("Email o nombre de usuario");
        emailField.getStyleClass().add("login-field");
        emailField.setMinWidth(280);

        Label msg = new Label("");
        msg.getStyleClass().add("perfil-msg");

        VBox contenido = new VBox(14, titulo,
            new Label("Introduce el email del usuario:") {{
                setStyle("-fx-text-fill: #9fb3c8; -fx-font-size: 13px;");
            }},
            emailField, msg
        );
        contenido.setPadding(new Insets(10, 0, 10, 0));
        dp.setContent(contenido);

        ButtonType enviarType = new ButtonType("Enviar solicitud", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dp.getButtonTypes().addAll(enviarType, cancelType);

        // Estilo botones
        Button btnEnviar  = (Button) dp.lookupButton(enviarType);
        Button btnCancelar = (Button) dp.lookupButton(cancelType);
        btnEnviar.getStyleClass().add("btn-play");
        btnCancelar.getStyleClass().add("btn-secondary");

        btnEnviar.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                msg.setText("⚠️ Introduce un email");
                e.consume();
                return;
            }
            enviarSolicitudAmistad(email, msg, dialog);
            e.consume(); // evitar cierre automático
        });

        dialog.setResultConverter(bt -> bt == enviarType ? emailField.getText() : null);
        dialog.show();
    }

    private void enviarSolicitudAmistad(String email, Label msg, Dialog<?> dialog) {
        new Thread(() -> {
            try {
                String url = Config.API_BASE_URL + "/usuarios/solicitudes";
                String json = String.format(
                    "{\"remitenteId\":%d,\"destinatarioEmail\":\"%s\"}",
                    usuarioActual.getId(), email
                );
                HttpResponse<String> resp = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                    HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                        msg.setText("✅ Solicitud enviada a " + email);
                        msg.setStyle("-fx-text-fill: #4caf50;");
                    } else if (resp.statusCode() == 404) {
                        msg.setText("❌ Usuario no encontrado");
                        msg.setStyle("-fx-text-fill: #f47f7f;");
                    } else {
                        msg.setText("⚠️ Error al enviar: " + resp.statusCode());
                        msg.setStyle("-fx-text-fill: #f47f7f;");
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    msg.setText("⚠️ Error de conexión");
                    msg.setStyle("-fx-text-fill: #f47f7f;");
                });
            }
        }).start();
    }

    public void detener() {
        detenerPolling();
    }
}
