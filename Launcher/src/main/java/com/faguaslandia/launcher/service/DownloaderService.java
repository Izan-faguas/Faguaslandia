package com.faguaslandia.launcher.service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloaderService {

    private final HttpClient client;

    public DownloaderService() {
        this.client = HttpClient.newHttpClient();
    }

    // Descarga un archivo desde URL y lo guarda en destino
    public void downloadFile(String url, Path destino) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<byte[]> response =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (!Files.exists(destino.getParent())) {
            Files.createDirectories(destino.getParent());
        }

        Files.write(destino, response.body());
    }

    // Descomprime un ZIP en la carpeta destino
    public void unzip(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFile = destDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    if (newFile.getParent() != null) {
                        Files.createDirectories(newFile.getParent());
                    }
                    Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
