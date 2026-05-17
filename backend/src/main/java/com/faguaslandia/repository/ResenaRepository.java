package com.faguaslandia.repository;

import com.faguaslandia.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByJuegoIdOrderByFechaDesc(Long juegoId);

    Optional<Resena> findByUsuarioIdAndJuegoId(Long usuarioId, Long juegoId);

    @Query("SELECT AVG(r.puntuacion) FROM Resena r WHERE r.juego.id = :juegoId")
    Double calcularPromedio(@Param("juegoId") Long juegoId);

    long countByUsuarioId(Long usuarioId);
}