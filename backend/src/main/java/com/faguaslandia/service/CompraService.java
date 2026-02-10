package com.faguaslandia.service;

import com.faguaslandia.model.Compra;
import com.faguaslandia.model.Juego;
import com.faguaslandia.model.Usuario;
import com.faguaslandia.repository.CompraRepository;
import com.faguaslandia.repository.JuegoRepository;
import com.faguaslandia.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {

    private final CompraRepository compraRepo;
    private final UsuarioRepository usuarioRepo;
    private final JuegoRepository juegoRepo;

    public CompraService(CompraRepository compraRepo,
                         UsuarioRepository usuarioRepo,
                         JuegoRepository juegoRepo) {
        this.compraRepo = compraRepo;
        this.usuarioRepo = usuarioRepo;
        this.juegoRepo = juegoRepo;
    }

    public boolean estaComprado(Long usuarioId, Long juegoId) {
        return compraRepo.existsByUsuarioIdAndJuegoId(usuarioId, juegoId);
    }

    @Transactional
    public void comprar(Long usuarioId, Long juegoId) {
        if (estaComprado(usuarioId, juegoId)) {
            throw new RuntimeException("El juego ya está comprado");
        }

        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Juego juego = juegoRepo.findById(juegoId)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado"));

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setJuego(juego);
        compra.setFechaCompra(LocalDateTime.now());

        compraRepo.save(compra);
    }

    public List<Juego> obtenerBiblioteca(Long usuarioId) {
        return compraRepo.findByUsuarioId(usuarioId)
                .stream()
                .map(Compra::getJuego)
                .toList();
    }


}
