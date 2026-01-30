package com.faguaslandia.controller;

import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {

        System.out.println("EMAIL RECIBIDO: " + loginRequest.getEmail());
        System.out.println("PASSWORD RECIBIDA: " + loginRequest.getPassword());

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        if(!loginRequest.getPassword().equals(usuario.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        // Opcional: no devolver la contraseña
        usuario.setPassword(null);

        return ResponseEntity.ok(usuario);
    }
}
