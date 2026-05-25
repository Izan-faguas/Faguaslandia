package com.faguaslandia.config;

import com.faguaslandia.model.EstadoUsuario;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PresenciaScheduler {

    private final UsuarioRepository usuarioRepository;

    public PresenciaScheduler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Scheduled(fixedDelay = 30_000)
    public void actualizarEstados() {
        LocalDateTime ahora       = LocalDateTime.now();
        LocalDateTime limiteAusente = ahora.minusSeconds(150);
        LocalDateTime limiteOffline = ahora.minusMinutes(5);

        List<Usuario> usuarios = usuarioRepository.findAll();

        for (Usuario u : usuarios) {
            if (u.getEstado() == EstadoUsuario.offline) continue;

            LocalDateTime ultima = u.getUltimaActividad();

            if (ultima == null || ultima.isBefore(limiteOffline)) {
                u.setEstado(EstadoUsuario.offline);
                usuarioRepository.save(u);

            } else if (ultima.isBefore(limiteAusente)
                    && u.getEstado() == EstadoUsuario.online) {
                u.setEstado(EstadoUsuario.ausente);
                usuarioRepository.save(u);
            }
        }
    }
}