package com.faguaslandia.launcher.service;

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

    private static final String API_BASE = "http://10.116.192.57:8081/juegos";

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
        String url = "http://10.116.192.57:8081/compras?usuarioId=" + usuarioId + "&juegoId=" + juegoId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public boolean estaComprado(Long usuarioId, Long juegoId) throws Exception {
        URL url = new URL(API + "/compras/usuario/" + usuarioId);
        List<Juego> juegos = mapper.readValue(
                url, new TypeReference<List<Juego>>() {}
        );

        return juegos.stream().anyMatch(j -> j.getId().equals(juegoId));
    }

    public List<Juego> obtenerBiblioteca(Long usuarioId) throws Exception {
        URL url = new URL(API + "/compras/usuario/" + usuarioId);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return mapper.readValue(
                url,
                new TypeReference<List<Juego>>() {}
        );
    }


}
