package com.faguaslandia.controller;

import com.faguaslandia.model.Juego;
import com.faguaslandia.service.JuegoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/juegos")
@CrossOrigin
public class JuegoController {

    private final JuegoService juegoService;

    public JuegoController(JuegoService juegoService) {
        this.juegoService = juegoService;
    }

    @GetMapping
    public List<Juego> listarJuegos() {
        return juegoService.obtenerTodos();
    }

    @PostMapping("/crear")
    public Juego crearJuego(@RequestBody Juego juego) {
        return juegoService.crearJuego(juego);
    }
    @GetMapping("/{id}")
    public Juego obtenerJuego(@PathVariable Long id) {
        return juegoService.obtenerPorId(id);
    }

    @PutMapping("/actualizar/{id}")
    public Juego actualizarJuego(@PathVariable Long id, @RequestBody Juego datos) {
        return juegoService.actualizarJuego(id, datos);
    }

    @DeleteMapping("/eliminar/{id}")
    public String eliminarJuego(@PathVariable Long id) {
        juegoService.eliminarJuego(id);
        return "Juego eliminado correctamente";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadJuego(@PathVariable Long id) throws IOException {

        Juego juego = juegoService.obtenerPorId(id);

        String fileName = juego.getTitulo().replace(" ", "_") + ".zip";

        Path file = Paths.get("games").resolve(fileName);

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + fileName)
                .body(resource);
    }
}
