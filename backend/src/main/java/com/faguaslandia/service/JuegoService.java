package com.faguaslandia.service;

import com.faguaslandia.model.Juego;
import com.faguaslandia.repository.JuegoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JuegoService {

    private final JuegoRepository juegoRepository;

    public JuegoService(JuegoRepository juegoRepository) {
        this.juegoRepository = juegoRepository;
    }

    // Obtener todos los juegos
    public List<Juego> obtenerTodos() {
        return juegoRepository.findAll();
    }

    // Obtener un juego por id
    public Juego obtenerPorId(Long id) {
        return juegoRepository.findById(id).orElse(null);
    }

    // Crear un nuevo juego
    public Juego crearJuego(Juego juego) {
        return juegoRepository.save(juego);
    }

    // Actualizar juego
    public Juego actualizarJuego(Long id, Juego datos) {
        Juego juego = juegoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

        if (datos.getNombre() != null) juego.setNombre(datos.getNombre());
        if (datos.getDescripcion() != null) juego.setDescripcion(datos.getDescripcion());
        if (datos.getPrecio() != null) juego.setPrecio(datos.getPrecio());
        if (datos.getImagen_url() != null) juego.setImagen_url(datos.getImagen_url());
        if (datos.getCategoria() != null) juego.setCategoria(datos.getCategoria());
        if (datos.getDesarrollador() != null) juego.setDesarrollador(datos.getDesarrollador());
        if (datos.getFecha_lanzamiento() != null) juego.setFecha_lanzamiento(datos.getFecha_lanzamiento());
        if (datos.getValoracion_promedio() != null) juego.setValoracion_promedio(datos.getValoracion_promedio());

        return juegoRepository.save(juego);
    }

    // Eliminar juego
    public void eliminarJuego(Long id) {
        juegoRepository.deleteById(id);
    }
}
