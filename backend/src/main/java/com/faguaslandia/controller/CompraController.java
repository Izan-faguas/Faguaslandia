package com.faguaslandia.controller;

import com.faguaslandia.dto.CompraRequest;
import com.faguaslandia.model.Juego;
import com.faguaslandia.service.CompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> comprar(@RequestBody CompraRequest request) {
        compraService.comprar(request.getUsuarioId(), request.getJuegoId());
        return ResponseEntity.ok().build();
    }


    @GetMapping("/usuario/{usuarioId}")
    public List<Juego> obtenerBiblioteca(@PathVariable Long usuarioId) {
        return compraService.obtenerBiblioteca(usuarioId);
    }




}
