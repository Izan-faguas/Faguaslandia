package com.faguaslandia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FaguaslandiaApplication {
  public static void main(String[] args) {
    SpringApplication.run(FaguaslandiaApplication.class, args);
  }
}
