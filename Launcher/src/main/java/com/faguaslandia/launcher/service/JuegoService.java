package com.faguaslandia.launcher.service;

import com.faguaslandia.launcher.Config;
import com.faguaslandia.launcher.model.Juego;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static jdk.javadoc.doclet.DocletEnvironment.ModuleMode.API;

public class JuegoService {

    private static final String API_BASE = Config.API_BASE_URL + "/juegos";


    private final HttpClient client;
    private final ObjectMapper mapper;


    public JuegoService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
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
        String url = Config.API_BASE_URL + "/compras?usuarioId=" + usuarioId + "&juegoId=" + juegoId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public boolean estaComprado(Long usuarioId, Long juegoId) throws Exception {
        String url = Config.API_BASE_URL + "/compras/usuario/" + usuarioId + "/juego/" + juegoId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        return Boolean.parseBoolean(response.body());
    }


    public List<Juego> obtenerBiblioteca(Long usuarioId) throws Exception {
        String url = Config.API_BASE_URL + "/compras/usuario/" + usuarioId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return mapper.readValue(response.body(), new TypeReference<List<Juego>>() {});
    }



}
