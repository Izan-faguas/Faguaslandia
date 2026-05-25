package com.faguaslandia.controller;

import com.faguaslandia.model.Juego;
import com.faguaslandia.model.Logro;
import com.faguaslandia.model.SesionJuego;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.JuegoRepository;
import com.faguaslandia.repository.LogroRepository;
import com.faguaslandia.repository.SesionJuegoRepository;
import com.faguaslandia.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.faguaslandia.service.LogroService;

@RestController
@RequestMapping("/sesiones")
public class SesionJuegoController {

    private final SesionJuegoRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final JuegoRepository juegoRepository;
    private final LogroRepository logroRepository;
    private final LogroService logroService;

    public SesionJuegoController(SesionJuegoRepository sesionRepository,
                                 UsuarioRepository usuarioRepository,
                                 JuegoRepository juegoRepository, LogroRepository logroRepository,
                                 LogroService logroService) {
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.juegoRepository = juegoRepository;
        this.logroRepository = logroRepository;
        this.logroService = logroService;
    }

    @PostMapping("/iniciar")
    public SesionJuego iniciar(@RequestBody Map<String, Long> body) {
        Long usuarioId = body.get("usuarioId");
        Long juegoId   = body.get("juegoId");

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));

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

        SesionJuego guardada = sesionRepository.save(sesion);
        logroService.onSesion(sesion.getUsuario().getId());
        return guardada;
    }

    @GetMapping("/usuario/{usuarioId}/juego/{juegoId}")
    public Map<String, Object> horasPorJuego(
            @PathVariable Long usuarioId,
            @PathVariable Long juegoId) {
        Double horas = sesionRepository.totalHorasByUsuarioAndJuego(usuarioId, juegoId);
        return Map.of("usuarioId", usuarioId, "juegoId", juegoId, "horas", horas);
    }

    @PostMapping("/logros/conceder")
    public ResponseEntity<?> concederDesdeJuego(@RequestBody Map<String, Long> body) {
        Long usuarioId = body.get("usuarioId");
        Long logroId   = body.get("logroId");

        Logro logro = logroRepository.findById(logroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado"));

        logroService.concederLogro(usuarioId, logro);
        return ResponseEntity.ok(Map.of("mensaje", "Logro concedido"));
    }
}