package com.faguaslandia.controller;

import com.faguaslandia.dto.CompraRequest;
import com.faguaslandia.model.Juego;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.service.CompraService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // =========================
    // COMPRAR
    // =========================
    @PostMapping
    public ResponseEntity<?> comprar(@RequestBody CompraRequest request, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body("No hay sesión activa");
        }
        compraService.comprar(usuario.getId(), request.getJuegoId());
        return ResponseEntity.ok().build();
    }

    // =========================
    // BIBLIOTECA
    // =========================
    @GetMapping("/usuario/{usuarioId}")
    public List<Juego> obtenerBiblioteca(@PathVariable Long usuarioId) {
        return compraService.obtenerBiblioteca(usuarioId);
    }

    // =========================
    // COMPROBAR COMPRA
    // =========================
    @GetMapping("/usuario/{usuarioId}/juego/{juegoId}")
    public boolean estaCompradoUsuario(@PathVariable Long usuarioId,
                                       @PathVariable Long juegoId) {
        return compraService.estaComprado(usuarioId, juegoId);
    }
}