package com.faguaslandia.controller;

import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;   
import org.springframework.http.HttpStatus; 

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/crear")
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @GetMapping("/listar")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @PostMapping("/login")
    public Usuario login(@RequestBody Usuario usuario) {
    Usuario usuarioExistente = usuarioRepository.findByNombre(usuario.getNombre());
    
    if (usuarioExistente != null && usuarioExistente.getPassword().equals(usuario.getPassword())) {
        return usuarioExistente;
    } else {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
    }
}

}
