package com.faguaslandia.controller;

import com.faguaslandia.model.Logro;
import com.faguaslandia.repository.LogroRepository;
import com.faguaslandia.service.LogroService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/logros")
public class LogroController {

    private final LogroService logroService;
    private final LogroRepository logroRepository;

    public LogroController(LogroService logroService, LogroRepository logroRepository) {
        this.logroService    = logroService;
        this.logroRepository = logroRepository;
    }

    @PostMapping("/conceder")
    public Map<String, String> conceder(@RequestBody Map<String, Long> body) {
        Long usuarioId = body.get("usuarioId");
        Long logroId   = body.get("logroId");

        Logro logro = logroRepository.findById(logroId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Logro no encontrado"));

        logroService.concederLogro(usuarioId, logro);
        return Map.of("mensaje", "Logro concedido");
    }
}