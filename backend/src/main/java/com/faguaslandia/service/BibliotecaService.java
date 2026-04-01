package com.faguaslandia.service;

import com.faguaslandia.model.Compra;
import com.faguaslandia.model.Juego;
import com.faguaslandia.repository.CompraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BibliotecaService {

    private final CompraRepository compraRepo;

    public BibliotecaService(CompraRepository compraRepo) {
        this.compraRepo = compraRepo;
    }

    public List<Juego> obtenerPorUsuario(Long usuarioId) {
        return compraRepo.findByUsuarioId(usuarioId)
                .stream()
                .map(Compra::getJuego)
                .toList();
    }
}