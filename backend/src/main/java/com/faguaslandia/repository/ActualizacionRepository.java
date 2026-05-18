package com.faguaslandia.repository;

import com.faguaslandia.model.Actualizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActualizacionRepository extends JpaRepository<Actualizacion, Long> {
    List<Actualizacion> findByJuegoIdOrderByFechaDesc(Long juegoId);
}