package com.faguaslandia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FaguaslandiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaguaslandiaApplication.class, args);
        System.out.println("🚀 Servidor Faguaslandia iniciado en http://localhost:8081");
    }
}
