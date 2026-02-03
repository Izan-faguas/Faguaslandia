package com.faguaslandia.service;

import com.faguaslandia.model.Compra;
import com.faguaslandia.model.Juego;
import com.faguaslandia.repository.CompraRepository;
import com.faguaslandia.repository.JuegoRepository;
import com.faguaslandia.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private JuegoRepository juegoRepo;

    public void comprar(Long usuarioId, Long juegoId) {
        if (compraRepo.existsByUsuarioIdAndJuegoId(usuarioId, juegoId)) {
            throw new RuntimeException("Juego ya comprado");
        }

        Compra c = new Compra();
        c.setUsuario(usuarioRepo.findById(usuarioId).orElseThrow());
        c.setJuego(juegoRepo.findById(juegoId).orElseThrow());
        c.setFechaCompra(LocalDateTime.now());

        compraRepo.save(c);
    }

    public List<Juego> juegosComprados(Long usuarioId) {
        return compraRepo.findByUsuarioId(usuarioId)
                .stream()
                .map(Compra::getJuego)
                .toList();
    }
}

