package com.faguaslandia.controller;

import com.faguaslandia.dto.MensajeRequest;
import com.faguaslandia.model.Mensaje;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.MensajeRepository;
import com.faguaslandia.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mensajes")
public class MensajeController {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;

    public MensajeController(MensajeRepository mensajeRepository,
                             UsuarioRepository usuarioRepository) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * POST /mensajes
     * Body: { "emisorId": 1, "receptorId": 2, "contenido": "Hola!" }
     */
    @PostMapping
    public Mensaje enviar(@RequestBody MensajeRequest req, HttpSession session) {
        Usuario sesion = (Usuario) session.getAttribute("usuario");
        if (sesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión");
        }

        if (req.getContenido() == null || req.getContenido().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El contenido no puede estar vacío");
        }

        Usuario emisor = usuarioRepository.findById(sesion.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Emisor no encontrado"));

        Usuario receptor = usuarioRepository.findById(req.getReceptorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receptor no encontrado"));

        Mensaje mensaje = new Mensaje(emisor, receptor, req.getContenido().trim());
        return mensajeRepository.save(mensaje);
    }

    /**
     * GET /mensajes/conversacion?id1=X&id2=Y
     * Devuelve todos los mensajes entre los dos usuarios y marca como leídos los del receptor en sesión.
     */
    @GetMapping("/conversacion")
    public List<Mensaje> getConversacion(
            @RequestParam Long id1,
            @RequestParam Long id2,
            HttpSession session
    ) {
        Usuario sesion = (Usuario) session.getAttribute("usuario");
        if (sesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No hay sesión");
        }

        List<Mensaje> mensajes = mensajeRepository.findConversacion(id1, id2);

        // Marcar como leídos los que recibe el usuario en sesión
        mensajes.stream()
                .filter(m -> m.getReceptor().getId().equals(sesion.getId()) && !m.isLeido())
                .forEach(m -> {
                    m.setLeido(true);
                    mensajeRepository.save(m);
                });

        return mensajes;
    }

    /**
     * GET /mensajes/no-leidos/{receptorId}/de/{emisorId}
     * Mensajes no leídos de un emisor concreto hacia un receptor.
     */
    @GetMapping("/no-leidos/{receptorId}/de/{emisorId}")
    public Map<String, Long> noLeidosDe(
            @PathVariable Long receptorId,
            @PathVariable Long emisorId
    ) {
        long total = mensajeRepository.countByEmisorIdAndReceptorIdAndLeidoFalse(emisorId, receptorId);
        return Map.of("total", total);
    }
}