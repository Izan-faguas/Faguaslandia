package com.faguaslandia.launcher;

import java.io.InputStream;
import java.util.Properties;

public class Config {

    public static final String API_BASE_URL;

    static {
        try {
            Properties props = new Properties();

            InputStream is = Config.class
                    .getClassLoader()
                    .getResourceAsStream("launcher.properties");

            if (is == null) {
                throw new RuntimeException(
                        "launcher.properties NO encontrado en classpath (src/main/resources)"
                );
            }

            props.load(is);
            API_BASE_URL = props.getProperty("api.base-url");

            if (API_BASE_URL == null || API_BASE_URL.isBlank()) {
                throw new RuntimeException("api.base-url no está definido");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error cargando launcher.properties", e);
        }
    }
}
