package com.faguaslandia.controller;

import com.faguaslandia.model.*;
import com.faguaslandia.repository.*;
import com.faguaslandia.service.JuegoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.faguaslandia.repository.ActualizacionRepository;

@RestController
@RequestMapping("/juegos")
public class JuegoController {

    private final JuegoService juegoService;
    private final LogroRepository logroRepository;
    private final LogroUsuarioRepository logroUsuarioRepository;
    private final SesionJuegoRepository sesionJuegoRepository;
    private final AmigoRepository amigoRepository;
    private final CompraRepository compraRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActualizacionRepository actualizacionRepository;

    public JuegoController(JuegoService juegoService,
                           LogroRepository logroRepository,
                           LogroUsuarioRepository logroUsuarioRepository,
                           SesionJuegoRepository sesionJuegoRepository,
                           AmigoRepository amigoRepository,
                           CompraRepository compraRepository,
                           UsuarioRepository usuarioRepository,
                           ActualizacionRepository actualizacionRepository) {
        this.juegoService              = juegoService;
        this.logroRepository           = logroRepository;
        this.logroUsuarioRepository    = logroUsuarioRepository;
        this.sesionJuegoRepository     = sesionJuegoRepository;
        this.amigoRepository           = amigoRepository;
        this.compraRepository          = compraRepository;
        this.usuarioRepository         = usuarioRepository;
        this.actualizacionRepository   = actualizacionRepository;
    }

    @GetMapping
    public List<Juego> listarJuegos() {
        return juegoService.obtenerTodos();
    }

    @PostMapping("/crear")
    public Juego crearJuego(@RequestBody Juego juego) {
        return juegoService.crearJuego(juego);
    }

    @GetMapping("/{id}")
    public Juego obtenerJuego(@PathVariable Long id) {
        return juegoService.obtenerPorId(id);
    }

    @PutMapping("/actualizar/{id}")
    public Juego actualizarJuego(@PathVariable Long id, @RequestBody Juego datos) {
        return juegoService.actualizarJuego(id, datos);
    }

    @DeleteMapping("/eliminar/{id}")
    public String eliminarJuego(@PathVariable Long id) {
        juegoService.eliminarJuego(id);
        return "Juego eliminado correctamente";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadJuego(@PathVariable Long id) throws IOException {
        Juego juego = juegoService.obtenerPorId(id);
        String fileName = juego.getTitulo().replace(" ", "_") + ".zip";
        Path file = Paths.get("games").resolve(fileName);
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(resource);
    }

    @GetMapping("/{id}/logros")
    public List<Map<String, Object>> getLogrosJuego(
            @PathVariable Long id,
            HttpSession session) {

        List<Logro> logros = logroRepository.findByJuegoId(id);
        Set<Long> misLogros = Collections.emptySet();

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            misLogros = logroUsuarioRepository.findByUsuarioId(usuario.getId())
                    .stream()
                    .map(lu -> lu.getLogro().getId())
                    .collect(Collectors.toSet());
        }

        Set<Long> misLogrosFinal = misLogros;
        return logros.stream().map(l -> Map.<String, Object>of(
                "id",          l.getId(),
                "nombre",      l.getNombre(),
                "descripcion", l.getDescripcion(),
                "icono",       l.getIconoUrl(),
                "desbloqueado", misLogrosFinal.contains(l.getId())
        )).collect(Collectors.toList());
    }

    @GetMapping("/{id}/mis-horas")
    public Map<String, Object> getMisHoras(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return Map.of("horas", 0.0);

        Double horas = sesionJuegoRepository.totalHorasByUsuarioAndJuego(usuario.getId(), id);
        return Map.of("horas", horas != null ? Math.round(horas * 10.0) / 10.0 : 0.0);
    }

    @GetMapping("/{id}/amigos-con-juego")
    public List<Map<String, Object>> getAmigosConJuego(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return Collections.emptyList();

        Set<Long> amigosIds = amigoRepository
                .findByUsuario1IdOrUsuario2Id(usuario.getId(), usuario.getId())
                .stream()
                .filter(a -> a.getEstado() == EstadoAmigo.aceptado)
                .map(a -> a.getUsuario1().getId().equals(usuario.getId())
                        ? a.getUsuario2().getId()
                        : a.getUsuario1().getId())
                .collect(Collectors.toSet());

        return compraRepository.findByJuegoId(id).stream()
                .filter(c -> amigosIds.contains(c.getUsuario().getId()))
                .map(c -> {
                    Usuario amigo = c.getUsuario();
                    Double horas = sesionJuegoRepository
                            .totalHorasByUsuarioAndJuego(amigo.getId(), id);
                    return Map.<String, Object>of(
                            "id",     amigo.getId(),
                            "nombre", amigo.getNombre(),
                            "foto",   amigo.getFoto() != null ? amigo.getFoto() : "default_avatar.png",
                            "horas",  horas != null ? Math.round(horas * 10.0) / 10.0 : 0.0
                    );
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/logros-estadisticas")
    public List<Map<String, Object>> getLogrosEstadisticas(@PathVariable Long id) {

        List<Logro> logros = logroRepository.findByJuegoId(id);
        long totalUsuarios = usuarioRepository.count();
        if (totalUsuarios == 0) return Collections.emptyList();

        return logros.stream().map(l -> {
            long desbloqueados = logroUsuarioRepository.countByLogroId(l.getId());
            int porcentaje = (int) (desbloqueados * 100 / totalUsuarios);
            return Map.<String, Object>of(
                    "id",          l.getId(),
                    "nombre",      l.getNombre(),
                    "icono",       l.getIconoUrl(),
                    "porcentaje",  porcentaje
            );
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}/actualizaciones")
    public List<Map<String, Object>> getActualizaciones(@PathVariable Long id) {
        return actualizacionRepository.findByJuegoIdOrderByFechaDesc(id).stream()
                .map(a -> Map.<String, Object>of(
                        "titulo",      a.getTitulo(),
                        "descripcion", a.getDescripcion() != null ? a.getDescripcion() : "",
                        "fecha",       a.getFecha().toLocalDate().toString()
                ))
                .collect(Collectors.toList());
    }
}