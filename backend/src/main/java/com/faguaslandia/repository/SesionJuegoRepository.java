package com.faguaslandia.repository;

import com.faguaslandia.model.SesionJuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SesionJuegoRepository extends JpaRepository<SesionJuego, Long> {

    List<SesionJuego> findByUsuarioId(Long usuarioId);

    List<SesionJuego> findByUsuarioIdAndJuegoId(Long usuarioId, Long juegoId);

    // Sesión activa (sin fin) de un usuario en un juego
    Optional<SesionJuego> findByUsuarioIdAndJuegoIdAndFinIsNull(Long usuarioId, Long juegoId);

    // Total horas jugadas de un usuario en todos sus juegos
    @Query("SELECT COALESCE(SUM(s.horasJugadas), 0) FROM SesionJuego s WHERE s.usuario.id = :usuarioId")
    Double totalHorasByUsuario(@Param("usuarioId") Long usuarioId);

    // Total horas jugadas de un usuario en un juego concreto
    @Query("SELECT COALESCE(SUM(s.horasJugadas), 0) FROM SesionJuego s WHERE s.usuario.id = :usuarioId AND s.juego.id = :juegoId")
    Double totalHorasByUsuarioAndJuego(@Param("usuarioId") Long usuarioId, @Param("juegoId") Long juegoId);
}