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

    // mantener el sistema de archivos que ya tienes
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("RUTA ABSOLUTA: " + new java.io.File("uploads").getAbsolutePath());
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:uploads/");
    }
}