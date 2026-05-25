package com.faguaslandia.service;

import com.faguaslandia.model.*;
import com.faguaslandia.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogroService {

    private final LogroRepository logroRepository;
    private final LogroUsuarioRepository logroUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompraRepository compraRepository;
    private final ResenaRepository resenaRepository;
    private final AmigoRepository amigoRepository;
    private final SesionJuegoRepository sesionJuegoRepository;

    public LogroService(LogroRepository logroRepository,
                        LogroUsuarioRepository logroUsuarioRepository,
                        UsuarioRepository usuarioRepository,
                        CompraRepository compraRepository,
                        ResenaRepository resenaRepository,
                        AmigoRepository amigoRepository,
                        SesionJuegoRepository sesionJuegoRepository) {
        this.logroRepository        = logroRepository;
        this.logroUsuarioRepository = logroUsuarioRepository;
        this.usuarioRepository      = usuarioRepository;
        this.compraRepository       = compraRepository;
        this.resenaRepository       = resenaRepository;
        this.amigoRepository        = amigoRepository;
        this.sesionJuegoRepository  = sesionJuegoRepository;
    }

    public void concederLogro(Long usuarioId, Logro logro) {
        boolean yaTiene = logroUsuarioRepository
                .findByUsuarioId(usuarioId)
                .stream()
                .anyMatch(lu -> lu.getLogro().getId().equals(logro.getId()));

        if (yaTiene) return;

        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        LogroUsuario lu = new LogroUsuario();
        lu.setUsuario(usuario);
        lu.setLogro(logro);
        lu.setFechaDesbloqueo(LocalDateTime.now());
        logroUsuarioRepository.save(lu);
    }

    public void onCompra(Long usuarioId) {
        long totalCompras = compraRepository.findByUsuarioId(usuarioId).size();
        List<Logro> logros = logroRepository.findByTipo(TipoLogro.compra);

        for (Logro logro : logros) {
            switch (logro.getNombre()) {
                case "Primer juego"    -> { if (totalCompras >= 1)  conceder(usuarioId, logro); }
                case "Coleccionista"   -> { if (totalCompras >= 3)  conceder(usuarioId, logro); }
                case "Fanático"        -> { if (totalCompras >= 5)  conceder(usuarioId, logro); }
            }
        }
    }

    public void onAmistad(Long usuarioId) {
        long totalAmigos = amigoRepository
                .findByUsuario1IdOrUsuario2Id(usuarioId, usuarioId)
                .stream()
                .filter(a -> a.getEstado() == EstadoAmigo.aceptado)
                .count();

        List<Logro> logros = logroRepository.findByTipo(TipoLogro.amistad);

        for (Logro logro : logros) {
            switch (logro.getNombre()) {
                case "Primer amigo"    -> { if (totalAmigos >= 1)  conceder(usuarioId, logro); }
                case "Bien acompañado" -> { if (totalAmigos >= 3)  conceder(usuarioId, logro); }
                case "El alma de la fiesta" -> { if (totalAmigos >= 5) conceder(usuarioId, logro); }
            }
        }
    }

    public void onResena(Long usuarioId) {
        long totalResenas = resenaRepository.countByUsuarioId(usuarioId);

        List<Logro> logros = logroRepository.findByTipo(TipoLogro.reseña);

        for (Logro logro : logros) {
            switch (logro.getNombre()) {
                case "Crítico novel"      -> { if (totalResenas >= 1)  conceder(usuarioId, logro); }
                case "Crítico experto"    -> { if (totalResenas >= 3)  conceder(usuarioId, logro); }
                case "Crítico profesional"-> { if (totalResenas >= 5)  conceder(usuarioId, logro); }
            }
        }
    }

    public void onSesion(Long usuarioId) {
        Double totalHoras = sesionJuegoRepository.totalHorasByUsuario(usuarioId);
        if (totalHoras == null) totalHoras = 0.0;

        List<Logro> logros = logroRepository.findByTipo(TipoLogro.puntuacion);

        for (Logro logro : logros) {
            switch (logro.getNombre()) {
                case "Primera hora"   -> { if (totalHoras >= 1)   conceder(usuarioId, logro); }
                case "Maratonista"    -> { if (totalHoras >= 10)  conceder(usuarioId, logro); }
                case "Sin vida social"-> { if (totalHoras >= 50)  conceder(usuarioId, logro); }
            }
        }
    }

    public void comprobarTodos(Long usuarioId) {
        onCompra(usuarioId);
        onAmistad(usuarioId);
        onResena(usuarioId);
        onSesion(usuarioId);
    }

    private void conceder(Long usuarioId, Logro logro) {
        concederLogro(usuarioId, logro);
    }
}