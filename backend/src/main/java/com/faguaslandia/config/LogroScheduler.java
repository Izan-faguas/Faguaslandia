package com.faguaslandia.config;

import com.faguaslandia.repository.UsuarioRepository;
import com.faguaslandia.service.LogroService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class LogroScheduler {

    private final UsuarioRepository usuarioRepository;
    private final LogroService logroService;

    public LogroScheduler(UsuarioRepository usuarioRepository,
                          LogroService logroService) {
        this.usuarioRepository = usuarioRepository;
        this.logroService      = logroService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void revisarLogros() {
        usuarioRepository.findAll()
                .forEach(u -> logroService.comprobarTodos(u.getId()));
    }
}