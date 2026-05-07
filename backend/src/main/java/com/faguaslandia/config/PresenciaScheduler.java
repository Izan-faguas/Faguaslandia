package com.faguaslandia.config;

import com.faguaslandia.model.EstadoUsuario;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Revisa cada 30 segundos qué usuarios llevan demasiado tiempo
 * sin hacer heartbeat y actualiza su estado automáticamente:
 *
 *  • > 90 s sin heartbeat  → ausente
 *  • > 3 min sin heartbeat → offline
 *
 * Para que funcione, añade @EnableScheduling en FaguaslandiaApplication.
 */
@Component
public class PresenciaScheduler {

    private final UsuarioRepository usuarioRepository;

    public PresenciaScheduler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Scheduled(fixedDelay = 30_000)   // cada 30 segundos
    public void actualizarEstados() {
        LocalDateTime ahora       = LocalDateTime.now();
        LocalDateTime limiteAusente  = ahora.minusSeconds(90);
        LocalDateTime limiteOffline  = ahora.minusMinutes(3);

        List<Usuario> usuarios = usuarioRepository.findAll();

        for (Usuario u : usuarios) {
            if (u.getEstado() == EstadoUsuario.offline) continue;

            LocalDateTime ultima = u.getUltimaActividad();

            // Si nunca hizo heartbeat (null) o lleva > 3 min → offline
            if (ultima == null || ultima.isBefore(limiteOffline)) {
                u.setEstado(EstadoUsuario.offline);
                usuarioRepository.save(u);

                // Entre 90 s y 3 min → ausente (solo si estaba online)
            } else if (ultima.isBefore(limiteAusente)
                    && u.getEstado() == EstadoUsuario.online) {
                u.setEstado(EstadoUsuario.ausente);
                usuarioRepository.save(u);
            }
        }
    }
}