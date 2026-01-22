package com.faguaslandia.service;

import com.faguaslandia.model.Juego;
import com.faguaslandia.repository.JuegoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JuegoService {

    private final JuegoRepository repo;

    public JuegoService(JuegoRepository repo) {
        this.repo = repo;
    }

    public List<Juego> obtenerTodos() {
        return repo.findAll();
    }

    public Juego crearJuego(Juego juego) {
        return repo.save(juego);
    }

    public Juego actualizarJuego(Long id, Juego datos) {
        Juego juego = repo.findById(id).orElseThrow();
        juego.setTitulo(datos.getTitulo());
        juego.setPrecio(datos.getPrecio());
        return repo.save(juego);
    }

    public void eliminarJuego(Long id) {
        repo.deleteById(id);
    }
}
