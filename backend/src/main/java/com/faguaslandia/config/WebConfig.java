package com.faguaslandia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // permitir cookies y CORS
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins(frontendUrl)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // servir imágenes subidas
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        System.out.println("RUTA ABSOLUTA: "
                + new java.io.File("uploads").getAbsolutePath());

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}