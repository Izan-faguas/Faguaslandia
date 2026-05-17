package com.faguaslandia.launcher.service;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    private final ObjectMapper mapper = new ObjectMapper();
    private static HttpClient client;

    public static HttpClient getClient() { return client; }

    public Usuario login(String email, String password) throws Exception {
        String url = Config.API_BASE_URL + "/auth/login";
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

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
            // La respuesta ahora es { "usuario": {...}, "logrosNuevos": [...] }
            JsonNode root = mapper.readTree(response.body());
            JsonNode usuarioNode = root.get("usuario");
            if (usuarioNode != null) {
                return mapper.treeToValue(usuarioNode, Usuario.class);
            }
            return null;
        } else {
            return null;
        }
    }
}