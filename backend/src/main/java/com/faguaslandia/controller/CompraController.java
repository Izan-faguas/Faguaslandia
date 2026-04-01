package com.faguaslandia.controller;

import com.faguaslandia.dto.CompraRequest;
import com.faguaslandia.model.Juego;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.service.BibliotecaService;
import com.faguaslandia.service.CompraService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    @GetMapping("/juego/{juegoId}")
    public boolean estaComprado(@PathVariable Long juegoId,
                                HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return compraService.estaComprado(usuario.getId(), juegoId);
    }

    // Comprar
    @PostMapping
    public ResponseEntity<?> comprar(@RequestBody CompraRequest request,
                                     HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        compraService.comprar(usuario.getId(), request.getJuegoId());

        return ResponseEntity.ok().build();
    }
}