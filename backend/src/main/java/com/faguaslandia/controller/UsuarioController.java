package com.faguaslandia.controller;

import com.faguaslandia.model.Usuario;
import com.faguaslandia.model.Amigo;
import com.faguaslandia.model.EstadoAmigo;
import com.faguaslandia.repository.UsuarioRepository;
import com.faguaslandia.repository.AmigoRepository;

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
    private final AmigoRepository amigoRepository;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            AmigoRepository amigoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.amigoRepository = amigoRepository;
    }

    @PostMapping("/crear")
    public Usuario crearUsuario(@RequestBody Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El email ya está en uso"
            );
        }

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

            if (!directory.exists()) {
                directory.mkdirs();
            }

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

        if (datos.getNombre() != null) {
            usuario.setNombre(datos.getNombre());
        }

        if (datos.getEmail() != null) {
            usuario.setEmail(datos.getEmail());
        }

        if (datos.getFoto() != null) {
            usuario.setFoto(datos.getFoto());
        }

        return usuarioRepository.save(usuario);
    }

    @PostMapping("/{id1}/agregar/{id2}")
    public String enviarSolicitud(
            @PathVariable Long id1,
            @PathVariable Long id2
    ) {
        if (id1.equals(id2)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes agregarte a ti mismo"
            );
        }

        Usuario usuario1 = usuarioRepository.findById(id1)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario emisor no encontrado"
                ));

        Usuario usuario2 = usuarioRepository.findById(id2)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario receptor no encontrado"
                ));

        Amigo solicitud = new Amigo(usuario1, usuario2);

        amigoRepository.save(solicitud);

        return "Solicitud enviada";
    }

    @GetMapping("/{id}/solicitudes")
    public List<Amigo> verSolicitudes(@PathVariable Long id) {
        return amigoRepository.findByUsuario2IdAndEstado(
                id,
                EstadoAmigo.pendiente
        );
    }

    @PutMapping("/solicitud/{id}/aceptar")
    public String aceptarSolicitud(@PathVariable Long id) {
        Amigo solicitud = amigoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Solicitud no encontrada"
                ));

        solicitud.setEstado(EstadoAmigo.aceptado);

        amigoRepository.save(solicitud);

        return "Solicitud aceptada";
    }

    @GetMapping("/{id}/amigos")
    public List<Amigo> verAmigos(@PathVariable Long id) {
        return amigoRepository.findByUsuario1IdOrUsuario2Id(id, id);
    }
}