package com.faguaslandia.controller;

import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest, HttpSession session) {

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }

        if (!loginRequest.getPassword().equals(usuario.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        // no enviar password al frontend
        usuario.setPassword(null);

        // guardar usuario en sesión
        session.setAttribute("usuario", usuario);

        return ResponseEntity.ok(usuario);
    }

    // comprobar sesión
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return ResponseEntity.status(401).body("No hay sesión activa");
        }

        return ResponseEntity.ok(usuario);
    }

    // logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok("Sesión cerrada");
    }
}