package com.faguaslandia.controller;

import com.faguaslandia.model.EstadoUsuario;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/presencia")
public class PresenciaController {

    private final UsuarioRepository usuarioRepository;

    public PresenciaController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * POST /presencia/heartbeat
     * El frontend lo llama cada 60 s mientras el usuario tiene la web abierta.
     * Actualiza ultimaActividad y pone estado online si venía de ausente.
     */
    @PostMapping("/heartbeat")
    public void heartbeat(HttpSession session) {
        Usuario sesion = (Usuario) session.getAttribute("usuario");
        if (sesion == null) return;

        Usuario u = usuarioRepository.findById(sesion.getId()).orElse(null);
        if (u == null) return;

        u.setUltimaActividad(LocalDateTime.now());

        // Si estaba ausente, volvemos a online automáticamente
        if (u.getEstado() == EstadoUsuario.ausente) {
            u.setEstado(EstadoUsuario.online);
        }

        usuarioRepository.save(u);
    }

    /**
     * PUT /presencia/estado
     * Body: { "estado": "ocupado" }
     * Permite al usuario cambiar su estado manualmente (online / ocupado / ausente).
     * No permite poner offline manualmente (eso solo lo hace el logout o el scheduler).
     */
    @PutMapping("/estado")
    public void cambiarEstado(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        Usuario sesion = (Usuario) session.getAttribute("usuario");
        if (sesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión");
        }

        String estadoStr = body.getOrDefault("estado", "").toLowerCase();
        EstadoUsuario nuevoEstado;
        try {
            nuevoEstado = EstadoUsuario.valueOf(estadoStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }

        if (nuevoEstado == EstadoUsuario.offline) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No puedes ponerte offline manualmente"
            );
        }

        Usuario u = usuarioRepository.findById(sesion.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));

        u.setEstado(nuevoEstado);
        u.setUltimaActividad(LocalDateTime.now());
        usuarioRepository.save(u);
    }
}