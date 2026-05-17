package com.faguaslandia.config;

import com.faguaslandia.repository.UsuarioRepository;
import com.faguaslandia.service.LogroService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Revisa cada noche a las 3:00 AM todos los usuarios
 * y concede los logros que se hayan ganado pero no tengan aún.
 * Cubre casos de usuarios anteriores a la implementación de logros
 * y cualquier logro que se pudiera haber escapado por algún motivo.
 */
@Component
public class LogroScheduler {

    private final UsuarioRepository usuarioRepository;
    private final LogroService logroService;

    public LogroScheduler(UsuarioRepository usuarioRepository,
                          LogroService logroService) {
        this.usuarioRepository = usuarioRepository;
        this.logroService      = logroService;
    }

    @Scheduled(cron = "0 0 3 * * *")   // cada día a las 3:00 AM
    public void revisarLogros() {
        usuarioRepository.findAll()
                .forEach(u -> logroService.comprobarTodos(u.getId()));
    }
}