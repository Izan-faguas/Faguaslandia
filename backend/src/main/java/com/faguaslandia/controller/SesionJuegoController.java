package com.faguaslandia.controller;

import com.faguaslandia.model.Juego;
import com.faguaslandia.model.SesionJuego;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.JuegoRepository;
import com.faguaslandia.repository.SesionJuegoRepository;
import com.faguaslandia.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/sesiones")
public class SesionJuegoController {

    private final SesionJuegoRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final JuegoRepository juegoRepository;

    public SesionJuegoController(SesionJuegoRepository sesionRepository,
                                 UsuarioRepository usuarioRepository,
                                 JuegoRepository juegoRepository) {
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.juegoRepository = juegoRepository;
    }

    /**
     * POST /sesiones/iniciar
     * Body: { "usuarioId": 1, "juegoId": 2 }
     */
    @PostMapping("/iniciar")
    public SesionJuego iniciar(@RequestBody Map<String, Long> body) {
        Long usuarioId = body.get("usuarioId");
        Long juegoId   = body.get("juegoId");

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));

        // Si ya hay una sesión activa, la cerramos antes
        sesionRepository.findByUsuarioIdAndJuegoIdAndFinIsNull(usuarioId, juegoId)
                .ifPresent(s -> {
                    s.setFin(LocalDateTime.now());
                    double horas = ChronoUnit.MINUTES.between(s.getInicio(), s.getFin()) / 60.0;
                    s.setHorasJugadas(Math.round(horas * 100.0) / 100.0);
                    sesionRepository.save(s);
                });

        SesionJuego sesion = new SesionJuego(usuario, juego);
        return sesionRepository.save(sesion);
    }

    /**
     * PUT /sesiones/finalizar/{id}
     */
    @PutMapping("/finalizar/{id}")
    public SesionJuego finalizar(@PathVariable Long id) {
        SesionJuego sesion = sesionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión no encontrada"));

        if (sesion.getFin() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sesión ya está cerrada");
        }

        sesion.setFin(LocalDateTime.now());
        double horas = ChronoUnit.MINUTES.between(sesion.getInicio(), sesion.getFin()) / 60.0;
        sesion.setHorasJugadas(Math.round(horas * 100.0) / 100.0);

        return sesionRepository.save(sesion);
    }

    /**
     * GET /sesiones/usuario/{usuarioId}/juego/{juegoId}
     * Horas totales de un usuario en un juego concreto
     */
    @GetMapping("/usuario/{usuarioId}/juego/{juegoId}")
    public Map<String, Object> horasPorJuego(
            @PathVariable Long usuarioId,
            @PathVariable Long juegoId) {
        Double horas = sesionRepository.totalHorasByUsuarioAndJuego(usuarioId, juegoId);
        return Map.of("usuarioId", usuarioId, "juegoId", juegoId, "horas", horas);
    }
}