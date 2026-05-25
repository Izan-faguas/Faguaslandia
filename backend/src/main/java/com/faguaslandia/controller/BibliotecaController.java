package com.faguaslandia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import com.faguaslandia.model.Juego;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.service.BibliotecaService;

@RestController
@RequestMapping("/biblioteca")
public class BibliotecaController {

    @Autowired
    private BibliotecaService bibliotecaService;

    @GetMapping
    public List<Juego> obtenerBiblioteca(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No logueado");
        }

        return bibliotecaService.obtenerPorUsuario(usuario.getId());
    }
}