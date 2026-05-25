package com.faguaslandia.launcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    private static final Path SESSION_DIR  = Paths.get(System.getProperty("user.home"), "Faguaslandia");
    private static final Path SESSION_FILE = SESSION_DIR.resolve("session.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void guardar(String email, String password) {
        try {
            Files.createDirectories(SESSION_DIR);
            Map<String, String> data = new HashMap<>();
            data.put("email",    email);
            data.put("password", password);
            mapper.writeValue(SESSION_FILE.toFile(), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> cargar() {
        try {
            File f = SESSION_FILE.toFile();
            if (!f.exists()) return null;
            Map<?, ?> raw = mapper.readValue(f, Map.class);
            Map<String, String> data = new HashMap<>();
            data.put("email",    (String) raw.get("email"));
            data.put("password", (String) raw.get("password"));
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    public static void borrar() {
        try {
            Files.deleteIfExists(SESSION_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean existeSesion() {
        return SESSION_FILE.toFile().exists();
    }
}