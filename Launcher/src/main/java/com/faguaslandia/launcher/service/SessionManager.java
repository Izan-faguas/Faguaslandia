package com.faguaslandia.launcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la sesión persistente del usuario en disco.
 * Guarda email y contraseña en ~/Faguaslandia/session.json
 * para permitir el login automático al arrancar el launcher.
 *
 * NOTA: la contraseña se guarda en texto plano porque el backend
 * aún no usa tokens JWT. Cuando se migre a tokens, basta con
 * sustituir el campo "password" por "token".
 */
public class SessionManager {

    private static final Path SESSION_DIR  = Paths.get(System.getProperty("user.home"), "Faguaslandia");
    private static final Path SESSION_FILE = SESSION_DIR.resolve("session.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Guarda las credenciales en disco tras un login exitoso. */
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

    /** Devuelve las credenciales guardadas, o null si no hay sesión. */
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

    /** Elimina la sesión guardada (logout). */
    public static void borrar() {
        try {
            Files.deleteIfExists(SESSION_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** True si existe un archivo de sesión en disco. */
    public static boolean existeSesion() {
        return SESSION_FILE.toFile().exists();
    }
}