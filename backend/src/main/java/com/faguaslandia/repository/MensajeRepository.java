package com.faguaslandia.repository;

import com.faguaslandia.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("""
        SELECT m FROM Mensaje m
        WHERE (m.emisor.id = :id1 AND m.receptor.id = :id2)
           OR (m.emisor.id = :id2 AND m.receptor.id = :id1)
        ORDER BY m.fechaEnvio ASC
    """)
    List<Mensaje> findConversacion(@Param("id1") Long id1, @Param("id2") Long id2);

    List<Mensaje> findByReceptorIdAndLeidoFalse(Long receptorId);

    long countByEmisorIdAndReceptorIdAndLeidoFalse(Long emisorId, Long receptorId);
}