package com.faguaslandia.repository;

import com.faguaslandia.model.LogroUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogroUsuarioRepository extends JpaRepository<LogroUsuario, Long> {

    List<LogroUsuario> findByUsuarioId(Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    List<LogroUsuario> findByUsuarioIdAndNotificadoFalse(Long usuarioId);
}