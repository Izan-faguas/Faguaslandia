package com.faguaslandia.controller;

import com.faguaslandia.model.*;
import com.faguaslandia.repository.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.faguaslandia.service.LogroService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AmigoRepository amigoRepository;
    private final CompraRepository compraRepository;
    private final SesionJuegoRepository sesionJuegoRepository;
    private final LogroUsuarioRepository logroUsuarioRepository;
    private final LogroService logroService;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            AmigoRepository amigoRepository,
            CompraRepository compraRepository,
            SesionJuegoRepository sesionJuegoRepository,
            LogroUsuarioRepository logroUsuarioRepository,
            LogroService logroService
    ) {
        this.usuarioRepository      = usuarioRepository;
        this.amigoRepository        = amigoRepository;
        this.compraRepository       = compraRepository;
        this.sesionJuegoRepository  = sesionJuegoRepository;
        this.logroUsuarioRepository = logroUsuarioRepository;
        this.logroService           = logroService;
    }

    @PostMapping("/crear")
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El email ya está en uso"
            );
        }
        return usuarioRepository.save(usuario);
    }

    @GetMapping("/listar")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @PutMapping(value = "/actualizar/{id}", consumes = {"multipart/form-data"})
    public Usuario actualizarUsuario(
            @PathVariable Long id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) MultipartFile foto
    ) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"
                ));

        if (nombre != null && !nombre.isBlank()) usuario.setNombre(nombre);
        if (email != null && !email.isBlank())   usuario.setEmail(email);

        if (foto != null && !foto.isEmpty()) {
            try {
                if (!foto.getContentType().startsWith("image/")) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Solo se permiten imágenes"
                    );
                }
                String folder = "uploads/avatars/";
                File directory = new File(folder);
                if (!directory.exists()) directory.mkdirs();

                File archivoAnterior = new File(folder + "user_" + id + ".jpg");
                if (archivoAnterior.exists()) archivoAnterior.delete();

                String filename = "user_" + id + ".jpg";
                Path path = Paths.get(folder + filename);
                BufferedImage original = ImageIO.read(foto.getInputStream());
                if (original == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Formato de imagen no soportado"
                    );
                }
                BufferedImage rgbImage = new BufferedImage(
                        original.getWidth(), original.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                java.awt.Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, original.getWidth(), original.getHeight());
                g2d.drawImage(original, 0, 0, null);
                g2d.dispose();
                ImageIO.write(rgbImage, "jpg", path.toFile());
                usuario.setFoto(filename);
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error guardando imagen"
                );
            }
        }

        return usuarioRepository.save(usuario);
    }

    @GetMapping("/{id}/stats")
    public Map<String, Object> getStats(@PathVariable Long id) {
        long   numJuegos = compraRepository.findByUsuarioId(id).size();
        Double horas     = sesionJuegoRepository.totalHorasByUsuario(id);
        if (horas == null) horas = 0.0;
        long   logros    = logroUsuarioRepository.countByUsuarioId(id);

        double ptsLogros = logros * 10.0;
        double ptsHoras;
        if (horas <= 50) {
            ptsHoras = horas;
        } else if (horas <= 150) {
            ptsHoras = 50 + (horas - 50) * 0.5;
        } else {
            ptsHoras = 50 + 50 + (horas - 150) * 0.2;
        }
        double puntos = ptsLogros + ptsHoras;

        int nivel;
        if      (puntos < 15)  nivel = 1;
        else if (puntos < 35)  nivel = 2;
        else if (puntos < 65)  nivel = 3;
        else if (puntos < 100) nivel = 4;
        else if (puntos < 150) nivel = 5;
        else if (puntos < 220) nivel = 6;
        else                   nivel = 7;

        double[] umbrales = {0, 15, 35, 65, 100, 150, 220, Double.MAX_VALUE};
        double ptsNivelActual = umbrales[nivel - 1];
        double ptsNivelSig    = umbrales[nivel];
        int progreso = nivel == 7 ? 100
                : (int) ((puntos - ptsNivelActual) / (ptsNivelSig - ptsNivelActual) * 100);

        return Map.of(
                "juegos",   numJuegos,
                "horas",    Math.round(horas * 10.0) / 10.0,
                "logros",   logros,
                "puntos",   (int) puntos,
                "nivel",    nivel,
                "progreso", progreso
        );
    }

    @GetMapping("/{id}/logros")
    public List<Map<String, Object>> getLogros(@PathVariable Long id) {
        try {
            return logroUsuarioRepository.findByUsuarioId(id).stream()
                    .map(lu -> Map.<String, Object>of(
                            "id",              lu.getId(),
                            "nombre",          lu.getLogro().getNombre(),
                            "descripcion",     lu.getLogro().getDescripcion(),
                            "icono",           lu.getLogro().getIconoUrl(),
                            "tipo",            lu.getLogro().getTipo().name(),
                            "fechaDesbloqueo", lu.getFechaDesbloqueo().toString()
                    ))
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}/logros-pendientes")
    public List<Map<String, Object>> getLogrosPendientes(@PathVariable Long id) {
        List<LogroUsuario> pendientes = logroUsuarioRepository.findByUsuarioIdAndNotificadoFalse(id);

        pendientes.forEach(lu -> lu.setNotificado(true));
        logroUsuarioRepository.saveAll(pendientes);

        return pendientes.stream()
                .map(lu -> Map.<String, Object>of(
                        "nombre",      lu.getLogro().getNombre(),
                        "descripcion", lu.getLogro().getDescripcion(),
                        "icono",       lu.getLogro().getIconoUrl()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    @PostMapping("/{id1}/agregar/{id2}")
    public String enviarSolicitud(@PathVariable Long id1, @PathVariable Long id2) {
        if (id1.equals(id2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes agregarte a ti mismo");
        }
        List<Amigo> existentes = amigoRepository.findByUsuario1IdOrUsuario2Id(id1, id2);
        boolean yaSolicitud = existentes.stream().anyMatch(a ->
                (a.getUsuario1().getId().equals(id1) && a.getUsuario2().getId().equals(id2)) ||
                        (a.getUsuario1().getId().equals(id2) && a.getUsuario2().getId().equals(id1))
        );
        if (yaSolicitud) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una relación con ese usuario");
        }
        Usuario u1 = usuarioRepository.findById(id1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario emisor no encontrado"));
        Usuario u2 = usuarioRepository.findById(id2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario receptor no encontrado"));
        amigoRepository.save(new Amigo(u1, u2));
        return "Solicitud enviada";
    }

    @GetMapping("/{id}/solicitudes")
    public List<Amigo> verSolicitudes(@PathVariable Long id) {
        return amigoRepository.findByUsuario2IdAndEstado(id, EstadoAmigo.pendiente);
    }

    @PutMapping("/solicitud/{id}/aceptar")
    public String aceptarSolicitud(@PathVariable Long id) {
        Amigo sol = amigoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        sol.setEstado(EstadoAmigo.aceptado);
        amigoRepository.save(sol);
        logroService.onAmistad(sol.getUsuario1().getId());
        logroService.onAmistad(sol.getUsuario2().getId());
        return "Solicitud aceptada";
    }

    @DeleteMapping("/solicitud/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id) {
        Amigo sol = amigoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        amigoRepository.delete(sol);
        return "Solicitud rechazada";
    }

    @DeleteMapping("/{id}/amigos/{amigoId}")
    public String eliminarAmigo(@PathVariable Long id, @PathVariable Long amigoId) {
        List<Amigo> relaciones = amigoRepository.findByUsuario1IdOrUsuario2Id(id, id);
        Amigo relacion = relaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmigo.aceptado &&
                        (a.getUsuario1().getId().equals(amigoId) || a.getUsuario2().getId().equals(amigoId)))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relación no encontrada"));
        amigoRepository.delete(relacion);
        return "Amigo eliminado";
    }

    @PutMapping("/{id}/bloquear/{amigoId}")
    public String bloquearAmigo(@PathVariable Long id, @PathVariable Long amigoId) {
        List<Amigo> relaciones = amigoRepository.findByUsuario1IdOrUsuario2Id(id, id);
        Amigo relacion = relaciones.stream()
                .filter(a ->
                        (a.getUsuario1().getId().equals(id) && a.getUsuario2().getId().equals(amigoId)) ||
                                (a.getUsuario1().getId().equals(amigoId) && a.getUsuario2().getId().equals(id)))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relación no encontrada"));
        relacion.setEstado(EstadoAmigo.bloqueado);
        amigoRepository.save(relacion);
        return "Usuario bloqueado";
    }

    @GetMapping("/{id}/amigos")
    public List<Amigo> verAmigos(@PathVariable Long id) {
        return amigoRepository.findByUsuario1IdOrUsuario2Id(id, id);
    }
}