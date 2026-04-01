package com.faguaslandia.controller;

import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

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

        if (usuarioExistente != null &&
                usuarioExistente.getPassword().equals(usuario.getPassword())) {
            return usuarioExistente;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
        );
    }   

    @PostMapping("/uploadFoto")
    public String uploadFoto(@RequestParam("file") MultipartFile file) {
        try {
            String folder = "uploads/";
            File directory = new File(folder);
            if (!directory.exists()) directory.mkdirs();

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(folder + filename);
            Files.write(path, file.getBytes());

            return filename;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error subiendo imagen"
            );
        }
    }

    @PutMapping("/actualizar/{id}")
    public Usuario actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado"
                        )
                );

        if (datos.getNombre() != null) usuario.setNombre(datos.getNombre());
        if (datos.getEmail() != null) usuario.setEmail(datos.getEmail());
        if (datos.getFoto() != null) usuario.setFoto(datos.getFoto());

        return usuarioRepository.save(usuario);
    }
}