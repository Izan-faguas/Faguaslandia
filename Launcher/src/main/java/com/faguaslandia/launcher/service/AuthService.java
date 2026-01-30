package com.faguaslandia.launcher.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    private static final String BACKEND_URL = "http://10.116.192.57:8081/auth/login";
    private final HttpClient client;

    public AuthService() {
        this.client = HttpClient.newHttpClient();
    }

    public boolean login(String email, String password) {
        try {
            String json = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    email, password
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("Error conectando con el backend");
            e.printStackTrace();
            return false;
        }
    }
}
