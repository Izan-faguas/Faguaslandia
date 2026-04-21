package com.faguaslandia.repository;

import com.faguaslandia.model.Amigo;
import com.faguaslandia.model.EstadoAmigo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmigoRepository extends JpaRepository<Amigo, Long> {

    List<Amigo> findByUsuario2IdAndEstado(Long usuarioId, EstadoAmigo estado);

    List<Amigo> findByUsuario1IdOrUsuario2Id(Long id1, Long id2);
}