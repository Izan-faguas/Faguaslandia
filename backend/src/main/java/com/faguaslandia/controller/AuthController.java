package com.faguaslandia.controller;

import com.faguaslandia.model.EstadoUsuario;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;
import com.faguaslandia.service.LogroService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogroService logroService;

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

        usuario.setEstado(EstadoUsuario.online);
        usuario.setUltimaActividad(LocalDateTime.now());
        usuarioRepository.save(usuario);

        session.setAttribute("usuario", usuario);

        logroService.comprobarTodos(usuario.getId());

        Map<String, Object> usuarioData = new java.util.HashMap<>();
        usuarioData.put("id",     usuario.getId());
        usuarioData.put("nombre", usuario.getNombre());
        usuarioData.put("email",  usuario.getEmail());
        usuarioData.put("estado", usuario.getEstado().toString());
        usuarioData.put("foto",   usuario.getFoto() != null ? usuario.getFoto() : "default_avatar.png");


        return ResponseEntity.ok(Map.of("usuario", usuarioData));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body("No hay sesión activa");
        }
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