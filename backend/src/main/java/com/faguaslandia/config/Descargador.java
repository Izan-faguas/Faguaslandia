package com.faguaslandia.config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class Descargador {

    public static void descargarArchivo(String url, Path destino) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // Crear directorios si no existen
        if (!Files.exists(destino.getParent())) {
            Files.createDirectories(destino.getParent());
        }

        Files.write(destino, response.body());
        System.out.println("Archivo descargado en: " + destino.toString());
    }
}
