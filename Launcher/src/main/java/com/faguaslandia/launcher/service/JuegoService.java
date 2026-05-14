package com.faguaslandia.launcher.service;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Juego;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class JuegoService {

    private static final String API_BASE = Config.API_BASE_URL + "/juegos";
    private final ObjectMapper mapper;

    public JuegoService() {
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public List<Juego> obtenerTodos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE))
                .GET()
                .build();
        HttpResponse<String> response = AuthService.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), new TypeReference<List<Juego>>() {});
    }

    public void comprarJuego(Long usuarioId, Long juegoId) throws Exception {
        String json = """
                {"usuarioId": %d, "juegoId": %d}
                """.formatted(usuarioId, juegoId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.API_BASE_URL + "/compras"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = AuthService.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("STATUS COMPRA: " + response.statusCode());
        System.out.println("RESPUESTA: " + response.body());
    }

    public boolean estaComprado(Long usuarioId, Long juegoId) throws Exception {
        String url = Config.API_BASE_URL + "/compras/usuario/" + usuarioId + "/juego/" + juegoId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = AuthService.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        return Boolean.parseBoolean(response.body());
    }

    public List<Juego> obtenerBiblioteca(Long usuarioId) throws Exception {
        String url = Config.API_BASE_URL + "/compras/usuario/" + usuarioId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = AuthService.getClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("BIBLIOTECA RESPONSE: " + response.body());
        return mapper.readValue(response.body(), new TypeReference<List<Juego>>() {});
    }
}