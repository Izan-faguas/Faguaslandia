package com.faguaslandia.launcher.service;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AuthService {

    private final ObjectMapper mapper = new ObjectMapper();
    private static HttpClient client;

    public static HttpClient getClient() { return client; }

    /**
     * Login manual con email y contraseña.
     * Si tiene éxito guarda la sesión en disco.
     */
    public Usuario login(String email, String password) throws Exception {
        Usuario usuario = doLogin(email, password);
        if (usuario != null) {
            SessionManager.guardar(email, password);
        }
        return usuario;
    }

    /**
     * Intenta login automático con las credenciales guardadas en disco.
     * Devuelve el usuario si sigue siendo válido, null si no.
     */
    public Usuario loginAutomatico() {
        Map<String, String> sesion = SessionManager.cargar();
        if (sesion == null) return null;

        try {
            Usuario usuario = doLogin(sesion.get("email"), sesion.get("password"));
            if (usuario == null) {
                // Credenciales ya no válidas — borramos sesión
                SessionManager.borrar();
            }
            return usuario;
        } catch (Exception e) {
            // Sin conexión o error — no borramos la sesión, puede ser temporal
            return null;
        }
    }

    /**
     * Cierra sesión: notifica al backend y borra la sesión local.
     */
    public void logout() {
        SessionManager.borrar();
        try {
            if (client != null) {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(Config.API_BASE_URL + "/auth/logout"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
                client.send(req, HttpResponse.BodyHandlers.discarding());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Interno ──────────────────────────────────────

    private Usuario doLogin(String email, String password) throws Exception {
        String url  = Config.API_BASE_URL + "/auth/login";
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        // Crear cliente nuevo con cookie manager para mantener la sesión HTTP
        client = HttpClient.newBuilder()
                .cookieHandler(new java.net.CookieManager())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = mapper.readTree(response.body());
            JsonNode usuarioNode = root.get("usuario");
            if (usuarioNode != null) {
                return mapper.treeToValue(usuarioNode, Usuario.class);
            }
        }
        return null;
    }
}