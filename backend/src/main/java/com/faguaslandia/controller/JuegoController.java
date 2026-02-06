package com.faguaslandia.controller;

import com.faguaslandia.model.Juego;
import com.faguaslandia.service.JuegoService;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/actualizar/{id}")
    public Juego actualizarJuego(@PathVariable Long id, @RequestBody Juego datos) {
        return juegoService.actualizarJuego(id, datos);
    }

    @DeleteMapping("/eliminar/{id}")
    public String eliminarJuego(@PathVariable Long id) {
        juegoService.eliminarJuego(id);
        return "Juego eliminado correctamente";
    }
}
