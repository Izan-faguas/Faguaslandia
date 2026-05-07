package com.faguaslandia.controller;

import com.faguaslandia.model.EstadoUsuario;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Usuario loginRequest,
            HttpSession session
    ) {
        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no encontrado");
        }
        if (!loginRequest.getPassword().equals(usuario.getPassword())) {
            return ResponseEntity.status(401).body("Contraseña incorrecta");
        }

        // Poner online + registrar actividad inicial
        usuario.setEstado(EstadoUsuario.online);
        usuario.setUltimaActividad(LocalDateTime.now());
        usuarioRepository.save(usuario);

        session.setAttribute("usuario", usuario);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body("No hay sesión activa");
        }
        // Devolver datos frescos de BD (el estado puede haber cambiado)
        return usuarioRepository.findById(usuario.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario != null) {
            usuarioRepository.findById(usuario.getId()).ifPresent(u -> {
                u.setEstado(EstadoUsuario.offline);
                u.setUltimaActividad(null);
                usuarioRepository.save(u);
            });
        }

        session.invalidate();
        return ResponseEntity.ok("Sesión cerrada");
    }
}