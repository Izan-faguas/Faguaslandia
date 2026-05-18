package com.faguaslandia.repository;

import com.faguaslandia.model.Logro;
import com.faguaslandia.model.TipoLogro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogroRepository extends JpaRepository<Logro, Long> {
    List<Logro> findByTipo(TipoLogro tipo);
    List<Logro> findByJuegoId(Long juegoId);
}