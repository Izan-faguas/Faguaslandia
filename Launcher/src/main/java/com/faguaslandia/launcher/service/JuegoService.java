package com.faguaslandia.launcher.service;

import com.faguaslandia.launcher.model.Juego;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class JuegoService {

    private static final String API_BASE = "http://10.116.192.57:8081/juegos";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public JuegoService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public List<Juego> obtenerTodos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), new TypeReference<List<Juego>>() {});
    }

    public void comprarJuego(Long usuarioId, Long juegoId) throws Exception {
        // Para registrar la compra, deberías tener un endpoint en Spring Boot que reciba usuarioId + juegoId
        String url = "http://10.116.192.57:8081/compras?usuarioId=" + usuarioId + "&juegoId=" + juegoId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
