package com.faguaslandia.controller;

import com.faguaslandia.service.CompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compras")
@CrossOrigin
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // ¿Está comprado?
    @GetMapping("/usuario/{usuarioId}/juego/{juegoId}")
    public boolean estaComprado(@PathVariable Long usuarioId,
                                @PathVariable Long juegoId) {
        return compraService.estaComprado(usuarioId, juegoId);
    }

    // Comprar
    @PostMapping
    public ResponseEntity<?> comprar(@RequestParam Long usuarioId,
                                     @RequestParam Long juegoId) {
        compraService.comprar(usuarioId, juegoId);
        return ResponseEntity.ok().build();
    }
}
