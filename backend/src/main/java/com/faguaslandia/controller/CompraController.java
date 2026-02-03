package com.faguaslandia.controller;

import com.faguaslandia.model.Juego;
import com.faguaslandia.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
@CrossOrigin
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping("/{usuarioId}/{juegoId}")
    public void comprar(@PathVariable Long usuarioId,
                        @PathVariable Long juegoId) {
        compraService.comprar(usuarioId, juegoId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Juego> biblioteca(@PathVariable Long usuarioId) {
        return compraService.juegosComprados(usuarioId);
    }
}

