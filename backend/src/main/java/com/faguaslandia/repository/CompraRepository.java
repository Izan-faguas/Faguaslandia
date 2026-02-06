package com.faguaslandia.repository;

import com.faguaslandia.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    boolean existsByUsuarioIdAndJuegoId(Long usuarioId, Long juegoId);

    Optional<Compra> findByUsuarioIdAndJuegoId(Long usuarioId, Long juegoId);
}
