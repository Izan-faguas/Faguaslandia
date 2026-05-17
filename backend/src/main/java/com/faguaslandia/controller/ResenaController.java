package com.faguaslandia.controller;

import com.faguaslandia.dto.ResenaRequest;
import com.faguaslandia.model.Juego;
import com.faguaslandia.model.Resena;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.JuegoRepository;
import com.faguaslandia.repository.ResenaRepository;
import com.faguaslandia.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.faguaslandia.service.LogroService;

@RestController
@RequestMapping("/resenas")
public class ResenaController {

    private final ResenaRepository resenaRepository;
    private final JuegoRepository juegoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogroService logroService;

    public ResenaController(ResenaRepository resenaRepository,
                            JuegoRepository juegoRepository,
                            UsuarioRepository usuarioRepository,
                            LogroService logroService) {
        this.resenaRepository  = resenaRepository;
        this.juegoRepository   = juegoRepository;
        this.usuarioRepository = usuarioRepository;
        this.logroService      = logroService;
    }

    /**
     * GET /resenas/juego/{juegoId}
     * Lista todas las reseñas de un juego, ordenadas de más reciente a más antigua.
     */
    @GetMapping("/juego/{juegoId}")
    public List<Resena> listarPorJuego(@PathVariable Long juegoId) {
        return resenaRepository.findByJuegoIdOrderByFechaDesc(juegoId);
    }

    /**
     * GET /resenas/juego/{juegoId}/mia
     * Devuelve la reseña del usuario en sesión para ese juego, o 404 si no existe.
     */
    @GetMapping("/juego/{juegoId}/mia")
    public Resena miResena(@PathVariable Long juegoId, HttpSession session) {
        Usuario sesion = (Usuario) session.getAttribute("usuario");
        if (sesion == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return resenaRepository.findByUsuarioIdAndJuegoId(sesion.getId(), juegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * POST /resenas/juego/{juegoId}
     * Crea o actualiza la reseña del usuario en sesión para ese juego.
     * Solo se puede reseñar si el juego está comprado.
     */
    @PostMapping("/juego/{juegoId}")
    public Resena guardar(@PathVariable Long juegoId,
                          @RequestBody ResenaRequest req,
                          HttpSession session) {

        Usuario sesion = (Usuario) session.getAttribute("usuario");
        if (sesion == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        if (req.getPuntuacion() < 1 || req.getPuntuacion() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La puntuación debe ser entre 1 y 5");
        }

        Usuario usuario = usuarioRepository.findById(sesion.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado"));

        // Crear o actualizar
        Optional<Resena> existente = resenaRepository.findByUsuarioIdAndJuegoId(sesion.getId(), juegoId);
        Resena resena = existente.orElseGet(() -> new Resena(usuario, juego, req.getPuntuacion(), req.getComentario()));

        if (existente.isPresent()) {
            resena.setPuntuacion(req.getPuntuacion());
            resena.setComentario(req.getComentario());
        }

        resenaRepository.save(resena);
        logroService.onResena(sesion.getId());

        // Actualizar valoración promedio del juego
        Double promedio = resenaRepository.calcularPromedio(juegoId);
        if (promedio != null) {
            juego.setValoracion_promedio(
                    BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP)
            );
            juegoRepository.save(juego);
        }

        return resena;
    }

    /**
     * DELETE /resenas/juego/{juegoId}
     * Elimina la reseña del usuario en sesión para ese juego.
     */
    @DeleteMapping("/juego/{juegoId}")
    public Map<String, String> eliminar(@PathVariable Long juegoId, HttpSession session) {
        Usuario sesion = (Usuario) session.getAttribute("usuario");
        if (sesion == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Resena resena = resenaRepository.findByUsuarioIdAndJuegoId(sesion.getId(), juegoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No tienes reseña para este juego"));

        resenaRepository.delete(resena);

        // Recalcular promedio tras borrar
        Double promedio = resenaRepository.calcularPromedio(juegoId);
        Juego juego = juegoRepository.findById(juegoId).orElseThrow();
        juego.setValoracion_promedio(
                promedio != null
                        ? BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO
        );
        juegoRepository.save(juego);

        return Map.of("mensaje", "Reseña eliminada");
    }
}