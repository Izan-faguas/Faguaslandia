package com.faguaslandia.launcher.service;

import com.faguaslandia.launcher.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    private final ObjectMapper mapper = new ObjectMapper();

    public Usuario login(String email, String password) throws Exception {
        String url = "http://10.116.192.57:8081/auth/login"; // URL de tu backend

        // Creamos el JSON para enviar al backend
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Devuelve un Usuario con todos sus datos
            return mapper.readValue(response.body(), Usuario.class);
        } else {
            return null; // login fallido
        }
    }
}
